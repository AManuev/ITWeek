package com.jcr.sling.junit.slingtest;

/**
 * The type Brand.
 */
public class Brand {

    private String id;

    private String name;

    private String image;

    private String description;
    
    private boolean subscribed;

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets image.
     *
     * @return the image
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets image.
     *
     * @param image the image
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Checks if is subscribed.
     *
     * @return true, if is subscribed
     */
    public boolean isSubscribed() {
        return subscribed;
    }

    /**
     * Sets the subscribed.
     *
     * @param subscribed the new subscribed
     */
    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }
}
