package com.jcr.sling.junit.transientRepository;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.testing.jcr.RepositoryUtil;
import org.apache.sling.testing.mock.jcr.MockJcr;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFeedGenerationPersisterTest {

    public static final String FEED_GENERATION_RESULT = "feed.generation.result";
    public static final String FEED_GENERATION_STATE = "feed.generation.state";
    public static final String FEED_GENERATION_START_TIME = "feed.generation.start.time";
    public static final String FEED_EXPORTER_MANAGER_ID = "feed.exporter.manager.id";
    public static final String FEED_EXPORTER_TYPE = "feed.exporter.type";
    public static final String FEED_EXPORTER_TYPE_FULL = "feed.exporter.type.full";

    @ClassRule
    public static TemporaryFolder TEMP_FOLDER = new TemporaryFolder();

    private static Session REAL_SESSION;

    private static final DateTime START_TIME = new DateTime().minusYears(1);

    private static final String FEED_GENERATION_ID = UUID.randomUUID().toString();

    private static final Map<String, Object> DEFAULT_FEED_GENERATION = ImmutableMap.<String, Object>builder()
            .put(FEED_EXPORTER_TYPE, FEED_EXPORTER_TYPE_FULL)
            .put(FEED_EXPORTER_MANAGER_ID, "feed_exporter_id")
            .put(FEED_GENERATION_STATE, FeedGeneration.FeedGenerationState.RUNNING)
            .put(FEED_GENERATION_START_TIME, START_TIME.getMillis()).build();

    private static final String REPOSITORY_PATH = "/etc/repository/path";

    private static final Map<String, Object> DEFAULT_CONFIGURATION = ImmutableMap.<String, Object>builder()
            .put("persist.path", REPOSITORY_PATH + "/").put("history.duration", 1).build();

    private static final String DEFAULT_FEED_GENERATION_FOLDER_PATH = REPOSITORY_PATH + "/" + START_TIME.getYear()
            + "/" + START_TIME.getMonthOfYear() + "/" + START_TIME.getDayOfMonth();

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @Mock
    private ResourceResolver resourceResolver;

    @InjectMocks
    private DefaultFeedGenerationPersister testedInstance;

    @BeforeClass
    public static void setUpRepository() throws IOException, RepositoryException {

        final InputStream configStream = DefaultFeedGenerationPersisterTest.class.getClassLoader().getResourceAsStream("repository.xml");

        RepositoryConfig config = RepositoryConfig.create(configStream, TEMP_FOLDER.newFolder().getAbsolutePath());

        TransientRepository REPOSITORY = new TransientRepository(config);

        final Credentials credentials = new SimpleCredentials("admin", "admin".toCharArray());

        REAL_SESSION = REPOSITORY.login(credentials, "default");

        boolean registered = RepositoryUtil.registerNodeType(REAL_SESSION, DefaultFeedGenerationPersisterTest.class
                .getClassLoader().getResourceAsStream("cq.cnd"));

        assertTrue("Failed to register node types.", registered);
    }

    @AfterClass
    public static void destroyRepository() {
        REAL_SESSION.logout();
    }

    @Before
    public void setUp() throws Exception {
        when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenReturn(resourceResolver);
        when(resourceResolver.adaptTo(Session.class)).thenReturn(REAL_SESSION);
    }

    @After
    public void tearDown() throws Exception {
        for (Node node : JcrUtils.getChildNodes(REAL_SESSION.getNode(REPOSITORY_PATH))) {
            node.remove();
        }
        REAL_SESSION.save();
    }

    @Test
    public void shouldAddNewFeedGeneration() throws Exception {
        testedInstance.activate(DEFAULT_CONFIGURATION);

        testedInstance.persistFeedGenerationProperties(FEED_GENERATION_ID, DEFAULT_FEED_GENERATION);

        assertTrue("Repository is not created", REAL_SESSION.nodeExists(REPOSITORY_PATH));
        assertTrue("Folder is not created", REAL_SESSION.nodeExists(DEFAULT_FEED_GENERATION_FOLDER_PATH));
        for (String property : DEFAULT_FEED_GENERATION.keySet()) {
            assertTrue(
                    "Property " + property + " is not persisted",
                    REAL_SESSION.propertyExists(DEFAULT_FEED_GENERATION_FOLDER_PATH + "/" + FEED_GENERATION_ID + "/"
                            + property));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainMissingMandatoryProperties() throws Exception {
        testedInstance.activate(DEFAULT_CONFIGURATION);

        testedInstance.persistFeedGenerationProperties(FEED_GENERATION_ID, Collections.<String, Object>emptyMap());
    }

    @Test
    public void shouldUpdateFeedGeneration() throws Exception {
        testedInstance.activate(DEFAULT_CONFIGURATION);
        testedInstance.persistFeedGenerationProperties(FEED_GENERATION_ID, DEFAULT_FEED_GENERATION);
        final Map<String, Object> newProperties = Maps.newHashMap(DEFAULT_FEED_GENERATION);
        newProperties
                .putAll(ImmutableMap.<String, Object>builder().put("test.property", "value")
                        .put(FEED_GENERATION_STATE, FeedGeneration.FeedGenerationState.SUCCEEDED)
                        .build());

        testedInstance.persistFeedGenerationProperties(FEED_GENERATION_ID, newProperties);

        assertTrue(
                "New property is not added",
                REAL_SESSION.propertyExists(DEFAULT_FEED_GENERATION_FOLDER_PATH + "/" + FEED_GENERATION_ID
                        + "/test.property"));
        assertEquals(
                "Existing property is not updated",
                REAL_SESSION.getProperty(
                        DEFAULT_FEED_GENERATION_FOLDER_PATH + "/" + FEED_GENERATION_ID + "/"
                                + FEED_GENERATION_STATE).getString(),
                FeedGeneration.FeedGenerationState.SUCCEEDED.name());
    }

    @Test
    public void shouldGetFeedGenerationById() throws Exception {
        testedInstance.activate(DEFAULT_CONFIGURATION);
        testedInstance.persistFeedGenerationProperties(FEED_GENERATION_ID, DEFAULT_FEED_GENERATION);

        Optional<Map<String, Object>> result = testedInstance.getFeedGenerationProperties(FEED_GENERATION_ID);

        assertTrue(result.isPresent());
        assertThat(result.get().keySet(), hasItems(DEFAULT_FEED_GENERATION.keySet().toArray(new String[DEFAULT_FEED_GENERATION.keySet().size()])));
    }

    @Test
    public void shouldFindFeedGenerationByFilter() throws Exception {
        testedInstance.activate(DEFAULT_CONFIGURATION);
        testedInstance.persistFeedGenerationProperties(FEED_GENERATION_ID, DEFAULT_FEED_GENERATION);

        Collection<Map<String, Object>> result = testedInstance.findFeedGenerationProperties(ImmutableMap
                .<String, Object>builder()
                .put(FEED_EXPORTER_TYPE, QueryBuilderHelper.IS_NOT_NULL)
                .put("missing.property", QueryBuilderHelper.IS_NULL)
                .put(FEED_GENERATION_STATE, FeedGeneration.FeedGenerationState.RUNNING)
                .put(FEED_EXPORTER_MANAGER_ID, "feed_exporter_id").build());

        assertFalse(result.isEmpty());
    }

    @Test
    public void shouldRemoveNodeAndFolderDuringMaintenance() throws Exception {
        final File file = TEMP_FOLDER.newFile();
        testedInstance.activate(DEFAULT_CONFIGURATION);
        testedInstance.persistFeedGenerationProperties(
                FEED_GENERATION_ID,
                ImmutableMap.<String, Object>builder().putAll(DEFAULT_FEED_GENERATION)
                        .put(FEED_GENERATION_RESULT, file.getAbsolutePath()).build());

        testedInstance.run();

        assertFalse("Folder is not removed", REAL_SESSION.nodeExists(REPOSITORY_PATH + "/" + START_TIME.getYear()));
        assertFalse("Feed file is not removed", file.exists());
    }


}