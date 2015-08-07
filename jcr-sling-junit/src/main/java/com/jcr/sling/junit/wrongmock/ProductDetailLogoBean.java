package com.jcr.sling.junit.wrongmock;

import com.day.cq.commons.LanguageUtil;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import static com.jcr.sling.junit.slingtest.query.add.FglJcrProductProperties.*;

public class ProductDetailLogoBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDetailLogoBean.class);

    private static final String PRODUCT_DETAIL_PAGE_TYPE = "sportchek/pages/product-detail-page";

    private static final String BRANDS_LOGO_PATH_PATTERN = "/brands/%s";
    private static final String BRANDS_NOT_FOUND_PATTERN = "brand '%s' was not found";

    private static final String COMMERCE_COMPONENTS_PRODUCT = "commerce/components/product";
    private static final String BRAND_PROPERTY = "brand";
    private static final String JCR_CONTENT_NAME = "jcr:content";
    private static final String BRANDS_JCR_PATH = "/content/sportchek/en/brands";
    private static final String[] BRAND_PROP_PREFIX = { "sportchek:brands/", /* "sportchek:custom/brands/" */};
    private String brandPagePath;
    private String imageLogoPath;

    /**
     * BrandLogo information bean.
     *
     * @param currentPage      the current page
     * @param resourceResolver the resource resolver
     * @param brandLogoService logo service
     */
    //@Inject
    public ProductDetailLogoBean(final Page currentPage, final ResourceResolver resourceResolver,
                                 final BrandLogoService brandLogoService) {
        Resource currentPageResource = currentPage.getContentResource();

        if (isProductDetailPage(currentPageResource)) {

            final String pathToProduct = currentPage.getProperties().get(PRODUCT_MASTER, String.class);
            final Resource productResource = resourceResolver.resolve(pathToProduct);
            if (productResource.isResourceType(COMMERCE_COMPONENTS_PRODUCT)) {
                final Product product = productResource.adaptTo(Product.class);
                final String rootPath = LanguageUtil.getLanguageRoot(currentPage.getPath());
                final String brand = product.getBrand();
                String correctBrandName = brand;
                try {
                    long startTime = System.currentTimeMillis();
                    String tmpBrandName = getCorrectBrandName(resourceResolver, brand);
                    if (StringUtils.isNotBlank(tmpBrandName)) {
                        correctBrandName = tmpBrandName.toLowerCase();
                        long endTime = System.currentTimeMillis();
                        long resTime = endTime - startTime;
                        LOGGER.trace(String.format("brand '%s' was found for %s milliseconds", brand, resTime));
                    } else {
                        LOGGER.error(String.format(BRANDS_NOT_FOUND_PATTERN, brand));
                    }
                } catch (RepositoryException e) {
                    LOGGER.error(String.format(BRANDS_NOT_FOUND_PATTERN, brand), e);
                }

                brandPagePath = rootPath + String.format(BRANDS_LOGO_PATH_PATTERN + ".html", correctBrandName);
                imageLogoPath = brandLogoService.getBrandLogo(product.getCode(), currentPage.getLanguage(true));
            }
        }
    }

    /**
     * find correct brand name by input brandName
     * @param resourceResolver
     * @param brandName
     * @return correct brand name
     * @throws RepositoryException
     */
    private String getCorrectBrandName(final ResourceResolver resourceResolver, final String brandName)
            throws RepositoryException {

        String retVal = null;
        Resource brandsNodeRes = resourceResolver.getResource(BRANDS_JCR_PATH);
        Node brandsNode = brandsNodeRes.adaptTo(Node.class);
        NodeIterator brandsNodeIter = brandsNode.getNodes();
        while (brandsNodeIter.hasNext()) {
            try {
                Node brandNode = brandsNodeIter.nextNode();
                if (brandNode.hasNode(JCR_CONTENT_NAME)) {
                    String tmpVal = extractBrandName(brandNode, brandName);
                    if (StringUtils.isNotBlank(tmpVal)) {
                        retVal = tmpVal;
                        break;
                    }
                }
            } catch (RepositoryException ex) {
                LOGGER.error("error occurred when search brand name", ex);
            }
        }
        return retVal;
    }

    /**
     * extract brand name from JCR Node
     * @param brandNode
     * @param brandName
     * @return brand name
     * @throws RepositoryException
     */
    private String extractBrandName(final Node brandNode, final String brandName) throws RepositoryException {
        String retVal = null;
        String tmpBrandName = brandName.toLowerCase();
        Node brandContent = brandNode.getNode(JCR_CONTENT_NAME);

        Value[] val = null;
        if (brandContent.hasProperty(BRAND_PROPERTY)) {
            Property brandProperty = brandContent.getProperty(BRAND_PROPERTY);
            val = brandProperty.getValues();
        }
        if (val != null && val.length > 0) {
            String foundBrandName = val[0].getString();
            if (StringUtils.isNotBlank(foundBrandName) && stringContainsItemFromList(foundBrandName, BRAND_PROP_PREFIX)) {
                String correctBrandName = trimBrandPropPrefix(foundBrandName, BRAND_PROP_PREFIX).toLowerCase();
                if (correctBrandName.equals(tmpBrandName)) {
                    retVal = brandNode.getName();
                }
            }
        }
        return retVal;
    }

    /**
     * check the contents of the substring
     * @param input string
     * @param items substring array
     * @return true if contain
     */
    private boolean stringContainsItemFromList(final String input, final String[] items) {
        boolean retVal = false;
        for (int i = 0; i < items.length; i++) {
            if (input.contains(items[i])) {
                retVal = true;
            }
        }
        return retVal;
    }

    /**
     * remove all substring from input string
     * @param input
     * @param items
     * @return - result string
     */
    private String trimBrandPropPrefix(final String input, final String[] items) {
        String retVal = input;
        for (int i = 0; i < items.length; i++) {
            retVal = retVal.replace(items[i], "");
        }
        return retVal;
    }

    /**
     * BrandLogo image path
     *
     * @return - image path
     */
    public String getLogo() {
        return imageLogoPath;
    }

    /**
     * Brand page Path
     *
     * @return brand page path
     */
    public String getBrandPagePath() {
        return brandPagePath;
    }

    private boolean isProductDetailPage(final Resource currentPageResource) {
        return PRODUCT_DETAIL_PAGE_TYPE.equals(currentPageResource.getResourceType());
    }
}
