package com.jcr.sling.junit.slingtest.query.add;

import com.day.cq.search.Predicate;

public class ProductsSelector {
    private final String categoryPath;
    private long start;
    private long limit;
    private String sortAttribute = FglJcrProductProperties.EXT_ID;
    private String sortDirection = Predicate.SORT_ASCENDING;

    /**
     * Instantiates a new products selector.
     *
     * @param categoryPath the category path
     */
    public ProductsSelector(final String categoryPath) {
        this.categoryPath = categoryPath;
    }

    /**
     * Gets the category path.
     *
     * @return the category path
     */
    public String getCategoryPath() {
        return categoryPath;
    }

    /**
     * Gets the start.
     *
     * @return the start
     */
    public long getStart() {
        return start;
    }

    /**
     * Gets the limit.
     *
     * @return the limit
     */
    public long getLimit() {
        return limit;
    }

    /**
     * Gets the sort attribute.
     *
     * @return the sort attribute
     */
    public String getSortAttribute() {
        return sortAttribute;
    }

    /**
     * Gets the sort direction.
     *
     * @return the sort direction
     */
    public String getSortDirection() {
        return sortDirection;
    }

    /**
     * Sets the sort attribute.
     *
     * @param sortAttribute the new sort attribute
     */
    public void setSortAttribute(final String sortAttribute) {
        this.sortAttribute = sortAttribute;
    }

    /**
     * Sets the sort direction.
     *
     * @param sortDirection the new sort direction
     */
    public void setSortDirection(final String sortDirection) {
        this.sortDirection = sortDirection;
    }

    /**
     * Sets the limit.
     *
     * @param limit the new limit
     */
    public void setLimit(final long limit) {
        this.limit = limit;
    }

    /**
     * Sets the start.
     *
     * @param start the new start
     */
    public void setStart(final long start) {
        this.start = start;
    }
}
