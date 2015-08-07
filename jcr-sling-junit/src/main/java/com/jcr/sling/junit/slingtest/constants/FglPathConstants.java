package com.jcr.sling.junit.slingtest.constants;

import com.day.cq.commons.jcr.JcrConstants;

public final class FglPathConstants {

    public static final String HOME_PATH_SUFFIX = "/homepage";
    public static final String PATH_SEPARATOR = "/";
    public static final String SOURCE_JCR_PATH = "/etc/commerce/products/sportchek/source";
    public static final String MASTER_JCR_PATH = "/etc/commerce/products/sportchek/master";
    public static final String SPORTCHEK_PRODUCTS_JCR_PATH = "/etc/commerce/products/sportchek";
    public static final String BRAND_PATH_PREFIX = "/etc/tags/sportchek/brands";
    public static final String CATEGORIES_PATH_PREFIX = "/etc/tags/sportchek/categories";
    public static final String CONTENT_PATH_PREFIX = "/content/sportchek";
    public static final String TAGS_PREFIX = "/etc/tags/sportchek";
    public static final String SOURCE_SUB_NODE_NAME = "product";
    public static final String SOURCE_SUB_NODE_REL_PATH = JcrConstants.JCR_CONTENT + PATH_SEPARATOR
            + SOURCE_SUB_NODE_NAME;

    public static final String PDP_PREVIEW_PAGE_PATH = "sportchek/pages/product-preview-page";
    public static final String SCAFFOLDING_DIALOG_PATH = "/etc/scaffolding/sportchek/product";
    public static final String PRODUCT_RESOURCE_PATH = "commerce/components/product";

    public static final String STORES_JCR_PATH = "/etc/commerce/stores/sportchek";
    public static final String TWITTER_NODE_PATH = "/homeCheckAdviceArticle/hom_twitter";
    public static final String STORES_PAGES_PATH = "/content/sportchek/en/stores";

    public static final String CATEGORIES_TAG_ID = "categories";
    public static final String BRANDS_TAG_ID = "brands";
    public static final String PRICE_EVENT_TAG_ID = "priceEvent";
    public static final String PRICE_EVENT_TAG_PATH = "sportchek:priceEvent";

    private FglPathConstants() {
    }
}
