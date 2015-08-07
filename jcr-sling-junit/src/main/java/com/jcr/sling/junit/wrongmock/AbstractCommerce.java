package com.jcr.sling.junit.wrongmock;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public abstract class AbstractCommerce {

    private static final String PREFIX_SEPARATOR = "_";

    protected final ValueMap properties;

    AbstractCommerce(final Resource resource) {
        this.properties = new ValueMapDecorator(new HashMap<>(resource.adaptTo(ValueMap.class)));
    }

    /**
     * Gets the keys.
     *
     * @return the keys
     */
    public Set<String> getKeys() {
        return properties.keySet();
    }

    /**
     * Gets the value by key.
     *
     * @param <T>  the generic type
     * @param key  the key
     * @param type the type
     * @return the value by key
     */
    public <T> T getValueByKey(final String key, final Class<T> type) {
        return properties.get(key, type);
    }

    /**
     * Check is attribute exist in SKU properties.
     *
     * @param key - property name
     * @return a {@link Boolean}
     */
    public boolean hasKey(final String key) {
        return properties.containsKey(key);
    }

    public String getLocalizedProperty(final Locale locale, final String propertyName) {
        return properties.get(getLocalePrefix(locale) + propertyName, String.class);
    }

    private static String getLocalePrefix(final Locale locale) {
        String result = StringUtils.EMPTY;
        if (!Locale.ENGLISH.equals(locale)) {
            result = locale.getLanguage() + PREFIX_SEPARATOR;
        }
        return result;
    }
}
