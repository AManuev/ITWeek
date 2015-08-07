package com.jcr.sling.junit.slingtest.query.add;

import java.util.List;

public final class ProductSearchResult {
    private final List<Product> products;
    private final long totalProductsNumber;

    /**
     * Instantiates a new product search result.
     *
     * @param products
     *            the products
     * @param totalProductsNumber
     *            the total products number
     */
    public ProductSearchResult(final List<Product> products, final long totalProductsNumber) {
        this.products = products;
        this.totalProductsNumber = totalProductsNumber;
    }

    /**
     * Gets the products.
     *
     * @return the products
     */
    public List<Product> getProducts() {
        return products;
    }

    /**
     * Gets the total products number.
     *
     * @return the total products number
     */
    public long getTotalProductsNumber() {
        return totalProductsNumber;
    }
}