package com.jcr.sling.junit.wrongmock;

import com.day.cq.tagging.Tag;

import java.util.Locale;

/**
 * The interface Images service.
 */
public interface BrandLogoService {

    /**
     * Return the brand logo.
     *
     * @param product - product code or product path in catalog
     * @param locale the locale
     * @return path to brand logo image
     */
    String getBrandLogo(String product, Locale locale);

    /**
     * Return the brand logo
     * @param tag - brand tag
     * @param locale - the locale
     * @return path to brand logo image
     */
    String getBrandLogo(Tag tag, Locale locale);
}
