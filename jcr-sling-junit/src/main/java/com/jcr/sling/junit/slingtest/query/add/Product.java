package com.jcr.sling.junit.slingtest.query.add;

import com.day.cq.commons.jcr.JcrConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Product {
    /**
     * The Enum Status.
     */
    public enum Status {

        /** The new. */
        NEW(1),
        /** The published. */
        PUBLISHED(2),
        /** The failed. */
        FAILED(3),
        /** The modified. */
        MODIFIED(4),
        /** The pending. */
        PENDING(5);

        private final long statusId;

        Status(final long statusId) {
            this.statusId = statusId;
        }

        /**
         * Gets the status id.
         *
         * @return the status id
         */
        public long getStatusId() {
            return statusId;
        }

        /**
         * get Status By status ID.
         *
         * @param statusId the status id
         * @return the status
         */
        public static Status byId(final long statusId) {
            Status result = null;
            for (Status value : values()) {
                if (value.statusId == statusId) {
                    result = value;
                    break;
                }
            }
            if (result == null) {
                throw new IllegalArgumentException();
            }
            return result;
        }
    }

    private String path;

    private Calendar activationDate;
    private Calendar ecommAvailabilityDate;
    private String ecommBrand;
    private Calendar ecommCreationDate;
    private String extId;
    private String features;
    private boolean ecommGiftWrappable;
    private Calendar lastImportedDate;
    private String longDescription;
    private String ecommProductFullName;
    private boolean ecommProductComparable;
    private long ecommProductStatus;
    private String productTitle;
    private String promoMessage;
    private boolean shipToStore;
    private String specification;
    private long fullfillerId;
    private String fulfillerName;
    private String vendor;
    private Calendar jcrLastModified;
    private Calendar ecommLastModifiedDate;
    private Status publishStatus;
    private boolean sellable;
    private boolean assemblyRequired;
    private boolean stickWarranty;
    private boolean isImageAssociated;
    private String price;
    private String lastModified;

    private String dateToString(final Calendar calendar) {
        DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        return calendar == null ? StringUtils.EMPTY : dateFormatter.format(calendar.getTime());
    }

    /**
     * Product to json.
     *
     * @return the JSON object
     * @throws JSONException the JSON exception
     */
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        // TODO move formatter

        json.put(JcrConstants.JCR_PATH, getPath());

        json.put(FglJcrProductProperties.ACTIVATION_DATE, dateToString(getActivationDate()));
        json.put(FglJcrProductProperties.AVAILABILITY_DATE, dateToString(getEcommAvailabilityDate()));
        json.put(FglJcrProductProperties.CREATION_DATE, dateToString(getEcommCreationDate()));
        json.put(FglJcrProductProperties.ECOMM_LAST_MODIFIED_DATE, dateToString(getEcommLastModifiedDate()));
        json.put(JcrConstants.JCR_LASTMODIFIED, dateToString(getJcrLastModified()));

        json.put(FglJcrProductProperties.LAST_IMPORTED_DATE, dateToString(getLastImportedDate()));

        json.put(FglJcrProductProperties.ECOMM_BRAND, getEcommBrand());
        json.put(FglJcrProductProperties.EXT_ID, getExtId());
        json.put(FglJcrProductProperties.FEATURES, Jsoup.clean(getFeatures(), Whitelist.none()));
        json.put(FglJcrProductProperties.HYBRIS_GIFT_WRAPPABLE, isHybrisGiftWrappable());

        json.put(FglJcrProductProperties.LONG_DESCRIPTION, Jsoup.clean(getLongDescription(), Whitelist.none()));
        json.put(FglJcrProductProperties.PRODUCT_NAME, getPmmProductTitle());
        json.put(FglJcrProductProperties.PRODUCT_COMPARABLE, isProductComparable());
        json.put(FglJcrProductProperties.PRODUCT_STATUS, getProductStatus());
        json.put(FglJcrProductProperties.PRODUCT_TITLE, getProductTitle());
        json.put(FglJcrProductProperties.PROMO_MESSAGE, getPromoMessage());
        json.put(FglJcrProductProperties.SHIP_TO_STORE, isShipToStore());
        json.put(FglJcrProductProperties.SPECIFICATION, Jsoup.clean(getSpecification(), Whitelist.none()));
        json.put(FglJcrProductProperties.STATUS, getPublishStatus());

        json.put(FglJcrProductProperties.ECOMM_FULFILLER_ID, getFulfillerId());
        json.put(FglJcrProductProperties.ECOMM_FULFILLER_NAME, getFulfillerName());
        json.put(FglJcrProductProperties.ECOMM_VENDOR, getVendor());

        json.put(FglJcrProductProperties.SELLABLE, isSellable());
        json.put(FglJcrProductProperties.ECOMM_ASSEMBLY_REQUIRED, isAssemblyRequired());
        json.put(FglJcrProductProperties.ECOMM_STICK_WARRANTY, isStickWarranty());
        json.put(FglJcrProductProperties.IMAGES + "Associated", isImageAssociated());
        json.put(FglJcrProductProperties.PRICE, getPrice());
        json.put(FglJcrProductProperties.LAST_MODIFIED, dateToString(getJcrLastModified()) +"("+ getLastModified()+")");

        return json;
    }

    /**
     * Gets the activation date.
     *
     * @return the activation date
     */
    public Calendar getActivationDate() {
        return activationDate;
    }

    /**
     * Sets the activation date.
     *
     * @param activationDate the new activation date
     */
    public void setActivationDate(final Calendar activationDate) {
        this.activationDate = activationDate;
    }

    /**
     * Gets the ecomm availability date.
     *
     * @return the ecomm availability date
     */
    public Calendar getEcommAvailabilityDate() {
        return ecommAvailabilityDate;
    }

    /**
     * Sets the ecomm availability date.
     *
     * @param ecommAvailabilityDate the new ecomm availability date
     */
    public void setEcommAvailabilityDate(final Calendar ecommAvailabilityDate) {
        this.ecommAvailabilityDate = ecommAvailabilityDate;
    }

    /**
     * Gets the ecomm brand.
     *
     * @return the ecomm brand
     */
    public String getEcommBrand() {
        return ecommBrand;
    }

    /**
     * Sets the brand.
     *
     * @param ecommBrand the new brand
     */
    public void setBrand(final String ecommBrand) {
        this.ecommBrand = ecommBrand;
    }

    /**
     * Gets the ecomm creation date.
     *
     * @return the ecomm creation date
     */
    public Calendar getEcommCreationDate() {
        return ecommCreationDate;
    }

    /**
     * Sets the ecomm creation date.
     *
     * @param creationDate the new ecomm creation date
     */
    public void setEcommCreationDate(final Calendar creationDate) {
        this.ecommCreationDate = creationDate;
    }

    /**
     * Gets the ext id.
     *
     * @return the ext id
     */
    public String getExtId() {
        return extId;
    }

    /**
     * Sets the ext id.
     *
     * @param extId the new ext id
     */
    public void setExtId(final String extId) {
        this.extId = extId;
    }

    /**
     * Gets the features.
     *
     * @return the features
     */
    public String getFeatures() {
        return features;
    }

    /**
     * Sets the features.
     *
     * @param features the new features
     */
    public void setFeatures(final String features) {
        this.features = features;
    }

    /**
     * Checks if is hybris gift wrappable.
     *
     * @return true, if is hybris gift wrappable
     */
    public boolean isHybrisGiftWrappable() {
        return ecommGiftWrappable;
    }

    /**
     * Sets the hybris gift wrappable.
     *
     * @param ecommGiftWrappable the new hybris gift wrappable
     */
    public void setHybrisGiftWrappable(final boolean ecommGiftWrappable) {
        this.ecommGiftWrappable = ecommGiftWrappable;
    }

    /**
     * Gets the last imported date.
     *
     * @return the last imported date
     */
    public Calendar getLastImportedDate() {
        return lastImportedDate;
    }

    /**
     * Sets the last imported date.
     *
     * @param lastImportedDate the new last imported date
     */
    public void setLastImportedDate(final Calendar lastImportedDate) {
        this.lastImportedDate = lastImportedDate;
    }

    /**
     * Gets the long description.
     *
     * @return the long description
     */
    public String getLongDescription() {
        return longDescription;
    }

    /**
     * Sets the long description.
     *
     * @param longDescription the new long description
     */
    public void setLongDescription(final String longDescription) {
        this.longDescription = longDescription;
    }

    /**
     * Gets the pmm product title.
     *
     * @return the pmm product title
     */
    public String getPmmProductTitle() {
        return ecommProductFullName;
    }

    /**
     * Sets the pmm product title.
     *
     * @param ecommProductFullName the new pmm product title
     */
    public void setPmmProductTitle(final String ecommProductFullName) {
        this.ecommProductFullName = ecommProductFullName;
    }

    /**
     * Checks if is product comparable.
     *
     * @return true, if is product comparable
     */
    public boolean isProductComparable() {
        return ecommProductComparable;
    }

    /**
     * Sets the product comparable.
     *
     * @param ecommProductComparable the new product comparable
     */
    public void setProductComparable(final boolean ecommProductComparable) {
        this.ecommProductComparable = ecommProductComparable;
    }

    /**
     * Gets the product status.
     *
     * @return the product status
     */
    public long getProductStatus() {
        return ecommProductStatus;
    }

    /**
     * Sets the product status.
     *
     * @param ecommProductStatus the new product status
     */
    public void setProductStatus(final long ecommProductStatus) {
        this.ecommProductStatus = ecommProductStatus;
    }

    /**
     * Gets the product title.
     *
     * @return the product title
     */
    public String getProductTitle() {
        return productTitle;
    }

    /**
     * Sets the product title.
     *
     * @param productTitle the new product title
     */
    public void setProductTitle(final String productTitle) {
        this.productTitle = productTitle;
    }

    /**
     * Gets the promo message.
     *
     * @return the promo message
     */
    public String getPromoMessage() {
        return promoMessage;
    }

    /**
     * Sets the promo message.
     *
     * @param promoMessage the new promo message
     */
    public void setPromoMessage(final String promoMessage) {
        this.promoMessage = promoMessage;
    }

    /**
     * Checks if is ship to store.
     *
     * @return true, if is ship to store
     */
    public boolean isShipToStore() {
        return shipToStore;
    }

    /**
     * Sets the ship to store.
     *
     * @param shipToStore the new ship to store
     */
    public void setShipToStore(final boolean shipToStore) {
        this.shipToStore = shipToStore;
    }

    /**
     * Gets the specification.
     *
     * @return the specification
     */
    public String getSpecification() {
        return specification;
    }

    /**
     * Sets the specification.
     *
     * @param specification the new specification
     */
    public void setSpecification(final String specification) {
        this.specification = specification;
    }

    /**
     * Gets the fulfiller id.
     *
     * @return the fulfiller id
     */
    public long getFulfillerId() {
        return fullfillerId;
    }

    /**
     * Sets the fulfiller id.
     *
     * @param fulfillerId the new fulfiller id
     */
    public void setFulfillerId(final long fulfillerId) {
        this.fullfillerId = fulfillerId;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path.
     *
     * @param path the new path
     */
    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * Gets the ecomm last modified date.
     *
     * @return the ecomm last modified date
     */
    public Calendar getEcommLastModifiedDate() {
        return ecommLastModifiedDate;
    }

    /**
     * Sets the ecomm last modified date.
     *
     * @param ecommLastModifiedDate the new ecomm last modified date
     */
    public void setEcommLastModifiedDate(final Calendar ecommLastModifiedDate) {
        this.ecommLastModifiedDate = ecommLastModifiedDate;
    }

    /**
     * Sets the publish status.
     *
     * @param status the new publish status
     */
    public void setPublishStatus(final long status) {
        this.publishStatus = Status.byId(status);
    }

    /**
     * Sets the publish status.
     *
     * @param status the new publish status
     */
    public void setPublishStatus(final Status status) {
        this.publishStatus = status;
    }

    /**
     * Gets the publish status.
     *
     * @return the publish status
     */
    public long getPublishStatus() {
        return publishStatus.getStatusId();
    }

    /**
     * Gets the status value.
     *
     * @return the status value
     */
    public String getStatusValue() {
        return publishStatus.name().toLowerCase();
    }

    /**
     * Gets the fulfiller name.
     *
     * @return the fulfiller name
     */
    public String getFulfillerName() {
        return fulfillerName;
    }


    /**
     * Sets vendor
     */
    public void setVendor(final String vendor) {
        this.vendor = vendor;
    }

    /**
     * Gets vendor
     * @return
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Sets the fulfiller name.
     *
     * @param fulfillerName the new fulfiller name
     */
    public void setFulfillerName(final String fulfillerName) {
        this.fulfillerName = fulfillerName;
    }

    /**
     * Checks if is sellable.
     *
     * @return true, if is sellable
     */
    public boolean isSellable() {
        return sellable;
    }

    /**
     * Sets the sellable.
     *
     * @param sellable the new sellable
     */
    public void setSellable(final boolean sellable) {
        this.sellable = sellable;
    }

    /**
     * Checks if is assembly required.
     * @return boolean boolean
     */
    public Boolean isAssemblyRequired() {
        return assemblyRequired;
    }

    /**
     * Sets the assembly required attribute.
     *
     * @param assemblyRequired is product required assembly
     */
    public void setAssemblyRequired(final Boolean assemblyRequired) {
        this.assemblyRequired = assemblyRequired;
    }

    /**
     * Is image associated.
     *
     * @return the boolean
     */
    public boolean isImageAssociated() {
        return isImageAssociated;
    }

    /**
     * Sets image associated.
     *
     * @param isImageAssociated the is image associated
     */
    public void setImageAssociated(boolean isImageAssociated) {
        this.isImageAssociated = isImageAssociated;
    }

    /**
     *  Get last modified date
     * @return Calendar jcr last modified
     */
    public Calendar getJcrLastModified() {
        return jcrLastModified;
    }

    /**
     * Set last modified date
     * @param jcrLastModified the jcr last modified
     */
    public void setJcrLastModified(final Calendar jcrLastModified) {
        this.jcrLastModified = jcrLastModified;
    }

    /**
     * Sets price in string representation.
     * For example : $1.0
     *
     * @param price the price
     */
    public void setPrice(final String price) {
        this.price = price;
    }

    /**
     * Gets price as String representation.
     * For example : $1.0
     *
     * Return null if no price set for product.
     *
     * @return the price
     */
    public String getPrice() {
        return this.price;
    }

    /**
     * Gets the last modified.
     *
     * @return the last modified
     */
    public String getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last modified.
     *
     * @param finalLastModified the new last modified
     */
    public void setLastModified(final String finalLastModified) {
        lastModified = finalLastModified;
    }

    /**
     * Gets stick warranty
     * @return stickWarranty boolean
     */
    public boolean isStickWarranty() {
        return stickWarranty;
    }

    /**
     * Sets stick warranty
     * @param stickWarranty the stick warranty
     */
    public void setStickWarranty(final boolean stickWarranty) {
        this.stickWarranty = stickWarranty;
    }

}
