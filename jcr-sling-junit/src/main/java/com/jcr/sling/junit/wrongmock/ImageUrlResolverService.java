package com.jcr.sling.junit.wrongmock;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public interface ImageUrlResolverService {
    /**
     * Check which property (s7/dam) to use
     * @param property the property
     * @return proper property name
     */
    String checkProperty(String property);

    /**
     * Defines which image link to use (s7/dam)
     * @param damLink the dam link
     * @param s7Link the s 7 link
     * @return string
     */
    String chooseLink(String damLink, String s7Link);

    /**
     * Returns image property value ("imageReference" - default name of property)
     * @param node the node
     * @return image reference
     * @throws RepositoryException the repository exception
     */
    String getImageReference(Node node) throws RepositoryException;

    /**
     * Returns image property value.
     *
     * @param path to the node
     * @param property to name of the property
     *
     * @return DAM/Scene7 image reference
     */
    String getImageReference(String path, String property);

    /**
     * Returns scene7 images root
     * @return S7 images root
     */
    String getS7ImagesRoot();

    /**
     * Return default image from scene7
     * @return S7 default image
     */
    String getS7DefaultImage();
}
