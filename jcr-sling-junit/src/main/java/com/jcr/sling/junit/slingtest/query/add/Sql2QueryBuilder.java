package com.jcr.sling.junit.slingtest.query.add;

import com.google.common.collect.Iterables;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Sql2QueryBuilder {

    private static final String OPERATOR_EQUAL = " = ";
    private static final String OPERATOR_LIKE = " LIKE ";

    private static final String BASE_PAGE_QUERY = "SELECT product.* FROM [cq:Page] as page"
            + " INNER JOIN [cq:PageContent] AS content ON ISCHILDNODE(content,page)"
            + " INNER JOIN [nt:unstructured] AS product ON ISCHILDNODE(product,content)"
            + " WHERE NAME(product) = 'product'";

    private static final String BASE_PRODUCT_QUERY = "SELECT product.* FROM [nt:unstructured] AS product WHERE NAME(product) = 'product'";
    private static final Map<Character, String> QUERY_STRING_CHARACTER_REPLACEMENT = new HashMap<Character, String>() {
        {
            put('"', "\"\\\"\"");
            put('-', "\"\\-\"");
            put('\\', "\"\\\\\"");
            put('#', "\"\\#\"");
            put('^', "\"\\^\"");
            put('(', "\"\\(\"");
            put(')', "\"\\)\"");
            put('{', "\"\\{\"");
            put('}', "\"\\}\"");
            put(']', "\"\\]\"");
            put('[', "\"\\[\"");
            put('&', "\"\\&\"");
            put('.', "\\.");
            put('?', "\"\\?\"");
            put('*', "\"\\*\"");
            put('\'', "\\'\'");
        }
    };

    private static final String SINGLE_QUOTE = "'";
    private static final String AND_PRODUCT = " AND product.";
    private static final String CLOSE_PART = "')";
    private static final String ESCAPED_SINGLE_QUOTE = "''";

    private final StringBuilder queryBuilder;

    private Sql2QueryBuilder(final String basePageQuery) {
        queryBuilder = new StringBuilder(basePageQuery);
    }

    /**
     * Creates the product search query.
     *
     * @param path the path
     * @return the sql2 query builder
     */
    public static Sql2QueryBuilder createProductSearchQuery(final String path) {
        Sql2QueryBuilder builder = new Sql2QueryBuilder(BASE_PRODUCT_QUERY);
        builder.queryBuilder.append(" AND ISDESCENDANTNODE(product, '").append(path).append(CLOSE_PART);

        return builder;
    }

    /**
     * Creates the category products query.
     *
     * @param categoryPath the category path
     * @return the sql2 query builder
     */
    public static Sql2QueryBuilder createCategoryProductsQuery(final String categoryPath) {
        Sql2QueryBuilder builder = new Sql2QueryBuilder(BASE_PAGE_QUERY);
        builder.queryBuilder.append(" AND ISCHILDNODE(page, '").append(categoryPath).append(CLOSE_PART);

        return builder;
    }

    /**
     * Adds the sorting clause.
     *
     * @param sortBy    the sort by
     * @param sortOrder the sort order
     */
    public void addSortingClause(final Collection<String> sortBy, final Collection<String> sortOrder) {
        if (sortBy.isEmpty()) {
            queryBuilder.append(" ORDER BY SCORE(product)");
        } else {
            queryBuilder.append(" ORDER BY product.").append(quote(Iterables.get(sortBy, 0)));
            if (!sortOrder.isEmpty()) {
                queryBuilder.append(" ").append(Iterables.get(sortOrder, 0));
            }
        }
    }

    /**
     * Adds the date range upper bound.
     *
     * @param property the property
     * @param values   the values
     */
    public void addDateRangeUpperBound(final String property, final Collection<String> values) {
        if (!values.isEmpty()) {
            String value = Iterables.get(values, 0);
            queryBuilder.append(AND_PRODUCT).append(quote(property)).append(" <= ").append(castAsDate(value));
        }
    }

    /**
     * Adds the date range lower bound.
     *
     * @param property the property
     * @param values   the values
     */
    public void addDateRangeLowerBound(final String property, final Collection<String> values) {
        if (!values.isEmpty()) {
            String value = Iterables.get(values, 0);
            queryBuilder.append(AND_PRODUCT).append(quote(property)).append(" >= ").append(castAsDate(value));
        }
    }

    /**
     * Adds the numeric property constraint.
     *
     * @param values the values
     */
    public void addPublishStatusConstraint(final Collection<String> values) {
        addRawPropertyConstraint(FglJcrProductProperties.STATUS, values);
    }

    /**
     * Adds the property constraint. Used " = " operator.
     *
     * @param property the property
     * @param values   the values
     */
    public void addPropertyConstraint(final String property, final Collection<String> values) {
        addRawPropertyConstraint(property, quoteValues(values));
    }

    /**
     * Adds the property with. Used " LIKE " operator.
     *
     * @param property the property
     * @param values   the values
     */
    public void addPropertyLike(final String property, final Collection<String> values) {
        addRawPropertyConstraint(property, quoteValues(values), OPERATOR_LIKE);
    }

    /**
     * Adds the full text constraint.
     *
     * @param values the values
     */
    public void addFullTextConstraint(final Collection<String> values) {
        addFullTextConstraintInternal("*", values);
    }

    /**
     * Adds the product name text constraint.
     *
     * @param values the values
     */
    public void addProductNameConstraint(final Collection<String> values) {
        addFullTextConstraintInternal(quote(FglJcrProductProperties.PRODUCT_NAME), values);
    }

    private void addFullTextConstraintInternal(final String propertyName, final Collection<String> values) {
        if (!values.isEmpty()) {
            String firstValue = escapeIllegalJcrChars(Iterables.get(values, 0));
            // 'or contains' case was added to suite query which start's or finishes with single
            // quote words like "men's" or "women's"
            queryBuilder.append(String.format(" AND (CONTAINS(product.%s, '*%s*') or (CONTAINS(product.%s, '%s')))",
                    propertyName, firstValue, propertyName, firstValue));
        }
    }

    /**
     * Gets the query.
     *
     * @return the query
     */
    public String getQuery() {
        return queryBuilder.toString();
    }

    private void addRawPropertyConstraint(final String property, final Collection<String> values, final String operator) {
        if (!values.isEmpty()) {
            queryBuilder.append(" AND (");
            for (Iterator<String> iterator = values.iterator(); iterator.hasNext(); ) {
                String value = iterator.next();

                queryBuilder.append("product.").append(quote(property)).append(operator).append(value);
                if (iterator.hasNext()) {
                    queryBuilder.append(" OR ");
                }
            }
            queryBuilder.append(")");
        }
    }

    private void addRawPropertyConstraint(final String property, final Collection<String> values) {
        addRawPropertyConstraint(property, values, OPERATOR_EQUAL);
    }

    private static Collection<String> quoteValues(final Collection<String> values) {
        Collection<String> quoted = new ArrayList<>(values.size());
        for (String value : values) {
            quoted.add(quote(value));
        }

        return quoted;
    }

    private static String castAsDate(final String dateString) {
        return "CAST('" + toIsoDateTimeFormat(dateString) + "' AS DATE)";
    }

    /**
     * In search value single quote should be escaped with additional single quote or expression will be broken.
     */
    private static String quote(final String input) {
        return SINGLE_QUOTE + input.replace(SINGLE_QUOTE, ESCAPED_SINGLE_QUOTE) + SINGLE_QUOTE;
    }

    private static String toIsoDateTimeFormat(final String date) {
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").parseDateTime(date);
        return ISODateTimeFormat.dateTime().print(dateTime);
    }

    /**
     * Within the search literal instances of double quote (“"”) and hyphen
     * (“-”) must be escaped with a backslash (“\”) Backslash itself must therefore also be escaped,
     * ending up as double backslash (“\\”).
     */
    private static String escapeIllegalJcrChars(final String input) {
        StringBuilder builder = new StringBuilder(input.length() * 2);
        for (int i = 0; i < input.length(); ++i) {
            Character character = input.charAt(i);
            if (QUERY_STRING_CHARACTER_REPLACEMENT.containsKey(character)) {
                builder.append(QUERY_STRING_CHARACTER_REPLACEMENT.get(character));
            } else {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    /**
     * Escape single quote
     *
     * @param input original String
     * @return escaped input
     */
    public static String escapeSingleQuote(final String input) {
        return input.replace("'", "''");
    }

}
