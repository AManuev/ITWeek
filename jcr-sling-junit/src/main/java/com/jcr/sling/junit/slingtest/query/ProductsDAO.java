package com.jcr.sling.junit.slingtest.query;

import com.google.common.collect.Multimap;
import com.jcr.sling.junit.slingtest.query.add.Product;
import com.jcr.sling.junit.slingtest.query.add.ProductSearchResult;
import com.jcr.sling.junit.slingtest.query.add.ProductsSelector;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Iterator;

/**
 * Search product interface.
 */
public interface ProductsDAO {

    /**
     * @param selector
     *            {@see com.fglsports.wcm.dto.ProductsSelector.class}
     * @return search result as ProductSearchResult.class
     */
    ProductSearchResult findProducts(ProductsSelector selector);

    /**
     * Search product image data.
     *
     * @param productCode
     *            - product code
     * @param session
     *            - current JCR session.
     * @return iterator with nodes.
     *
     * @throws RepositoryException
     */
    Iterator<Node> getImageData(String productCode, Session session) throws RepositoryException;

    /**
     * Search swatch image product data.
     *
     * @param productCode
     *            - product code.
     * @param session
     *            - current JCR session.
     * @return iterator with nodes.
     *
     * @throws RepositoryException
     */
    Iterator<Node> getSwatchImageData(String productCode, Session session) throws RepositoryException;

    /**
     * Searches products by specified criteria.
     *
     * @param searchCriteria
     *            map of values.
     * @return search result as {@link ProductSearchResult}
     */
    ProductSearchResult findProducts(Multimap<String, String> searchCriteria);

    /**
     * Looks up products by their status
     *
     * @param statuses
     *            statuses to search for
     * @return search result as {@link ProductSearchResult}
     */
    ProductSearchResult findProductsByStatuses(Product.Status... statuses);

    /**
     * Search all product's images node.
     *
     * @param productCode
     *            - product code
     * @param session
     *            - current JCR session.
     * @return iterator with nodes.
     *
     * @throws RepositoryException
     */
    Iterator<Node> getAllProductImageData(String productCode, Session session) throws RepositoryException;
}
