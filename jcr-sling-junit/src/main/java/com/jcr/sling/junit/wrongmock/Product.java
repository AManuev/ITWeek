package com.jcr.sling.junit.wrongmock;

import com.day.cq.wcm.api.NameConstants;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.jcr.sling.junit.slingtest.query.add.FglJcrProductProperties.*;

public class Product extends AbstractCommerce {

    private static final String A_TAG = "a";
    private static final Whitelist WHITELIST = new Whitelist() {
        {
            addTags(A_TAG, "b", "sup", "sub", "u", "i");
            addAttributes(A_TAG, "href", "target");
        }
    };
    private static final String GLYPHS_TO_REMOVE = "·•";
    private static final String PATTERN = "\r?\n";

    private final String path;
    private final List<Sku> skus;
    private final Set<String> skuCodes;
    private final Map<String, String> swatches;
    private final Map<String, String> s7Swatches;
    private final List<String> productPageUrls;

    private Product(final Builder builder) {
        super(builder.resource);
        this.path = builder.resource.getPath();
        this.swatches = builder.swatches;
        this.s7Swatches = builder.swatchesS7;
        this.skus = builder.skus;
        this.skuCodes = builder.skuCodes;
        this.productPageUrls = builder.productPageUrls;
    }

    /**
     * Return localized title
     *
     * @param locale
     *            - current site locale
     * @return product title
     */
    public String getTitle(final Locale locale) {
        return getLocalizedProperty(locale, PRODUCT_TITLE);
    }

    /**
     * Return path to product
     *
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets sku codes.
     *
     * @return the sku codes
     */
    public Set<String> getSkuCodes() {
        return skuCodes;
    }

    /**
     * Gets the product page urls.
     *
     * @return the product page urls
     */
    public List<String> getProductPageUrls() {
        return productPageUrls;
    }

    /**
     * Check product for equivalent
     *
     * @param object
     *            - Other product
     * @return true or false
     */
    @Override
    public boolean equals(final Object object) {
        boolean equal = false;
        if (object instanceof Product) {
            Product another = (Product) object;
            equal = this.getPath().equals(another.getPath());
        }

        return equal;
    }

    /**
     * Hash code based on product path
     *
     * @return int
     */
    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * Product code
     *
     * @return code
     */
    public String getCode() {
        return properties.get(CODE, String.class);
    }

    /**
     * Product Ext id
     *
     * @return Ext Id
     */
    public String getExtId() {
        return properties.get(EXT_ID, String.class);
    }

    /**
     * Product features
     *
     * @param locale
     *            - current site locale
     * @return features
     */
    public String getFeatures(final Locale locale) {
        return getLocalizedProperty(locale, FEATURES);
    }

    /**
     * Product features
     *
     * @param locale
     *            - current site locale
     * @return bullet features
     */
    public List<String> getBulletFeatures(final Locale locale) {
        return createBulletList(getFeatures(locale));
    }

    /**
     * Product long description.
     *
     * @param locale
     *            - current site locale
     * @return string
     */
    public String getLongDescription(final Locale locale) {
        return escapeApostrophe(getLocalizedProperty(locale, LONG_DESCRIPTION));
    }

    /**
     * Product long description.
     *
     * @param locale
     *            - current site locale
     * @return bullet list of description
     */
    public List<String> getBulletLongDescription(final Locale locale) {
        return createBulletList(getLongDescription(locale));
    }

    /**
     * Product specifications
     *
     * @param locale
     *            - current site locale
     * @return specifications
     */
    public String getSpecifications(final Locale locale) {
        return getLocalizedProperty(locale, SPECIFICATION);
    }

    /**
     * Product specifications
     *
     * @param locale
     *            - current site locale
     * @return bullet specifications
     */
    public List<String> getBulletSpecifications(final Locale locale) {
        return createBulletList(getSpecifications(locale));
    }

    /**
     * Is product sellable
     *
     * @return true or false
     */
    public boolean isSellable() {
        return properties.get(SELLABLE, Boolean.class);
    }

    /**
     * Should PDP stay on site when online&offline inventory are 0
     *
     * @return true or false
     */
    public boolean isKeepWithOnSiteWithZeroInventory() {
        return PropertiesUtil.toBoolean(properties.get(KEEP_ON_SITE_WITH_ZERO_INVENTORY), false);
    }

    /**
     * Product brand
     *
     * @return brand
     */
    public String getBrand() {
        return properties.get(ECOMM_BRAND, String.class);
    }

    /**
     * Is product require assembly
     *
     * @return true or false
     */
    public Boolean isAssemblyRequired() {
        return properties.get(ECOMM_ASSEMBLY_REQUIRED, Boolean.class);
    }

    /**
     * Is product Stick Warranty
     *
     * @return true or false
     */
    public boolean isStickWarranty() {
        boolean result = false;
        if (properties.containsKey(ECOMM_STICK_WARRANTY)) {
            result = properties.get(ECOMM_STICK_WARRANTY, Boolean.class);
        }
        return result;
    }

    /**
     * Product name
     *
     * @return string
     */
    public String getName() {
        return properties.get(PRODUCT_NAME, String.class);
    }

    /**
     * Get Product variants list
     *
     * @return iterator
     */
    public List<Sku> getSkus() {
        return skus;
    }

    /**
     * Gets main section code.
     *
     * @return the main section code
     */
    public String getMainSectionCode() {
        return properties.get(MAIN_SECTION_CODE, String.class);
    }

    /**
     * Get Variant by Id or Optional.Absent
     *
     * @param code
     *            - Variant id
     * @return Optional for Variant
     */
    public Optional<Sku> getSku(final String code) {
        Optional<Sku> result = Optional.absent();
        for (Sku sku : skus) {
            if (sku.getCode().equalsIgnoreCase(code)) {
                result = Optional.of(sku);
                break;
            }
        }
        return result;
    }

    /**
     * Gets the swatches.
     *
     * @return the swatches
     */
    public Map<String, String> getSwatches() {
        return swatches;
    }

    /**
     * Gets the s7 swatches.
     *
     * @return the s7 swatches
     */
    public Map<String, String> getS7Swatches() {
        return s7Swatches;
    }

    /**
     * Gets the tags.
     *
     * @return the tags
     */
    public String[] getTags() {
        return properties.get(NameConstants.PN_TAGS, String[].class);
    }

    /**
     * Return array of images which disabled.
     *
     * @return array
     */
    public String[] getDisabledImages() {
        return properties.get(DISABLED_IMAGES, String[].class);
    }

    /**
     * Return product swatch image. Swatch image - a little image for Variant selector.
     *
     * @return array
     */
    public String[] getSwatchImages() {
        return properties.get(SWATCH_IMAGES, String[].class);
    }

    /**
     * Return product on time as Calendar.
     *
     * @return calendar
     */
    public Calendar getOnTime() {
        return properties.get(DATE_START, Calendar.class);
    }

    /**
     * Return product off time as Calendar.
     *
     * @return calendar.
     */
    public Calendar getOffTime() {
        return properties.get(DATE_END, Calendar.class);
    }

    /**
     * Return date of availability product.
     *
     * @return calendar
     */
    public Calendar getAvailabilityDate() {
        return properties.get(AVAILABILITY_DATE, Calendar.class);
    }

    /**
     * Gets the gender.
     *
     * @return the gender
     */
    public String[] getGender() {
        return ArrayUtils.nullToEmpty(properties.get(GENDER, String[].class));
    }

    /**
     * Gets the skill.
     *
     * @return the skill
     */
    public String getSkill() {
        return PropertiesUtil.toString(properties.get(SKILL, String.class), StringUtils.EMPTY);
    }

    /**
     * Gets the style.
     *
     * @return the style
     */
    public String getStyle() {
        return PropertiesUtil.toString(properties.get(STYLE, String.class), StringUtils.EMPTY);
    }

    /**
     * Gets the cut.
     *
     * @return the cut
     */
    public String getCut() {
        return PropertiesUtil.toString(properties.get(CUT, String.class), StringUtils.EMPTY);
    }

    /**
     * Gets the team.
     *
     * @return the team
     */
    public String getTeam() {
        return PropertiesUtil.toString(properties.get(TEAM, String.class), StringUtils.EMPTY);
    }

    /**
     * Gets the vendor.
     *
     * @return the vendor
     */
    public String getVendor() {
        return PropertiesUtil.toString(properties.get(ECOMM_VENDOR, String.class), StringUtils.EMPTY);
    }

    /**
     * Gets the prime case pack id.
     *
     * @return the PrimeCasePackId
     */
    public String getPrimeCasePackId() {
        return PropertiesUtil.toString(properties.get(PRIME_CASE_PACK_ID, String.class), StringUtils.EMPTY);
    }

    private List<String> createBulletList(final String field) {
        List<String> resultList = Lists.newArrayList();
        String fieldHtml = StringUtils.stripToEmpty(field);
        if (StringUtils.isNotEmpty(fieldHtml)) {
            fieldHtml = clearGlyphs(fieldHtml);
            resultList = clearHtml(fieldHtml);
        }
        return resultList;
    }

    private List<String> clearHtml(final String inHtml) {
        final Iterable<String> list = Splitter.onPattern(PATTERN).trimResults().omitEmptyStrings().split(inHtml);
        final List<String> result = Lists.newArrayList();
        for (String row : list) {
            String trimmed = Jsoup.clean(row, WHITELIST);
            if (!StringUtils.isWhitespace(trimmed)) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private String clearGlyphs(final String text) {
        return CharMatcher.anyOf(GLYPHS_TO_REMOVE).removeFrom(text).replace("&bull;", StringUtils.EMPTY);
    }

    private String escapeApostrophe(final String input) {
        return StringUtils.isBlank(input) ? input : input.replace("\'", "’");
    }

    /**
     * Builder.
     *
     * @return the product. builder
     */
    public static Product.Builder builder() {
        return new Builder();
    }

    /**
     * The Class Builder.
     */
    public static class Builder {

        private Resource resource;
        private final List<Sku> skus = Lists.newArrayList();
        private final Set<String> skuCodes = Sets.newHashSet();

        private List<String> productPageUrls;

        private Map<String, String> swatches;
        private Map<String, String> swatchesS7;

        /**
         * New product.
         *
         * @param resource
         *            the resource
         * @return the product. builder
         */
        public Product.Builder newProduct(final Resource resource) {
            this.resource = resource;
            this.swatches = this.swatchesS7 = Collections.emptyMap();
            aggregateSkuAndCodes(resource);

            return this;
        }

        /**
         * Returns SKU IDs
         *
         * @return SKU IDs set
         */
        public Set<String> getSkuCodes() {
            return skuCodes;
        }

        /**
         * Returns SKUs
         *
         * @return SKUs list
         */
        public List<Sku> getSkus() {
            return skus;
        }

        /**
         * Sets the product page urls.
         *
         * @param productPageUrls
         *            the product page urls
         * @return the product. builder
         */
        public Product.Builder setProductPageUrls(final List<String> productPageUrls) {
            this.productPageUrls = ImmutableList.copyOf(productPageUrls);
            return this;
        }

        /**
         * Sets the swatches.
         *
         * @param swatches
         *            the swatches
         * @return the product. builder
         */
        public Product.Builder setSwatches(final Map<String, String> swatches) {
            this.swatches = ImmutableMap.copyOf(swatches);
            return this;
        }

        /**
         * Sets the swatches s7.
         *
         * @param swatchesS7
         *            the swatches s7
         * @return the product. builder
         */
        public Product.Builder setSwatchesS7(final Map<String, String> swatchesS7) {
            this.swatchesS7 = ImmutableMap.copyOf(swatchesS7);
            return this;
        }

        /**
         * Builds the.
         *
         * @return the product
         */
        public Product build() {
            return new Product(this);
        }

        private void aggregateSkuAndCodes(final Resource resource) {
            final Iterator<Resource> resourceIterator = resource.listChildren();
            while (resourceIterator.hasNext()) {
                final Sku sku = new Sku(resourceIterator.next());
                skus.add(sku);
                skuCodes.add(sku.getCode());
            }
            Collections.sort(skus, new Comparator<Sku>() {
                @Override
                public int compare(final Sku left, final Sku right) {
                    return NumberUtils.toInt(left.getColor()) - NumberUtils.toInt(right.getColor());
                }
            });
        }
    }

}
