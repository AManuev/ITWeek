package com.jcr.sling.junit.slingtest;

import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.testing.jcr.RepositoryTestBase;
import org.apache.sling.commons.testing.jcr.RepositoryUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;

//CHANGE: First extend RepositoryTestBase from the same package as RepositoryProvider.
@RunWith(MockitoJUnitRunner.class)
public class ImprovedBrandsDAOTest extends RepositoryTestBase {

    private static final String PATH_TO_BRAND_TAGS = "/etc/tags/sportchek/brands";
    private static final String ADIDAS_PAGE = "/content/sportchek/en/brands/adidas/jcr:content";
    private static final String SLING_FOLDER = "sling:Folder";
    private static final String CQ_TAG = "cq:Tag";
    private static final String ADIDAS_BRAND_DESCRIPTION = "Adidas brand description";
    private static final String SCENE7_IMAGE_LINK = "//s7d2.scene7.com/is/image/FGL/new-balance-logo";
    private static final String[] PREDEFINED_BRANDS = {"ADIDAS", "ASICS", "BAUER", "COLUMBIA"};

    private Node brandNode;

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @Mock
    private ResourceResolver resourceResolver;

    @Mock
    private Resource resource;

    @Mock
    private TagManager tagManager;

    @Mock
    private Tag tag;

    @InjectMocks
    private final BrandsDAO jcrBrandsDAO = new ImprovedJcrBrandsDAOImpl();

    @Before
    public void setUpRepository() throws NamingException, RepositoryException, IOException, LoginException {
        //CHANGE: use getSession() instead of session object in @Before method and remove field no need anymore;
        RepositoryUtil.registerNodeType(getSession(), getClass().getClassLoader().getResourceAsStream("cq.cnd"));

        brandNode = JcrUtil.createPath(PATH_TO_BRAND_TAGS, SLING_FOLDER, getSession());

        //CHANGE: add resourceResolverFactory logic
        given(resourceResolverFactory.getAdministrativeResourceResolver(null)).willReturn(resourceResolver);
        given(resourceResolver.adaptTo(Session.class)).willReturn(getSession());

        addBrands();
    }

    //CHANGE: @After with session logout is not need anymore

    @Test
    public void shouldGetBrandTags() throws RepositoryException {

        //CHANGE: remove all consider SlingRepository object

        Set<String> brands = jcrBrandsDAO.getSortedBrandTagsTitles();

        assertEquals("Wrong number of brands", brands.size(), 3);
    }

    @Test
    public void shouldGetBrands() throws RepositoryException, LoginException {

        given(resourceResolver.resolve(PATH_TO_BRAND_TAGS)).willReturn(resource);
        given(resource.adaptTo(Node.class)).willReturn(brandNode);

        List<Brand> brands = jcrBrandsDAO.getBrands();

        assertNotNull("Brands should be not null", brands);
        assertEquals("Brands size should be as expected", 3, brands.size());
    }

    @Test
    public void shouldGetAllBrands() throws RepositoryException, LoginException {

        given(resourceResolver.adaptTo(TagManager.class)).willReturn(tagManager);
        given(tagManager.resolve(anyString())).willReturn(tag, tag, tag, null);

        List<String> favBrands = Lists.newArrayList();
        List<Brand> brands = jcrBrandsDAO.getAllBrands(PREDEFINED_BRANDS, favBrands);

        assertNotNull("Brands should be not null", brands);
        assertEquals("Brands size should be as expected", 3, brands.size());
    }

    @Test
    public void shouldGetActiveBrands() throws RepositoryException {

        List<Brand> brands = jcrBrandsDAO.getActiveBrands(PREDEFINED_BRANDS, ImmutableList.<String>builder().add("ADIDAS").build());

        assertNotNull("Brands should be not null", brands);
        assertEquals("Brands size should be as expected", 1, brands.size());
    }

    @Test
    public void shouldGetBrandsWithAggregatedDataFromBrandPage() throws RepositoryException, LoginException {
        Node pageContent = JcrUtil.createPath(ADIDAS_PAGE, "cq:PageContent", session);

        Node brandLogo = pageContent.addNode("brandLogo", JcrConstants.NT_UNSTRUCTURED);
        brandLogo.setProperty("description", ADIDAS_BRAND_DESCRIPTION);
        brandLogo.setProperty("s7_imageReference", SCENE7_IMAGE_LINK);

        given(resourceResolver.resolve(anyString())).willReturn(resource, resource, null, null);
        given(resource.adaptTo(Node.class)).willReturn(brandNode, brandLogo);

        List<Brand> brands = jcrBrandsDAO.getBrands();

        Brand brand = brands.get(0);

        assertEquals("Brands size should be as expected", 3, brands.size());
        assertEquals("First element should be Adidas", "ADIDAS", brand.getId());
        assertEquals("Image link should be as expected", SCENE7_IMAGE_LINK, brand.getImage());
        assertEquals("Description should be as expected", ADIDAS_BRAND_DESCRIPTION, brand.getDescription());

        assertNull("Columbia should not have aggregated data", brands.get(1).getDescription());
    }

    private void addBrands() throws RepositoryException {
        Node adidas = brandNode.addNode("ADIDAS", CQ_TAG);
        adidas.setProperty(Property.JCR_TITLE, "Adidas");
        adidas.setProperty("label", "Adidas");
        Node columbia = brandNode.addNode("COLUMBIA", CQ_TAG);
        columbia.setProperty(Property.JCR_TITLE, "COLUMBIA");
        columbia.setProperty("label", "COLUMBIA");
        Node nikeAthletic = brandNode.addNode("NIKE_ATHLETIC", CQ_TAG);
        nikeAthletic.setProperty(Property.JCR_TITLE, "NIKE_ATHLETIC");
        nikeAthletic.setProperty("label", "NIKE_ATHLETIC");
    }

}