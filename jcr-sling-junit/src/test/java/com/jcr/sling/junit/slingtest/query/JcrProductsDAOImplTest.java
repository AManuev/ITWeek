package com.jcr.sling.junit.slingtest.query;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.NameConstants;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.jcr.sling.junit.slingtest.query.add.FglJcrProductProperties;
import com.jcr.sling.junit.slingtest.query.add.Product;
import com.jcr.sling.junit.slingtest.query.add.ProductSearchResult;
import com.jcr.sling.junit.slingtest.query.add.ProductsSelector;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.commons.testing.jcr.RepositoryProvider;
import org.apache.sling.commons.testing.jcr.RepositoryUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class JcrProductsDAOImplTest {

    private static final String ROOT_NODE_PATH = "/etc/commerce/products/sportchek/source";
    private static final String SLING_FOLDER_NODE_TYPE = "sling:Folder";

    private static final int GHOST_PRODUCTS_NUMBER = 10;
    private static final String ANY_BRAND = "Any Brand";
    private static final String TEST_EXT_ID = "424242";

    private static final String NOT_ALL_PROPERTIES_POPULATED = "Not all product properties populated";
    private static final String SCLS_22_SHORTS_CATEGORY_TAG = "sportchek:categories/DIV-03-SOFTGOODS/DPT-72-CASUAL-CLOTHING/SDPT-03-BOYS/CLS-14-ACTION-SPORTS-APPAREL/SCLS-22-SHORTS";
    private static final String SDPT_03_BOYS = "sportchek:categories/DIV-03-SOFTGOODS/DPT-72-CASUAL-CLOTHING/SDPT-03-BOYS";
    public static final String DAM_IMAGE_NODE_PATH = "/content/dam/sportchek/product-images/prd/000/prd000001_10_a.png";

    @Mock
    private SlingRepository mockSlingRepository;
    @Mock
    private UsersDao usersDao;

    @InjectMocks
    private final JcrProductsDAOImpl jcrProductsDAO = new JcrProductsDAOImpl();

    private Session session;
    private Node rootNode;

    @BeforeClass
    public static void configureRepository() throws RepositoryException, IOException {
        Session session = RepositoryProvider.instance().getRepository().loginAdministrative(null);
        try {
            RepositoryUtil.registerNodeType(session,
                    JcrProductsDAOImplTest.class.getClassLoader().getResourceAsStream("cq.cnd"));
            session.save();
        } finally {
            session.logout();
        }
    }

    @Before
    public void setUp() throws RepositoryException, IOException {
        session = RepositoryProvider.instance().getRepository().loginAdministrative(null);

        rootNode = JcrUtil.createPath(ROOT_NODE_PATH, SLING_FOLDER_NODE_TYPE, session);

        // Ghost products - some set of products, not responding gto the search criteria in tests
        for (int i = 0; i < GHOST_PRODUCTS_NUMBER; ++i) {
            createProductNode(rootNode, Integer.toString(i), Integer.toString(i));
        }
        session.save();

        Session spiedSession = spy(session);
        doNothing().when(spiedSession).logout();
        given(mockSlingRepository.loginAdministrative(null)).willReturn(spiedSession);
        given(usersDao.getUserDisplayName(any(String.class))).willReturn("Administrator");
    }

    @After
    public void tearDown() throws RepositoryException {
        try {
            rootNode.remove();
            session.save();
        } finally {
            session.logout();
        }
    }

    private static Node createProductNode(final Node rootNode, final String productId, final String brand)
            throws RepositoryException {

        Node productPageNode = rootNode.addNode("PRODUCT " + brand, NameConstants.NT_PAGE);
        final Node productNode = productPageNode.addNode(JcrConstants.JCR_CONTENT,
                "cq:PageContent").addNode("product", JcrConstants.NT_UNSTRUCTURED);
        fillProductContentNode(productId, productNode);
        productNode.setProperty(FglJcrProductProperties.ECOMM_BRAND, brand);

        return productNode;
    }

    private static void fillProductContentNode(final String productId, final Node node) throws RepositoryException {
        node.setProperty(FglJcrProductProperties.ACTIVATION_DATE, Calendar.getInstance());
        node.setProperty(FglJcrProductProperties.AVAILABILITY_DATE, Calendar.getInstance());
        node.setProperty(FglJcrProductProperties.ECOMM_LAST_MODIFIED_DATE, Calendar.getInstance());
        node.setProperty(FglJcrProductProperties.CREATION_DATE, Calendar.getInstance());
        node.setProperty(FglJcrProductProperties.EXT_ID, productId);
        node.setProperty(FglJcrProductProperties.FEATURES, FglJcrProductProperties.FEATURES);
        node.setProperty(FglJcrProductProperties.HYBRIS_GIFT_WRAPPABLE, true);
        node.setProperty(FglJcrProductProperties.SELLABLE, false);
        node.setProperty(FglJcrProductProperties.LAST_IMPORTED_DATE, Calendar.getInstance());
        node.setProperty(FglJcrProductProperties.LONG_DESCRIPTION, FglJcrProductProperties.LONG_DESCRIPTION + productId);
        node.setProperty(FglJcrProductProperties.PRODUCT_NAME, FglJcrProductProperties.PRODUCT_NAME + productId);
        node.setProperty(FglJcrProductProperties.PRODUCT_COMPARABLE, true);
        node.setProperty(FglJcrProductProperties.PRODUCT_STATUS, productId);
        node.setProperty(FglJcrProductProperties.PRODUCT_TITLE, FglJcrProductProperties.PRODUCT_TITLE + productId);
        node.setProperty(FglJcrProductProperties.PROMO_MESSAGE, FglJcrProductProperties.PROMO_MESSAGE + productId);
        node.setProperty(FglJcrProductProperties.SHIP_TO_STORE, true);
        node.setProperty(FglJcrProductProperties.SPECIFICATION, FglJcrProductProperties.SPECIFICATION + productId);
        node.setProperty(FglJcrProductProperties.ECOMM_FULFILLER_ID, productId);
        node.setProperty(FglJcrProductProperties.STATUS, Product.Status.NEW.getStatusId());
        node.setProperty(FglJcrProductProperties.ECOMM_FULFILLER_NAME, FglJcrProductProperties.ECOMM_FULFILLER_NAME
                + productId);
        node.setProperty(FglJcrProductProperties.ECOMM_ASSEMBLY_REQUIRED, true);
        node.setProperty(FglJcrProductProperties.ECOMM_STICK_WARRANTY, true);
        node.setProperty(NameConstants.PN_PAGE_LAST_MOD_BY, "admin");
    }

    @Test
    public void shouldPopulateAllProductProperties() throws RepositoryException {
        Node productNode = createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        productNode.setProperty(FglJcrProductProperties.SELLABLE, true);
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.of("pmmId", TEST_EXT_ID);
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertProductProperiesPopulated(searchResult.getProducts().get(0));
    }

    @Test
    public void shouldFindProductsByCategory() throws RepositoryException {
        String extId = "42";
        Node categoryNode = rootNode.addNode("category-1", SLING_FOLDER_NODE_TYPE);
        createProductNode(categoryNode, extId, ANY_BRAND);
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.of("path", "/" + categoryNode.getName());
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertProductFound("Product was not found by category", searchResult.getProducts(), extId);
    }

    @Test
    public void shouldFindProductsByPmmId() throws RepositoryException {
        createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.of("pmmId", TEST_EXT_ID);
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertProductFound("Product was not found by PMM ID", searchResult.getProducts(), TEST_EXT_ID);
    }

    @Test
    public void shouldFindByPmmTitle() throws Exception {
        final Node productNode = createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        productNode.setProperty(FglJcrProductProperties.PRODUCT_NAME, "The Test Product Title");
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.of("pmmTitle", "Test Product");
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertProductFound("Product was not found by PMM ID", searchResult.getProducts(), TEST_EXT_ID);
    }

    @Test
    public void shouldFindProductsByFullTextSearchCase9() throws RepositoryException {
        Node productNode = createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        productNode.setProperty("prop", "Nike Jordan Varsity 2.0 Hoody Mens^");
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.of("fulltext",
                "Nike Jordan Varsity 2.0 Hoody Mens^");
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);
        assertProductFound("Product was not found by full-text search", searchResult.getProducts(), TEST_EXT_ID);
    }

    @Test
    public void shouldFindProductsByFullTextSearchCase10() throws RepositoryException {
        Node productNode = createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        productNode.setProperty("prop", "Nike Jordan Varsity 2.0 Hoody Mens &");
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.of("fulltext",
                "Nike Jordan Varsity 2.0 Hoody Mens &");
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);
        assertProductFound("Product was not found by full-text search", searchResult.getProducts(), TEST_EXT_ID);
    }

    @Test
    public void shouldFindProductsByFullTextSearchCase11() throws RepositoryException {
        Node productNode = createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        productNode.setProperty("prop", "Nike Jordan Varsity 2.0 { Hoody Mens }");
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.of("fulltext",
                "Nike Jordan Varsity 2.0 { Hoody Mens }");
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);
        assertProductFound("Product was not found by full-text search", searchResult.getProducts(), TEST_EXT_ID);
    }

    @Test
    public void shouldFindProductsByVendorName() throws RepositoryException {

        Node productNode = createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        productNode.setProperty(FglJcrProductProperties.ECOMM_FULFILLER_NAME, "The Supplier");
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.of(JcrProductsDAOImpl.FULFILLER_NAME_PREDICATE,
                "The Supplier");
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertProductFound("Product was not found by Fulfiller name", searchResult.getProducts(), TEST_EXT_ID);
    }

    @Test
    public void shouldFindProductsByAvailability() throws RepositoryException {

        Node productNode = createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        productNode.setProperty(FglJcrProductProperties.SELLABLE, true);
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.of("sellable", "true");
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertProductFound("Product was not found by availability", searchResult.getProducts(), TEST_EXT_ID);
    }

    @Test
    public void shouldFindProductsByProductStatus() throws RepositoryException {
        Node productNode = createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        productNode.setProperty(FglJcrProductProperties.STATUS, 3);
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.of("publishStatus", "3");
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertProductFound("Product was not found by publish status", searchResult.getProducts(), TEST_EXT_ID);
    }

    @Test
    public void shouldFindProductsByBrand() throws RepositoryException {
        createProductNode(rootNode, "818181", ANY_BRAND);
        createProductNode(rootNode, "828282", "Another Brand");
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.<String, String>builder()
                .put("3_group.property.1_value", ANY_BRAND).put("3_group.property.2_value", "Another Brand").build();
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertEquals("Products were not found by brands", 2, searchResult.getProducts().size());
    }

    @Test
    public void shouldFindProductsByTags() throws RepositoryException {
        Node productNode1 = createProductNode(rootNode, "818181", ANY_BRAND);
        productNode1.setProperty("cq:tags", new String[]{"sportchek:brands/adidas", "sportchek:custom/my-tag"});
        Node productNode2 = createProductNode(rootNode, "828282", "Another Brand");
        productNode2.setProperty("cq:tags", new String[]{"sportchek:brands/nike"});
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.<String, String>builder()
                .putAll("tags", "sportchek:custom/my-tag", "sportchek:brands/nike").build();
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertEquals("Products were not found by tags", 2, searchResult.getProducts().size());
    }

    @Test
    public void shouldFindProductsByCategoriesTags() throws RepositoryException {
        Node productNode1 = createProductNode(rootNode, "81818124", ANY_BRAND);
        productNode1.setProperty("cq:tags", new String[]{"sportchek:brands/adidas", SCLS_22_SHORTS_CATEGORY_TAG});
        Node productNode2 = createProductNode(rootNode, "8282823", "Some Brand");
        productNode2.setProperty("cq:tags", new String[]{"sportchek:brands/adidas", SDPT_03_BOYS});
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.<String, String>builder()
                .putAll("tags", SDPT_03_BOYS).build();
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertEquals("Products were not found by category tag", 2, searchResult.getProducts().size());
    }

    @Test
    public void shouldFindProductsByCreationDate() throws RepositoryException, ParseException {
        Node productNode = createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        productNode.setProperty(FglJcrProductProperties.CREATION_DATE, date("2014-05-05"));
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.<String, String>builder()
                .put("1_daterangecustom.lowerBound", "2014-05-01T00:00:00")
                .put("1_daterangecustom.upperBound", "2014-05-31T00:00:00").build();
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertProductFound("Product was not found by creation date", searchResult.getProducts(), TEST_EXT_ID);
    }

    @Test
    public void shouldFindProductsByModificationDate() throws RepositoryException, ParseException {

        Node productNode = createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        productNode.setProperty(FglJcrProductProperties.ECOMM_LAST_MODIFIED_DATE, date("2014-05-05"));
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.<String, String>builder()
                .put("0_daterange.lowerBound", "2014-05-01T00:00:00")
                .put("0_daterange.upperBound", "2014-05-31T00:00:00").build();
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertProductFound("Product was not found by modification date", searchResult.getProducts(), TEST_EXT_ID);
    }

    @Test
    public void shouldSortSearchResults() throws RepositoryException {

        createProductNode(rootNode, TEST_EXT_ID, "ZZZZZZZZZZZZ");
        session.save();

        SetMultimap<String, String> criteria = ImmutableSetMultimap.<String, String>builder()
                .put("p.sort", FglJcrProductProperties.ECOMM_BRAND).put("p.dir", "DESC").build();
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        List<Product> products = searchResult.getProducts();
        assertEquals("Incorrect order of products", TEST_EXT_ID, products.get(0).getExtId());
    }

    @Test
    public void shouldFindProductsByStatuses() throws RepositoryException {
        Node modifiedProductNode = createProductNode(rootNode, TEST_EXT_ID, ANY_BRAND);
        modifiedProductNode.setProperty(FglJcrProductProperties.STATUS, Product.Status.MODIFIED.getStatusId());
        Node pendingProductNode = createProductNode(rootNode, "8376920", "Other Brand");
        pendingProductNode.setProperty(FglJcrProductProperties.STATUS, Product.Status.PENDING.getStatusId());
        session.save();

        ProductSearchResult searchResult = jcrProductsDAO.findProductsByStatuses(Product.Status.PENDING, Product.Status.MODIFIED);

        assertEquals("Products were not found by statuses", 2, searchResult.getProducts().size());
    }

    @Test
    public void shouldPaginateSearchResults() {
        SetMultimap<String, String> criteria = ImmutableSetMultimap.<String, String>builder().put("p.offset", "5")
                .put("p.limit", "1").build();

        ProductSearchResult searchResult = jcrProductsDAO.findProducts(criteria);

        assertPaginationValid(searchResult, 1, GHOST_PRODUCTS_NUMBER);
    }

    @Test
    public void shouldLoadProductsWithinCategory() throws RepositoryException {
        String extId = "42";
        Node categoryNode = rootNode.addNode("category-1", SLING_FOLDER_NODE_TYPE);
        createProductNode(categoryNode, extId, ANY_BRAND);
        session.save();

        ProductsSelector selector = new ProductsSelector(categoryNode.getPath());
        selector.setLimit(100);
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(selector);

        assertProductFound("Category products are not loaded", searchResult.getProducts(), extId);
    }

    @Test
    public void shouldSortProductsWithinCategory() throws RepositoryException {

        createProductNode(rootNode, TEST_EXT_ID, "ZZZZZZZZZZZZ");
        session.save();

        ProductsSelector selector = new ProductsSelector(ROOT_NODE_PATH);
        selector.setLimit(100);
        selector.setSortAttribute(FglJcrProductProperties.ECOMM_BRAND);
        selector.setSortDirection("DESC");
        ProductSearchResult searchResult = jcrProductsDAO.findProducts(selector);

        List<Product> products = searchResult.getProducts();
        assertEquals("Products are not sorted", TEST_EXT_ID, products.get(0).getExtId());
    }

    @Test
    public void shouldApplyPaginationWithinCategory() {
        ProductsSelector selector = new ProductsSelector(ROOT_NODE_PATH);
        selector.setLimit(1);

        ProductSearchResult searchResult = jcrProductsDAO.findProducts(selector);

        assertPaginationValid(searchResult, selector.getLimit(), GHOST_PRODUCTS_NUMBER);
    }

    @Test
    public void shouldFindAllProductImageByProductCode() throws RepositoryException {
        JcrUtils.getOrCreateByPath(DAM_IMAGE_NODE_PATH, NT_UNSTRUCTURED, session);
        Iterator<Node> nodeIterator = jcrProductsDAO.getAllProductImageData("prd000001", session);
        while (nodeIterator.hasNext()) {
            assertEquals(DAM_IMAGE_NODE_PATH, nodeIterator.next().getPath());
        }
    }

    @Test
    public void shouldNotFindProductIncorrectProductCode() throws RepositoryException {
        Iterator<Node> nodeIterator = jcrProductsDAO.getAllProductImageData("prd01", session);
        assertFalse(nodeIterator.hasNext());
    }

    private static void assertPaginationValid(final ProductSearchResult searchResult, final long pageSize,
                                              final int totalResults) {

        assertEquals("Should respect the limit", pageSize, searchResult.getProducts().size());
        assertEquals("Should return total results value", totalResults, searchResult.getTotalProductsNumber());
    }

    private static Calendar date(final String dateString) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(dateString));
        return calendar;
    }

    private static void assertProductFound(final String message, final List<Product> products, final String extId) {
        assertEquals(message, 1, products.size());
        assertEquals(message, extId, products.get(0).getExtId());
    }

    private void assertProductProperiesPopulated(final Product product) {
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


}