package com.jcr.sling.junit.wrongmock;

import com.day.cq.wcm.api.Page;
import com.jcr.sling.junit.slingtest.query.add.FglJcrProductProperties;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductDetailLogoBeanTest {

    public static final String PRODUCT_PATH = "productPath";
    public static final String TEST_JPG = "test.jpg";

    @Mock
    private Page mPage;

    @Mock
    private Resource mCurrentPageResource;

    @Mock
    private Resource mProductResource;

    @Mock
    private Resource mBrandLogoResource;

    @Mock
    private Resource mBrandRootResource;

    @Mock
    private Node mBrandRootNode;

    @Mock
    private NodeIterator mRootNodeIterator;

    @Mock
    private Node mBrandLogoNode;

    @Mock
    private Property mBrandProperty;

    @Mock
    private ResourceResolver mResourceResolver;

    @Mock
    private BrandLogoService brandLogoService;

    @Mock
    private ValueMap mValueMap;

    @Mock
    private Value vValue;

    @Mock
    private Product mProduct;

    @Mock
    private ImageUrlResolverService mImageUrlResolverService;


    private ProductDetailLogoBean brandLogoBean;

    @Before
    public void prepareMocks() throws RepositoryException {

        when(mImageUrlResolverService.checkProperty(anyString())).thenReturn("imageReference");
        when(mPage.getContentResource()).thenReturn(mCurrentPageResource);
        when(mPage.getProperties()).thenReturn(mValueMap);

        when(mPage.getProperties().get(FglJcrProductProperties.PRODUCT_MASTER)).thenReturn(mock(Property.class));

        when(mCurrentPageResource.getResourceType()).thenReturn("sportchek/pages/product-detail-page");
        when(mProductResource.adaptTo(Product.class)).thenReturn(mProduct);
        when(mProductResource.isResourceType(anyString())).thenReturn(true);

        when(mResourceResolver.getResource(eq("/content/sportchek/en/brands"))).thenReturn(mBrandRootResource);
        when(mBrandRootResource.adaptTo(Node.class)).thenReturn(mBrandRootNode);

        when(mBrandRootNode.getNodes()).thenReturn(mRootNodeIterator);
        when(mRootNodeIterator.hasNext()).thenReturn(true, false);
        when(mRootNodeIterator.nextNode()).thenReturn(mBrandLogoNode);
        when(mBrandLogoNode.hasNode(eq("jcr:content"))).thenReturn(true);

        when(mBrandLogoNode.getNode(eq("jcr:content"))).thenReturn(mBrandLogoNode);

        when(mBrandLogoNode.hasProperty("brand")).thenReturn(true);

        when(vValue.getString()).thenReturn("Adidas");
        Value[] values = new Value[] { vValue };
        when(mBrandProperty.getValues()).thenReturn(values);
        when(mBrandLogoNode.getProperty("brand")).thenReturn(mBrandProperty);

        when(mValueMap.get(FglJcrProductProperties.PRODUCT_MASTER, String.class)).thenReturn(PRODUCT_PATH);

        when(mProduct.getBrand()).thenReturn("nike");

        when(mResourceResolver.resolve(PRODUCT_PATH)).thenReturn(mProductResource);
        when(mResourceResolver.resolve(null + "/brands/nike/jcr:content/brandLogo")).thenReturn(mBrandLogoResource);
        when(mBrandLogoResource.isResourceType(anyString())).thenReturn(true);

        when(mBrandLogoResource.adaptTo(Node.class)).thenReturn(mBrandLogoNode);
        when(brandLogoService.getBrandLogo(anyString(), any(Locale.class))).thenReturn(TEST_JPG);

        brandLogoBean = new ProductDetailLogoBean(mPage, mResourceResolver, brandLogoService);
    }

    @Test
    public void shouldGetPathToBrandLogo() {
        assertEquals("Should have same image path", TEST_JPG, brandLogoBean.getLogo());
    }

    @Test
    public void shouldGetPathToBrandPage() {
        assertTrue("Should have path to  same brand page ", brandLogoBean.getBrandPagePath().contains("nike.html"));
    }

}