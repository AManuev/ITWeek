package com.jcr.sling.junit.transientRepository;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jcr.sling.junit.slingtest.holders.ResolverHolder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.apache.sling.jcr.resource.JcrResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.jcr.sling.junit.transientRepository.FeedGenerationPersisterHelper.createFilterQuery;
import static com.jcr.sling.junit.transientRepository.FeedGenerationPersisterHelper.createMaintenanceQuery;

public class DefaultFeedGenerationPersister {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFeedGenerationPersister.class);

    public static final String FEED_GENERATION_RESULT = "feed.generation.result";
    public static final String FEED_GENERATION_ID = "feed.generation.id";
    public static final String FEED_GENERATION_STATE = "feed.generation.state";
    public static final String FEED_GENERATION_START_TIME = "feed.generation.start.time";
    public static final String FEED_EXPORTER_MANAGER_ID = "feed.exporter.manager.id";
    public static final String FEED_EXPORTER_TYPE = "feed.exporter.type";

    private static final String DEFAULT_PERSIST_PATH = "/etc/sportchek/feeding/generations";

    @Property(label = "Persist path", description = "Path in repository for feed generation information storage", value = DEFAULT_PERSIST_PATH)
    private static final String PERSIST_PATH = "persist.path";

    private static final int DEFAULT_HISTORY_PERIOD = 7;
    @Property(label = "History duration", description = "For how many days feed generation history should be kept", intValue = DEFAULT_HISTORY_PERIOD)
    private static final String HISTORY_DURATION = "history.duration";

    private static final Cache<String, Map<String, Object>> CACHE = CacheBuilder.newBuilder().weakValues().build();

    private static final Collection<String> SYSTEM_PROPERTIES = ImmutableList.<String>builder()
            .add(FEED_GENERATION_ID).add(FEED_GENERATION_STATE)
            .add(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY).add(JcrConstants.JCR_PRIMARYTYPE).build();

    private static final Collection<String> MANDATORY_PROPERTIES = ImmutableList.<String>builder()
            .add(FEED_GENERATION_START_TIME).add(FEED_EXPORTER_TYPE)
            .add(FEED_EXPORTER_MANAGER_ID).add(FEED_GENERATION_STATE)
            .build();

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private String repositoryPath;
    private int historyPeriod;


    protected void activate(final Map<String, Object> properties) {
        final String rawRepositoryPath = PropertiesUtil.toString(properties.get(PERSIST_PATH), DEFAULT_PERSIST_PATH);
        repositoryPath = StringUtils.removeEnd(rawRepositoryPath, "/");
        historyPeriod = PropertiesUtil.toInteger(properties.get(HISTORY_DURATION), DEFAULT_HISTORY_PERIOD);
    }

    public Optional<Map<String, Object>> getFeedGenerationProperties(final String feedGenerationId) {

        Optional<Map<String, Object>> result = Optional.absent();
        try {
            result = Optional.of(getFromCache(feedGenerationId));
        } catch (ExecutionException e) {
            final Throwable root = Throwables.getRootCause(e);
            LOGGER.warn(String.format("Unable to find feed generation with id '%s'", feedGenerationId), root);
        }
        return result;
    }

    public void persistFeedGenerationProperties(final String feedGenerationId, final Map<String, Object> properties)
            throws RepositoryException {

        Preconditions.checkNotNull(properties);
        validateMandatoryProperties(properties);
        try (ResolverHolder holder = new ResolverHolder(resourceResolverFactory)) {
            final Session session = holder.getResolver().adaptTo(Session.class);
            final String nodePath = getNodePath(feedGenerationId, properties);
            final Node node = JcrUtils.getOrCreateByPath(nodePath, JcrResourceConstants.NT_SLING_FOLDER,
                    NodeType.NT_UNSTRUCTURED, session, false);

            setNodeProperties(node, properties);
            setExtraProperties(node, feedGenerationId, properties);

            session.save();
            CACHE.put(feedGenerationId, ImmutableMap.copyOf(properties));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Feed generation with id '{}' successfully persisted with properties [{}].",
                        feedGenerationId, properties);
            }
        }
    }

    public Collection<Map<String, Object>> findFeedGenerationProperties(final Map<String, Object> filter)
            throws RepositoryException {

        Collection<Map<String, Object>> result;
        try (ResolverHolder holder = new ResolverHolder(resourceResolverFactory)) {
            final Session session = holder.getResolver().adaptTo(Session.class);
            final Node root = getRootNode(session);
            result = findFeedGenerations(session, root, filter);
        }
        return result;
    }

    public void run() {
        this.maintenance();
    }

    private Map<String, Object> getFromCache(final String feedGenerationId) throws ExecutionException {
        return CACHE.get(feedGenerationId, new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() throws RepositoryException {
                final Map<String, Object> filter = ImmutableMap.of(FEED_GENERATION_ID,
                        (Object) feedGenerationId);
                final Collection<Map<String, Object>> generations = findFeedGenerationProperties(filter);
                return generations.iterator().next();
            }
        });
    }

    private void maintenance() {
        LOGGER.debug("Running maintenance task...");
        try (ResolverHolder holder = new ResolverHolder(resourceResolverFactory)) {
            final Session session = holder.getResolver().adaptTo(Session.class);
            final Query query = createMaintenanceQuery(session, getRootNode(session), historyPeriod);
            final NodeIterator nodeIterator = query.execute().getNodes();

            int count = 0;
            while (nodeIterator.hasNext()) {
                final Node node = nodeIterator.nextNode();
                final String feedGenerationId = JcrUtils.getStringProperty(node,
                        FEED_GENERATION_ID, StringUtils.EMPTY);
                final String feedFile = JcrUtils.getStringProperty(node, FEED_GENERATION_RESULT,
                        StringUtils.EMPTY);

                removeNodeOrFolderIfPossible(node);
                CACHE.invalidate(feedGenerationId);
                FileUtils.deleteQuietly(new File(feedFile));

                if (++count % 100 == 0) {
                    session.save();
                }
            }
            session.save();

        } catch (RepositoryException e) {
            LOGGER.warn("Error occurred during maintenance", e);
        }
        LOGGER.debug("Maintenance task completed");
    }

    private void removeNodeOrFolderIfPossible(final Node node) throws RepositoryException {
        Node parent = node.getParent();
        Node toRemove = node;
        while (Iterators.size(parent.getNodes()) == 1) {
            toRemove = parent;
            parent = parent.getParent();
            if (repositoryPath.equals(parent.getPath())) {
                break;
            }
        }
        toRemove.remove();
    }

    private Node getRootNode(final Session session) throws RepositoryException {
        return JcrUtils.getOrCreateByPath(repositoryPath, JcrResourceConstants.NT_SLING_FOLDER, session);
    }

    private String getNodePath(final String feedGenerationId, final Map<String, Object> properties) {
        final Calendar startTime = Calendar.getInstance();
        final StringBuilder builder = new StringBuilder();

        startTime.setTimeInMillis((long) properties.get(FEED_GENERATION_START_TIME));

        builder.append(repositoryPath);
        builder.append('/');
        builder.append(startTime.get(Calendar.YEAR));
        builder.append('/');
        builder.append(startTime.get(Calendar.MONTH) + 1);
        builder.append('/');
        builder.append(startTime.get(Calendar.DAY_OF_MONTH));
        builder.append('/');
        builder.append(JcrUtil.createValidName(feedGenerationId));

        return builder.toString();
    }

    private static void setNodeProperties(final Node node, final Map<String, Object> properties)
            throws RepositoryException {
        final Map<String, Object> filteredProperties = Maps.filterKeys(properties,
                Predicates.not(Predicates.in(SYSTEM_PROPERTIES)));

        for (Map.Entry<String, Object> entry : filteredProperties.entrySet()) {
            JcrResourceUtil.setProperty(node, entry.getKey(), entry.getValue());
        }
    }

    private static void setExtraProperties(final Node node, final String feedGenerationId,
                                           final Map<String, Object> properties) throws RepositoryException {
        JcrResourceUtil.setProperty(node, FEED_GENERATION_ID, feedGenerationId);
        Object state = properties.get(FEED_GENERATION_STATE);
        if (state != null) {
            if (state.getClass().isEnum()) {
                state = state.toString();
            }
            JcrResourceUtil.setProperty(node, FEED_GENERATION_STATE, state);
        }
    }

    private static Collection<Map<String, Object>> findFeedGenerations(final Session session, final Node root,
                                                                       final Map<String, Object> filter) throws RepositoryException {
        final Query query = createFilterQuery(session, root, filter);
        final NodeIterator nodeIterator = query.execute().getNodes();
        final Collection<Map<String, Object>> result = Lists.newArrayList();
        while (nodeIterator.hasNext()) {
            final Node node = nodeIterator.nextNode();
            final Map<String, Object> properties = FeedGenerationPersisterHelper.toMap(node);

            result.add(properties);
            CACHE.put(properties.get(FEED_GENERATION_ID).toString(),
                    ImmutableMap.copyOf(properties));
        }
        return result;
    }

    private static void validateMandatoryProperties(final Map<String, Object> properties) {
        final Collection<String> missingProperties = Lists.newArrayList();
        for (String property : MANDATORY_PROPERTIES) {
            if (properties.get(property) == null) {
                missingProperties.add(property);
            }
        }
        Preconditions.checkArgument(missingProperties.isEmpty(), "Missing mandatory properties '%s'", missingProperties);
    }

}
