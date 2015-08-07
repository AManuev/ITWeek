package com.jcr.sling.junit.slingtest;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Set;

/**
 * The interface Brands DAO.
 */
public interface BrandsDAO {

    /**
     * Gets brand tags.
     *
     * @return the brand tags
     */
    Set<String> getSortedBrandTagsTitles();

    /**
     * Gets brands.
     *
     * @return the brands
     */
    List<Brand> getBrands() throws RepositoryException;

    /**
     * Gets all brands.
     *
     * @param filterArray the filter array
     * @param favouriteBrands the favourite brands
     * @return the filtered brands
     */
    List<Brand> getAllBrands(final String[] filterArray, final List<String> favouriteBrands);

    /**
     * Gets the active brands.
     *
     * @param filterArray the filter array
     * @param favouriteBrands the favourite brands
     * @return the favourite brands
     */
    List<Brand> getActiveBrands(final String[] filterArray, final List<String> favouriteBrands);
}
