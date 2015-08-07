package com.jcr.sling.junit.slingtest.mocksling;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.jcr.sling.junit.slingtest.Brand;
import com.jcr.sling.junit.slingtest.BrandsDAO;
import com.jcr.sling.junit.slingtest.ImprovedAgainAndAgainJcrBrandsDAOImpl;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.testing.mock.sling.MockSling;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/***
 * NOTES:
 *
 * Improve Unit test with a Sling Mocking.
 * https://sling.apache.org/documentation/development/sling-mock.html
 */
@RunWith(MockitoJUnitRunner.class)
public class ImprovedAgainAndAgainJcrBrandsDAOImplTest {

    private static final String PATH_TO_BRAND_TAGS = "/etc/tags/sportchek/brands";
    private static final String ADIDAS_PAGE = "/content/sportchek/en/brands/adidas/jcr:content";
    private static final String SLING_FOLDER = "sling:Folder";
    private static final String ADIDAS_BRAND_DESCRIPTION = "Adidas brand description";
    private static final String SCENE7_IMAGE_LINK = "//s7d2.scene7.com/is/image/FGL/new-balance-logo";

    private static final String[] PREDEFINED_BRANDS = {"ADIDAS", "ASICS", "BAUER", "COLUMBIA"};

    /**
     * ResourceResolverType - is different type of adapters for the mock.
     * Depending on the type an underlying JCR repository is used or not, and the data
     * is stored in-memory or in a real repository.
     */
    @Spy
    private ResourceResolverFactory resourceResolverFactory = MockSling.newResourceResolverFactory();

    @Mock
    private TagManager tagManager;

    @InjectMocks
    private final BrandsDAO jcrBrandsDAO = new ImprovedAgainAndAgainJcrBrandsDAOImpl();

    private ResourceResolver resourceResolver;

    @Before
    public void setUpRepository() throws LoginException, PersistenceException {

        resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
        final Resource brandResource = ResourceUtil.getOrCreateResource(resourceResolver, PATH_TO_BRAND_TAGS, SLING_FOLDER, SLING_FOLDER, true);

        resourceResolver.create(brandResource, "ADIDAS", ImmutableMap.<String, Object>builder().put(JcrConstants.JCR_TITLE, "Adidas").build());
        resourceResolver.create(brandResource, "COLUMBIA", ImmutableMap.<String, Object>builder().put(JcrConstants.JCR_TITLE, "COLUMBIA").build());
        resourceResolver.create(brandResource, "NIKE_ATHLETIC", ImmutableMap.<String, Object>builder().put(JcrConstants.JCR_TITLE, "NIKE_ATHLETIC").build());
        resourceResolver.commit();
    }

    @Test
    public void shouldGetBrandTags() {

        Set<String> brands = jcrBrandsDAO.getSortedBrandTagsTitles();
        assertEquals("Wrong number of brands", brands.size(), 3);
    }

    @Test
    public void shouldGetBrands() throws RepositoryException {

        List<Brand> brands = jcrBrandsDAO.getBrands();

        assertNotNull("Brands should be not null", brands);
        assertEquals("Brands size should be as expected", 3, brands.size());
    }

    @Test
    public void shouldGetAllBrands() throws RepositoryException, LoginException {
        ResourceResolver spyResolver = spy(resourceResolver);
        given(spyResolver.adaptTo(TagManager.class)).willReturn(tagManager);
        given(resourceResolverFactory.getAdministrativeResourceResolver(null)).willReturn(spyResolver);
        Tag tag = mock(Tag.class);

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
    public void shouldGetBrandsWithAggregatedDataFromBrandPage() throws RepositoryException, LoginException, PersistenceException {

        final Resource adidasPageContent = ResourceUtil.getOrCreateResource(resourceResolver, ADIDAS_PAGE, "cq:PageContent", SLING_FOLDER, true);
        resourceResolver.create(adidasPageContent, "brandLogo", ImmutableMap.<String, Object>builder()
                .put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED)
                .put("description", ADIDAS_BRAND_DESCRIPTION)
                .put("s7_imageReference", SCENE7_IMAGE_LINK)
                .build());
        resourceResolver.commit();


        List<Brand> brands = jcrBrandsDAO.getBrands();

        Brand brand = brands.get(0);
        assertEquals("First element should be Adidas", "ADIDAS", brand.getId());
        assertEquals("Image link should be as expected", SCENE7_IMAGE_LINK, brand.getImage());
        assertEquals("Description should be as expected", ADIDAS_BRAND_DESCRIPTION, brand.getDescription());
        assertNull("Columbia should not have aggregated data", brands.get(1).getDescription());
    }

}