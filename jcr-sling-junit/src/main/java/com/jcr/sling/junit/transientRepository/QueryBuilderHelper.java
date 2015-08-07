package com.jcr.sling.junit.transientRepository;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.sling.jcr.resource.JcrResourceUtil;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class QueryBuilderHelper {

    public static final Object IS_NULL = new Object();
    public static final Object IS_NOT_NULL = new Object();

    private static final String DISJUNCTION_TOKEN = RandomStringUtils.randomAlphabetic(16);

    private QueryBuilderHelper() {
    }

    /**
     * Builds the filter constraint.
     *
     * @param session the session
     * @param selector the selector
     * @param filter the filter
     * @return the constraint
     * @throws RepositoryException the repository exception
     */
    public static Constraint buildFilterConstraint(final Session session, final String selector,
                                                   final Map<String, Object> filter) throws RepositoryException {
        final ImmutableMultimap.Builder<String, Object> builder = ImmutableMultimap.builder();
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            builder.put(entry);
        }
        return buildFilterConstraint(session, selector, builder.build());
    }

    /**
     * Builds the filter constraint.
     *
     * @param session the session
     * @param selector the selector
     * @param filter the filter
     * @return the constraint
     * @throws RepositoryException the repository exception
     */
    public static Constraint buildFilterConstraint(final Session session, final String selector,
                                                   final Multimap<String, Object> filter) throws RepositoryException {
        final Collection<Constraint> constraints = Lists.newArrayListWithCapacity(filter.keySet().size());
        for (Map.Entry<String, Collection<Object>> filterEntry : filter.asMap().entrySet()) {
            constraints.add(buildCurrentConstraint(session, selector, filterEntry));
        }

        return allOf(session, constraints);
    }

    private static Constraint buildCurrentConstraint(final Session session, final String selector,
                                                     final Map.Entry<String, Collection<Object>> filterEntry) throws RepositoryException {
        final QueryObjectModelFactory qomf = session.getWorkspace().getQueryManager().getQOMFactory();
        final String property = filterEntry.getKey();

        final Collection<Constraint> valueConstraints = Lists.newArrayListWithCapacity(filterEntry.getValue().size());
        for (Object rawValue : filterEntry.getValue()) {
            final Object value = getValue(rawValue);
            final Constraint currentValueConstraint;

            if (value == QueryBuilderHelper.IS_NOT_NULL) {
                currentValueConstraint = qomf.propertyExistence(selector, property);
            } else if (value == QueryBuilderHelper.IS_NULL) {
                // because of a bug in query builder, negation constrain adds parentheses only
                // to
                // AND
                // or OR constrains, but NOT [s].[property] IS NOT NULL without parentheses
                // doesn't
                // work. So we wrap same IS NOT NULL constraint into OR and negate it
                currentValueConstraint = qomf.not(qomf.or(qomf.propertyExistence(selector, property),
                        qomf.propertyExistence(selector, property)));
            } else {
                Value jcrValue = JcrResourceUtil.createValue(value, session);
                if (jcrValue == null) {
                    jcrValue = JcrResourceUtil.createValue(DISJUNCTION_TOKEN, session);
                }
                currentValueConstraint = qomf.comparison(qomf.propertyValue(selector, property),
                        QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, qomf.literal(jcrValue));
            }

            valueConstraints.add(currentValueConstraint);
        }

        return anyOf(session, valueConstraints);
    }

    /**
     * All of.
     *
     * @param session the session
     * @param constraints the constraints
     * @return the constraint
     * @throws RepositoryException the repository exception
     */
    public static Constraint allOf(final Session session, final Collection<Constraint> constraints)
            throws RepositoryException {
        final QueryObjectModelFactory qomf = session.getWorkspace().getQueryManager().getQOMFactory();
        final Iterator<Constraint> iterator = constraints.iterator();
        Constraint constraint = null;
        if (iterator.hasNext()) {
            constraint = iterator.next();
            for (; iterator.hasNext();) {
                constraint = qomf.and(constraint, iterator.next());
            }
        }
        return constraint;
    }

    /**
     * Any of.
     *
     * @param session the session
     * @param constraints the constraints
     * @return the constraint
     * @throws RepositoryException the repository exception
     */
    public static Constraint anyOf(final Session session, final Collection<Constraint> constraints)
            throws RepositoryException {
        final QueryObjectModelFactory qomf = session.getWorkspace().getQueryManager().getQOMFactory();
        final Iterator<Constraint> iterator = constraints.iterator();
        Constraint constraint = null;
        if (iterator.hasNext()) {
            constraint = iterator.next();
            while (iterator.hasNext()) {
                constraint = qomf.or(constraint, iterator.next());
            }
        }
        return constraint;
    }

    /**
     * Disjunction.
     *
     * @param session the session
     * @param selector the selector
     * @return the constraint
     * @throws RepositoryException the repository exception
     */
    public static Constraint disjunction(final Session session, final String selector) throws RepositoryException {
        final QueryObjectModelFactory qomf = session.getWorkspace().getQueryManager().getQOMFactory();
        final Value jcrValue = JcrResourceUtil.createValue(DISJUNCTION_TOKEN, session);
        return qomf.comparison(qomf.propertyValue(selector, DISJUNCTION_TOKEN),
                QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, qomf.literal(jcrValue));
    }

    private static Object getValue(final Object value) {
        Object result = value;
        if (value.getClass().isEnum()) {
            result = value.toString();
        }
        return result;
    }

}
