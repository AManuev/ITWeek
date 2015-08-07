package com.jcr.sling.junit.wrongmock;

import com.google.common.collect.Lists;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static com.jcr.sling.junit.slingtest.query.add.FglJcrProductProperties.*;

public class Sku extends AbstractCommerce {

    private final List<Barcode> barcodes;

    /**
     * Instantiates a new sku.
     *
     * @param resource the resource
     *
     */
    Sku(final Resource resource) {
        super(resource);
        barcodes = collectBarCodes(resource);
    }

    /**
     * Gets the code.
     *
     * @return the code
     */
    public String getCode() {
        return properties.get(SKU_CODE, String.class);
    }

    /**
     * Gets PMM color.
     *
     * @return PMM color value
     */
    public String getPMMColor() {
        return properties.get(SKU_PMM_COLOR, DEFAULT_COLOR_IF_ABSENT);
    }

    /**
     * Gets the color. Values like 1 are converted to 01
     *
     * @return the color
     */
    public String getColor() {
        return getFormattedColor(properties);
    }

    /**
     * Gets the color. Values like 1 are converted to 01
     *
     * @return the color
     */
    public static String getColor(final ValueMap values) {
        return getFormattedColor(values);
    }

    private static String getFormattedColor(final ValueMap values) {
        String color = values.get(SKU_COLOR, String.class);
        return color != null && color.length() == 1 ? "0" + color : color;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public Double getSize() {
        return properties.get(SKU_SIZE, Double.class);
    }

    /**
     * Gets the color description.
     *
     * @param locale
     *            the locale
     * @return the color description
     */
    public String getColorDescription(final Locale locale) {
        return getLocalizedProperty(locale, SKU_COLOR_DESCRIPTION);
    }

    /**
     * Gets the size description.
     *
     * @param locale
     *            the locale
     * @return the size description
     */
    public String getSizeDescription(final Locale locale) {
        return getLocalizedProperty(locale, SKU_SIZE_DESCRIPTION);
    }

    /**
     * Gets the description.
     *
     * @param locale
     *            the locale
     * @return the description
     */
    public String getDescription(final Locale locale) {
        return getLocalizedProperty(locale, DESCRIPTION);
    }

    /**
     * Gets the ecommOnly flag
     * @return ecommOnly flag
     */
    public Boolean getEcommOnly() {
        Boolean ecommOnly = properties.get(SKU_ECOMM_ONLY, Boolean.class);
        return ecommOnly != null && ecommOnly;
    }

    /**
     * Gets bar codes
     * @return barcode object
     */
    public List<Barcode> getBarcodes() {
        return barcodes;
    }

    private List<Barcode> collectBarCodes(final Resource resource) {
        List<Barcode> result = Lists.newArrayList();
        Iterator<Resource> barcodeResources = resource.listChildren();

        while (barcodeResources.hasNext()) {
            result.add(barcodeResources.next().adaptTo(Barcode.class));
        }
        return result;
    }

    /**
     * Gets property
     * @param propertyName property name
     * @return property
     */
    public String getProperty(final String propertyName) {
        return properties.get(propertyName, String.class);
    }

}
