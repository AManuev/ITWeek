package com.jcr.sling.junit.slingtest.slingcontext;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.NameConstants;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.jcr.sling.junit.slingtest.callback.RegisterNodeTypes;
import com.jcr.sling.junit.slingtest.query.ImprovedJcrProductsDAOImpl;
import com.jcr.sling.junit.slingtest.query.ProductsDAO;
import com.jcr.sling.junit.slingtest.query.UsersDao;
import com.jcr.sling.junit.slingtest.query.add.FglJcrProductProperties;
import com.jcr.sling.junit.slingtest.query.add.Product;
import com.jcr.sling.junit.slingtest.query.add.ProductSearchResult;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.testing.mock.osgi.junit.OsgiContext;
import org.apache.sling.testing.mock.sling.MockSling;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.Calendar;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;


/**
 * <artifactId>org.apache.sling.serviceusermapper</artifactId>
 * <artifactId>org.apache.sling.resourceresolver</artifactId>
 * <artifactId>io.wcm.testing.aem-mock</artifactId>
 * <p/>
 * Improved? Huh.. :)
 */
@RunWith(MockitoJUnitRunner.class)
public class ImprovedJcrProductsDAOImplTest {

    private static final String SOURCE_NODE_PATH = "/etc/commerce/products/sportchek/source";
    private static final String MASTER_NODE_PATH = "/etc/commerce/products/sportchek/master";

    private static final int GHOST_PRODUCTS_NUMBER = 10;
    private static final String ANY_BRAND = "Any Brand";
    private static final String TEST_EXT_ID = "424242";

    private static final String NOT_ALL_PROPERTIES_POPULATED = "Not all product properties populated";

    // let use Sling Context object to test our class with query
    @Rule
    public final SlingContext context = new SlingContext(new RegisterNodeTypes(), ResourceResolverType.JCR_JACKRABBIT);

    // I cannot find a way to retrieve an internal SlingRepository with current API
    // May be something change in the future.:)
    @Mock
    private SlingRepository mSlingRepository;

    @Mock
    private UsersDao usersDao;

    @InjectMocks
    private final ProductsDAO jcrProductsDAO = new ImprovedJcrProductsDAOImpl();

    private Session session;

    @Before
    public void setUpRepository() throws RepositoryException, IOException, LoginException {

        // A BIG PROBLEM!!! Pay attention.
        //Avoid using context tips like context.resourceResolver();
        // resourceResolver is not related with session, but adapted to session
        //create only in session!
        //JcrUtils.getOrCreateByPath()
        session = context.resourceResolver().adaptTo(Session.class);

        // Repository is not clear after test - so be prepare that node already exist or clean folder
        JcrUtils.getOrCreateByPath(MASTER_NODE_PATH, JcrConstants.NT_UNSTRUCTURED, JcrConstants.NT_UNSTRUCTURED, session, false);

        given(mSlingRepository.loginAdministrative(null)).willReturn(session);

        for (int i = 0; i < GHOST_PRODUCTS_NUMBER; ++i) {
            getProduct(String.valueOf(i));
        }

        given(usersDao.getUserDisplayName(any(String.class))).willReturn("Administrator");
    }


    private Node getProduct(final String productId) throws RepositoryException {
        Node product = generateProductNode(productId);

        JcrUtil.setProperty(product, FglJcrProductProperties.ACTIVATION_DATE, Calendar.getInstance());
        JcrUtil.setProperty(product, FglJcrProductProperties.AVAILABILITY_DATE, Calendar.getInstance());
        JcrUtil.setProperty(product, FglJcrProductProperties.ECOMM_LAST_MODIFIED_DATE, Calendar.getInstance());
        JcrUtil.setProperty(product, FglJcrProductProperties.CREATION_DATE, Calendar.getInstance());
        JcrUtil.setProperty(product, FglJcrProductProperties.EXT_ID, productId);
        JcrUtil.setProperty(product, FglJcrProductProperties.FEATURES, FglJcrProductProperties.FEATURES);
        JcrUtil.setProperty(product, FglJcrProductProperties.HYBRIS_GIFT_WRAPPABLE, true);
        JcrUtil.setProperty(product, FglJcrProductProperties.SELLABLE, false);
        JcrUtil.setProperty(product, FglJcrProductProperties.LAST_IMPORTED_DATE, Calendar.getInstance());
        JcrUtil.setProperty(product, FglJcrProductProperties.LONG_DESCRIPTION, FglJcrProductProperties.LONG_DESCRIPTION + productId);
        JcrUtil.setProperty(product, FglJcrProductProperties.PRODUCT_NAME, FglJcrProductProperties.PRODUCT_NAME + productId);
        JcrUtil.setProperty(product, FglJcrProductProperties.PRODUCT_COMPARABLE, true);
        JcrUtil.setProperty(product, FglJcrProductProperties.PRODUCT_STATUS, productId);
        JcrUtil.setProperty(product, FglJcrProductProperties.PRODUCT_TITLE, FglJcrProductProperties.PRODUCT_TITLE + productId);
        JcrUtil.setProperty(product, FglJcrProductProperties.PROMO_MESSAGE, FglJcrProductProperties.PROMO_MESSAGE + productId);
        JcrUtil.setProperty(product, FglJcrProductProperties.SHIP_TO_STORE, true);
        JcrUtil.setProperty(product, FglJcrProductProperties.SPECIFICATION, FglJcrProductProperties.SPECIFICATION + productId);
        JcrUtil.setProperty(product, FglJcrProductProperties.ECOMM_FULFILLER_ID, productId);
        JcrUtil.setProperty(product, FglJcrProductProperties.STATUS, Product.Status.NEW.getStatusId());
        JcrUtil.setProperty(product, FglJcrProductProperties.ECOMM_FULFILLER_NAME, FglJcrProductProperties.ECOMM_FULFILLER_NAME
                + productId);
        JcrUtil.setProperty(product, FglJcrProductProperties.ECOMM_ASSEMBLY_REQUIRED, true);
        JcrUtil.setProperty(product, FglJcrProductProperties.ECOMM_STICK_WARRANTY, true);
        JcrUtil.setProperty(product, NameConstants.PN_PAGE_LAST_MOD_BY, "admin");
        JcrUtil.setProperty(product, FglJcrProductProperties.ECOMM_BRAND, productId);
        return product;
    }

    private Node generateProductNode(final String productId) throws RepositoryException {

        final Node product = JcrUtils.getOrCreateByPath(SOURCE_NODE_PATH.concat("/PRODUCT " + productId),
                JcrConstants.NT_UNSTRUCTURED, NameConstants.NT_PAGE, session, false);
        final Node content = JcrUtils.getOrAddNode(product, JcrConstants.JCR_CONTENT, "cq:PageContent");
        return JcrUtils.getOrAddNode(content, "product", JcrConstants.NT_UNSTRUCTURED);
    }

    private void assertProductPropertiesPopulated(final Product product) {

        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getActivationDate());
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getEcommAvailabilityDate());
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getEcommLastModifiedDate());
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getEcommCreationDate());
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getExtId());
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getFeatures());
        assertTrue(NOT_ALL_PROPERTIES_POPULATED, product.isHybrisGiftWrappable());
        assertTrue(NOT_ALL_PROPERTIES_POPULATED, product.isSellable());
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getLastImportedDate());
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getLongDescription());
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getPmmProductTitle());
        assertTrue(NOT_ALL_PROPERTIES_POPULATED, product.isProductComparable());
        assertTrue(NOT_ALL_PROPERTIES_POPULATED, product.getProductStatus() > 0);
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getProductTitle());
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getPromoMessage());
        assertTrue(NOT_ALL_PROPERTIES_POPULATED, product.isShipToStore());
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getSpecification());
        assertTrue(NOT_ALL_PROPERTIES_POPULATED, product.getFulfillerId() > 0);
        assertTrue(NOT_ALL_PROPERTIES_POPULATED, product.getPublishStatus() > 0);
        assertNotNull(NOT_ALL_PROPERTIES_POPULATED, product.getFulfillerName());
        assertTrue(NOT_ALL_PROPERTIES_POPULATED, product.isAssemblyRequired());
        assertTrue(NOT_ALL_PROPERTIES_POPULATED, product.isStickWarranty());
    }

    @Test
    public void shouldPopulateAllProductProperties() throws RepositoryException, PersistenceException {

        final Node product = getProduct(TEST_EXT_ID);
        JcrUtil.setProperty(product, FglJcrProductProperties.ECOMM_BRAND, ANY_BRAND);
        JcrUtil.setProperty(product, FglJcrProductProperties.SELLABLE, true);
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.of("pmmId", TEST_EXT_ID);
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertProductPropertiesPopulated(searchResult.getProducts().get(0));
    }

    /* ... */
}