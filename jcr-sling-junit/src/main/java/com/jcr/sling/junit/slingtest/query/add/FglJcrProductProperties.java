package com.jcr.sling.junit.slingtest.query.add;

import com.day.cq.tagging.TagConstants;

public final class FglJcrProductProperties {

    public static final String PRODUCT_TITLE = "productTitle";
    public static final String STATUS = "publishStatus";
    public static final String ACTIVATION_DATE = "activationDate";
    public static final String FEATURES = "features";
    public static final String LAST_IMPORTED_DATE = "lastImportedDate";
    public static final String LONG_DESCRIPTION = "longDescription";
    public static final String SPECIFICATION = "specification";
    public static final String TAGS = TagConstants.PN_TAGS;
    public static final String GENDER = "gender";
    public static final String SKILL = "skill";
    public static final String STYLE = "style";
    public static final String CUT = "cut";
    public static final String TEAM = "team";
    public static final String DEFAULT_COLOR_IF_ABSENT = "99";

    public static final String PROMO_MESSAGE = "promoMessage";
    public static final String SELLABLE = "sellable";
    public static final String KEEP_ON_SITE_WITH_ZERO_INVENTORY = "keepOnSiteWithZeroInventory";
    public static final String DATE_START = "dateStart";
    public static final String DATE_END = "dateEnd";
    public static final String MAIN_SECTION_CODE = "mainSectionCode";

    public static final String IMAGES = "images";
    public static final String DISABLED_IMAGES = "disabledImages";
    public static final String SWATCH_IMAGES = "swatchImages";
    public static final String SWATCH_IMAGE = "swatchImage";

    public static final String PRICE = "price";

    public static final String LAST_MODIFIED = "lastModified";

    // Hybris properties
    public static final String HYBRIS_NAME_SPACE = "ecomm:";
    public static final String ECOMM_VENDOR = "ecomm:Vendor";
    public static final String PRIME_CASE_PACK_ID = "ecomm:PrimeCasePackId";
    public static final String HYBRIS_GIFT_WRAPPABLE = "ecomm:GiftWrappable";
    public static final String EXT_ID = "ecomm:ExtId";
    public static final String AVAILABILITY_DATE = "ecomm:AvailabilityDate";
    public static final String PRODUCT_NAME = "ecomm:Name";
    public static final String PRODUCT_COMPARABLE = "ecomm:ProductComparable";
    public static final String PRODUCT_STATUS = "ecomm:ProductStatus";
    public static final String SHIP_TO_STORE = "ecomm:ShipToStore";
    public static final String ECOMM_FULFILLER_ID = "ecomm:FulfillerId";
    public static final String ECOMM_FULFILLER_NAME = "ecomm:FulfillerName";
    public static final String CODE = "ecomm:Code";
    public static final String ECOMM_BRAND = "ecomm:ProductBrand";
    public static final String CREATION_DATE = "ecomm:CreationDate";
    public static final String ECOMM_LAST_MODIFIED_DATE = "ecomm:LastModifiedDate";
    public static final String ECOMM_ASSEMBLY_REQUIRED = "ecomm:AssemblyRequired";
    public static final String ECOMM_PMM_VENDOR_COLOUR_DESCRIPTION = "ecomm:pmmVendorColourDescription";
    public static final String ECOMM_STICK_WARRANTY = "ecomm:StickWarranty";

    public static final String PRODUCT_MASTER = "cq:productMaster";
    public static final String STORE_NODE = "cq:store";
    public static final String DESCRIPTION = "Description";

    // SKU properties
    public static final String SKU_EXT_ID = EXT_ID;
    public static final String SKU_CODE = CODE;
    public static final String SKU_ATTRIBUTE_NAME_SUFFIX = "Name";
    public static final String SKU_SIZE = "ecomm:size";
    public static final String SKU_COLOR = "ecomm:color";
    public static final String SKU_PMM_COLOR = "ecomm:pmmcolor";
    public static final String SKU_HAND = "ecomm:hand";
    public static final String SKU_SIZE_DESCRIPTION = "ecomm:sizeDescription";
    public static final String SKU_COLOR_DESCRIPTION = "ecomm:colorDescription";
    public static final String SKU_BASE_PRODUCT = "ecomm:baseProduct";
    public static final String SKU_ECOMM_ONLY = "ecomm:ecommOnly";

    // Category properties
    public static final String CATEGORY_CODE = "code";
    public static final String CATEGORY_TITLE = "jcr:title";

    //BARCODE properties
    public static final String BARCODE_TEXT = "text";
    public static final String BARCODE_TYPE = "type";
    public static final String BARCODE_ACTIVE = "active";
    public static final String BARCODE_DEFAULT = "default";
    public static final String BARCODE_PRIMARY = "primary";

    public static final int PRODUCT_TITLE_MAX_LENGTH = 255;

    private FglJcrProductProperties() {
    }
}
