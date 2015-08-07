package com.jcr.sling.junit.transientRepository;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.jcr.resource.JcrResourceUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class FeedGenerationPersisterHelper {

    public static final String FEED_GENERATION_START_TIME = "feed.generation.start.time";

    private static final String SELECTOR = "s";

    private FeedGenerationPersisterHelper() {

    }

    static Query createFilterQuery(final Session session, final Node root, final Map<String, Object> filter)
            throws RepositoryException {
        final Constraint constraint = createFilterConstraint(session, root, filter);
        return buildQuery(session, constraint);
    }

    static Query createMaintenanceQuery(final Session session, final Node root, final int period)
            throws RepositoryException {
        final QueryObjectModelFactory qomf = session.getWorkspace().getQueryManager().getQOMFactory();
        final Value periodValue = JcrResourceUtil.createValue(
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(period), session);

        final Constraint rootConstraint = qomf.descendantNode(SELECTOR, root.getPath());
        final Constraint filterConstraint = qomf.comparison(
                qomf.propertyValue(SELECTOR, FEED_GENERATION_START_TIME),
                QueryObjectModelFactory.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO, qomf.literal(periodValue));

        return buildQuery(session, qomf.and(rootConstraint, filterConstraint));
    }

    private static Query buildQuery(final Session session, final Constraint constraint) throws RepositoryException {
        final QueryObjectModelFactory qomf = session.getWorkspace().getQueryManager().getQOMFactory();
        final Selector selector = qomf.selector(NodeType.NT_UNSTRUCTURED, SELECTOR);
        final Ordering ordering = qomf.ascending(qomf.propertyValue(SELECTOR,
                FEED_GENERATION_START_TIME));

        return qomf.createQuery(selector, constraint, new Ordering[]{ordering}, null);
    }

    private static Constraint createFilterConstraint(final Session session, final Node root,
                                                     final Map<String, Object> filter) throws RepositoryException {
        final QueryObjectModelFactory qomf = session.getWorkspace().getQueryManager().getQOMFactory();

        final Constraint rootConstraint = qomf.descendantNode(SELECTOR, root.getPath());
        final Constraint filterConstraint = QueryBuilderHelper.buildFilterConstraint(session, SELECTOR, filter);

        return qomf.and(rootConstraint, filterConstraint);

    }

    static Map<String, Object> toMap(final Node node) throws RepositoryException {
        final Map<String, Object> result = new HashMap<>();
        for (javax.jcr.Property property : JcrUtils.getProperties(node)) {
            final Object value = JcrResourceUtil.toJavaObject(property);
            if (value != null) {
                result.put(property.getName(), value);
            }
        }
        return result;
    }

}
