package com.jcr.sling.junit.wrongmock;

import com.jcr.sling.junit.slingtest.query.add.FglJcrProductProperties;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.HashMap;

public class Barcode {
    private final ValueMap properties;

    public Barcode(final Resource resource) {
        this.properties = new ValueMapDecorator(new HashMap<>(resource.adaptTo(ValueMap.class)));
    }

    /**
     * Gets the value of the code property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCode() {
        return properties.get(FglJcrProductProperties.CODE, String.class);
    }

    /**
     * Gets the value of the type property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getType() {
        return properties.get(FglJcrProductProperties.BARCODE_TYPE, String.class);
    }

    /**
     * Gets the value of the text property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getText() {
        return properties.get(FglJcrProductProperties.BARCODE_TEXT, String.class);
    }

    /**
     * Gets the value of the active property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isActive() {
        return properties.get(FglJcrProductProperties.BARCODE_ACTIVE, Boolean.class);
    }

    /**
     * Gets the value of the primary property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isPrimary() {
        return properties.get(FglJcrProductProperties.BARCODE_PRIMARY, Boolean.class);
    }

    /**
     * Gets the value of the is default property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isDefault() {
        return properties.get(FglJcrProductProperties.BARCODE_DEFAULT, Boolean.class);
    }

}
