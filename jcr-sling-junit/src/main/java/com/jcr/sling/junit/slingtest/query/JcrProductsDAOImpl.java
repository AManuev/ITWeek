package com.jcr.sling.junit.slingtest.query;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.jcr.sling.junit.slingtest.constants.FglPathConstants;
import com.jcr.sling.junit.slingtest.holders.SessionHolder;
import com.jcr.sling.junit.slingtest.query.add.FglJcrProductProperties;
import com.jcr.sling.junit.slingtest.query.add.Product;
import com.jcr.sling.junit.slingtest.query.add.ProductSearchResult;
import com.jcr.sling.junit.slingtest.query.add.ProductsSelector;
import com.jcr.sling.junit.slingtest.query.add.Sql2QueryBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@Service(ProductsDAO.class)
public class JcrProductsDAOImpl implements ProductsDAO {

    private static final Logger LOG = LoggerFactory.getLogger(JcrProductsDAOImpl.class);
    private static final String ANY_ENDING = "/%";
    private static final String PATH_PREDICATE = "path";
    private static final String P_OFFSET = "p.offset";
    private static final String P_LIMIT = "p.limit";
    private static final String MODIFIED_DATE_RANGE_LOWER_BOUND = "0_daterange.lowerBound";
    private static final String MODIFIED_DATE_RANGE_UPPER_BOUND = "0_daterange.upperBound";
    private static final String IDENTIFIER = "pmmId";
    private static final String PMM_TITLE_PREDICATE = "pmmTitle";
    private static final String FULL_TEXT = "fulltext";
    private static final String P_SORT = "p.sort";
    protected static final String FULFILLER_NAME_PREDICATE = "fulfillerName";
    private static final String P_DIR = "p.dir";
    private static final String CREATION_DATE_LOWER_BOUND_PREDICATE = "1_daterangecustom.lowerBound";
    private static final String CREATION_DATE_UPPER_BOUND_PREDICATE = "1_daterangecustom.upperBound";
    private static final String SELLABLE_PREDICATE = "sellable";
    private static final String STATUS_PREDICATE = "publishStatus";
    private static final String TAGS_PREDICATE = "tags";
    private static final Pattern VENDORS_PROPERTIES_PATTERN = Pattern.compile("2_group\\.property\\.\\d+_value");
    private static final Pattern BRANDS_PROPERTIES_PATTERN = Pattern.compile("3_group\\.property\\.\\d+_value");

    private static final String IMAGE_SEARCH_QUERY_BEGINNING = "select * from dam:Asset where jcr:path like '/content/dam/sportchek/%' and contains(*, '";
    private static final String IMAGE_SEARCH_QUERY_END = "') and (not contains(*, '_s') or not contains(*, '_S'))";
    private static final String SWATCH_IMAGE_SEARCH_QUERY_END = "') and (contains(*, '_s') or contains(*, '_S'))";
    private static final String ALL_IMAGE_SEARCH_QUERY = "select * from [dam:Asset] as asset where ISCHILDNODE('/content/dam/sportchek/product-images/%s') AND CONTAINS(asset.*, '%s') ";
    private static final String CATEGORY_TAG = "sportchek:categories";

    @Reference
    private SlingRepository repository;

    @Reference
    private UsersDao usersDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductSearchResult findProducts(final ProductsSelector selector) {
        String queryString = buildSql2SearchQuery(selector);
        long offset = selector.getStart();
        int limit = (int) selector.getLimit();

        return searchProducts(queryString, offset, limit);
    }

    /**
     * {@inheritDoc}
     *
     * @param searchCriteria
     */
    @Override
    public ProductSearchResult findProducts(final Multimap<String, String> searchCriteria) {
        String queryString = buildSql2SearchQuery(searchCriteria);
        int limit = Integer.valueOf(Iterables.getFirst(searchCriteria.get(P_LIMIT), "0"));
        long offset = Long.valueOf(Iterables.getFirst(searchCriteria.get(P_OFFSET), "0"));

        return searchProducts(queryString, offset, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductSearchResult findProductsByStatuses(final Product.Status... statuses) {
        Multimap<String, String> criteria = HashMultimap.create();
        for (Product.Status status : statuses) {
            criteria.put(STATUS_PREDICATE, String.valueOf(status.getStatusId()));
        }

        return findProducts(criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Node> getImageData(final String productCode, final Session session) throws RepositoryException {
        Iterator<Node> nodeIterator = Iterators.emptyIterator();
        final QueryManager queryManager = session.getWorkspace().getQueryManager();
        if (StringUtils.isNotBlank(productCode)) {
            javax.jcr.query.Query query = queryManager.createQuery(IMAGE_SEARCH_QUERY_BEGINNING + productCode
                    + IMAGE_SEARCH_QUERY_END, javax.jcr.query.Query.SQL);
            nodeIterator = query.execute().getNodes();
        }
        return nodeIterator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Node> getAllProductImageData(final String productCode, final Session session)
            throws RepositoryException {
        Iterator<Node> nodeIterator = Iterators.emptyIterator();
        if (StringUtils.isNotBlank(productCode)) {

            final String[] productCodeParts = FluentIterable.from(Splitter.fixedLength(3).split(productCode)).toArray(
                    String.class);
            final String pathToProductImage = Joiner.on(File.separator).join(productCodeParts);
            String queryString = String.format(ALL_IMAGE_SEARCH_QUERY, pathToProductImage, productCode);

            final QueryManager queryManager = session.getWorkspace().getQueryManager();
            final javax.jcr.query.Query query = queryManager.createQuery(queryString, Query.JCR_SQL2);

            nodeIterator = query.execute().getNodes();
        }
        return nodeIterator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Node> getSwatchImageData(final String productCode, final Session session)
            throws RepositoryException {
        Iterator<Node> nodeIterator = Iterators.emptyIterator();
        final QueryManager queryManager = session.getWorkspace().getQueryManager();
        if (StringUtils.isNotBlank(productCode)) {
            javax.jcr.query.Query query = queryManager.createQuery(IMAGE_SEARCH_QUERY_BEGINNING + productCode
                    + SWATCH_IMAGE_SEARCH_QUERY_END, javax.jcr.query.Query.SQL);
            nodeIterator = query.execute().getNodes();
        }
        return nodeIterator;
    }

    private ProductSearchResult searchProducts(final String queryString, final long offset, final int limit) {
        ProductSearchResult searchResult = new ProductSearchResult(Collections.<Product>emptyList(), 0);
        try (SessionHolder sessionHolder = new SessionHolder(repository)) {

            RowIterator rows = executeQuery(sessionHolder.getSession(), queryString, offset);

            searchResult = createSearchResultsPage(rows, offset, limit);
        } catch (RepositoryException e) {
            LOG.error("Error on access to node: ", e);
        }

        return searchResult;
    }

    private static RowIterator executeQuery(final Session session, final String queryString, final long offset)
            throws RepositoryException {

        LOG.trace("Searching Source Catalog: [{}]", queryString);

        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(queryString, Query.JCR_SQL2);
        query.setOffset(offset);

        QueryResult searchResult = query.execute();

        return searchResult.getRows();
    }

    private ProductSearchResult createSearchResultsPage(final RowIterator rows, final long offset,
                                                        final int limit) throws RepositoryException {

        // We do not use Query.setLimit(limit), because otherwise
        // RowIterator#getSize will return the limit value
        Iterator<Row> limited = limitToSize(rows, limit);

        List<Product> products = mapRowsToProducts(limited);
        long total = offset > 0 ? rows.getSize() + offset : rows.getSize();
        return new ProductSearchResult(products, total);
    }

    private static Iterator<Row> limitToSize(final RowIterator rows, final int limit) {
        @SuppressWarnings("unchecked")
        Iterator<Row> page = rows;
        if (limit > 0) {
            page = Iterators.limit(page, limit);
        }

        return page;
    }

    private List<Product> mapRowsToProducts(final Iterator<Row> iterator) throws RepositoryException {
        List<Product> result = Lists.newArrayList();
        while (iterator.hasNext()) {
            Node productNode = iterator.next().getNode("product");

            result.add(createProduct(productNode));
        }

        return result;
    }

    private Product createProduct(final Node productNode) throws RepositoryException {
        Node productPageNode = productNode.getParent().getParent();
        Product product = new Product();
        product.setPath(productPageNode.getPath());
        setProductProperties(productNode, product);

        return product;
    }

    private void setProductProperties(final Node productNode, final Product product) throws RepositoryException {
        product.setEcommCreationDate(JcrUtils.getDateProperty(productNode, FglJcrProductProperties.CREATION_DATE, null));
        product.setActivationDate(JcrUtils.getDateProperty(productNode, FglJcrProductProperties.ACTIVATION_DATE, null));
        product.setEcommAvailabilityDate(JcrUtils.getDateProperty(productNode,
                FglJcrProductProperties.AVAILABILITY_DATE, null));

        Calendar ecommLastModDate = JcrUtils.getDateProperty(productNode,
                FglJcrProductProperties.ECOMM_LAST_MODIFIED_DATE, null);
        product.setEcommLastModifiedDate(ecommLastModDate);
        product.setJcrLastModified(JcrUtils.getDateProperty(productNode.getParent(),
                NameConstants.PN_PAGE_LAST_MOD, ecommLastModDate));

        product.setLastImportedDate(JcrUtils.getDateProperty(productNode, FglJcrProductProperties.LAST_IMPORTED_DATE,
                null));
        product.setBrand(JcrUtils
                .getStringProperty(productNode, FglJcrProductProperties.ECOMM_BRAND, StringUtils.EMPTY));
        product.setExtId(JcrUtils.getStringProperty(productNode, FglJcrProductProperties.EXT_ID, StringUtils.EMPTY));
        product.setLongDescription(JcrUtils.getStringProperty(productNode, FglJcrProductProperties.LONG_DESCRIPTION,
                StringUtils.EMPTY));
        product.setPmmProductTitle(JcrUtils.getStringProperty(productNode, FglJcrProductProperties.PRODUCT_NAME,
                StringUtils.EMPTY));
        product.setProductTitle(JcrUtils.getStringProperty(productNode, FglJcrProductProperties.PRODUCT_TITLE,
                StringUtils.EMPTY));
        product.setPromoMessage(JcrUtils.getStringProperty(productNode, FglJcrProductProperties.PROMO_MESSAGE,
                StringUtils.EMPTY));
        product.setSpecification(JcrUtils.getStringProperty(productNode, FglJcrProductProperties.SPECIFICATION,
                StringUtils.EMPTY));
        product.setFulfillerName(JcrUtils.getStringProperty(productNode, FglJcrProductProperties.ECOMM_FULFILLER_NAME,
                StringUtils.EMPTY));
        product.setVendor(JcrUtils.getStringProperty(productNode, FglJcrProductProperties.ECOMM_VENDOR, StringUtils.EMPTY));

        product.setFeatures(JcrUtils
                .getStringProperty(productNode, FglJcrProductProperties.FEATURES, StringUtils.EMPTY));

        product.setSellable(JcrUtils.getBooleanProperty(productNode, FglJcrProductProperties.SELLABLE, false));
        product.setHybrisGiftWrappable(JcrUtils.getBooleanProperty(productNode,
                FglJcrProductProperties.HYBRIS_GIFT_WRAPPABLE, false));
        product.setProductComparable(JcrUtils.getBooleanProperty(productNode,
                FglJcrProductProperties.PRODUCT_COMPARABLE, false));
        product.setShipToStore(JcrUtils.getBooleanProperty(productNode, FglJcrProductProperties.SHIP_TO_STORE, false));
        product.setSellable(JcrUtils.getBooleanProperty(productNode, FglJcrProductProperties.SELLABLE, true));

        product.setProductStatus(productNode.getProperty(FglJcrProductProperties.PRODUCT_STATUS).getLong());
        product.setFulfillerId(productNode.getProperty(FglJcrProductProperties.ECOMM_FULFILLER_ID).getLong());
        product.setPublishStatus(productNode.getProperty(FglJcrProductProperties.STATUS).getLong());

        final Iterable<Property> properties = JcrUtils.getProperties(productNode, "images*");
        product.setImageAssociated(Iterables.size(properties) > 0);
        product.setAssemblyRequired(JcrUtils.getBooleanProperty(productNode,
                FglJcrProductProperties.ECOMM_ASSEMBLY_REQUIRED, false));
        product.setStickWarranty(JcrUtils.getBooleanProperty(productNode, FglJcrProductProperties.ECOMM_STICK_WARRANTY, false));
        final String lastModifiedBy = JcrUtils
                .getStringProperty(productNode.getParent(), NameConstants.PN_PAGE_LAST_MOD_BY, null);
        final String createdBy = JcrUtils
                .getStringProperty(productNode.getParent(), JcrConstants.JCR_CREATED_BY, StringUtils.EMPTY);
        //TODO UsersDAO obtains resourceResolver upon each request. This can be rewritten to one instance of resourceResolver for all search results.
        product.setLastModified(usersDao.getUserDisplayName(Objects.firstNonNull(lastModifiedBy, createdBy)));
    }

    private static String buildSql2SearchQuery(final Multimap<String, String> searchCriteria) {
        StringBuilder path = new StringBuilder(FglPathConstants.SOURCE_JCR_PATH);
        if (searchCriteria.containsKey(PATH_PREDICATE)) {
            path.append(Iterables.get(searchCriteria.get(PATH_PREDICATE), 0));
        }

        Sql2QueryBuilder builder = Sql2QueryBuilder.createProductSearchQuery(path.toString());

        builder.addFullTextConstraint(searchCriteria.get(FULL_TEXT));
        builder.addProductNameConstraint(searchCriteria.get(PMM_TITLE_PREDICATE));

        builder.addPropertyConstraint(FglJcrProductProperties.EXT_ID, searchCriteria.get(IDENTIFIER));
        builder.addPropertyConstraint(FglJcrProductProperties.ECOMM_FULFILLER_NAME,
                searchCriteria.get(FULFILLER_NAME_PREDICATE));
        builder.addPropertyConstraint(FglJcrProductProperties.SELLABLE, searchCriteria.get(SELLABLE_PREDICATE));
        builder.addPublishStatusConstraint(searchCriteria.get(STATUS_PREDICATE));
        builder.addPropertyConstraint(FglJcrProductProperties.ECOMM_BRAND, extractBrandFilters(searchCriteria));
        builder.addPropertyConstraint(FglJcrProductProperties.ECOMM_VENDOR, extractVendorsFilters(searchCriteria));
        addTagsCriteria(builder, searchCriteria.get(TAGS_PREDICATE));
        builder.addDateRangeLowerBound(FglJcrProductProperties.CREATION_DATE,
                searchCriteria.get(CREATION_DATE_LOWER_BOUND_PREDICATE));
        builder.addDateRangeUpperBound(FglJcrProductProperties.CREATION_DATE,
                searchCriteria.get(CREATION_DATE_UPPER_BOUND_PREDICATE));

        builder.addDateRangeLowerBound(FglJcrProductProperties.ECOMM_LAST_MODIFIED_DATE, searchCriteria.get(MODIFIED_DATE_RANGE_LOWER_BOUND));
        builder.addDateRangeUpperBound(FglJcrProductProperties.ECOMM_LAST_MODIFIED_DATE, searchCriteria.get(MODIFIED_DATE_RANGE_UPPER_BOUND));

        builder.addSortingClause(searchCriteria.get(P_SORT), searchCriteria.get(P_DIR));

        return builder.getQuery();
    }

    private static void addTagsCriteria(final Sql2QueryBuilder builder, final Collection<String> criterias) {
        if (criterias.isEmpty()) {
            return;
        }
        final Collection<String> strongConditions = Lists.newArrayList();
        final Collection<String> weakConditions = Lists.newArrayList();

        for (String criteria : criterias) {
            if (criteria.toLowerCase().startsWith(CATEGORY_TAG)) {
                weakConditions.add(criteria + ANY_ENDING);
                weakConditions.add(criteria);
            } else {
                strongConditions.add(criteria);
            }
        }

        builder.addPropertyConstraint(NameConstants.PN_TAGS, strongConditions);
        builder.addPropertyLike(NameConstants.PN_TAGS, weakConditions);
    }

    private static String buildSql2SearchQuery(final ProductsSelector productsSelector) {
        Sql2QueryBuilder builder = Sql2QueryBuilder.createCategoryProductsQuery(productsSelector.getCategoryPath());

        builder.addSortingClause(Collections.singleton(productsSelector.getSortAttribute()),
                Collections.singleton(productsSelector.getSortDirection()));

        return builder.getQuery();
    }

    private static Set<String> extractBrandFilters(final Multimap<String, String> searchCriteria) {
        Set<String> brands = Sets.newHashSet();
        for (Map.Entry<String, String> property : searchCriteria.entries()) {
            if (BRANDS_PROPERTIES_PATTERN.matcher(property.getKey()).matches()) {
                brands.add(property.getValue());
            }
        }
        return brands;
    }

    private static Set<String> extractVendorsFilters(final Multimap<String, String> searchCriteria) {
        Set<String> brands = Sets.newHashSet();
        for (Map.Entry<String, String> property : searchCriteria.entries()) {
            if (VENDORS_PROPERTIES_PATTERN.matcher(property.getKey()).matches()) {
                brands.add(property.getValue());
            }
        }
        return brands;
    }
}
