package com.jcr.sling.junit.slingtest;

import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.tagging.TagManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.jcr.sling.junit.slingtest.holders.ResolverHolder;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.testing.jcr.RepositoryProvider;
import org.apache.sling.commons.testing.jcr.RepositoryUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class BrandsDAOTest {

    private static final String PATH_TO_BRAND_TAGS = "/etc/tags/sportchek/brands";
    private static final String ADIDAS_PAGE = "/content/sportchek/en/brands/adidas/jcr:content";
    public static final String SLING_FOLDER = "sling:Folder";
    public static final String CQ_TAG = "cq:Tag";
    private static final String ADIDAS_BRAND_DESCRIPTION = "Adidas brand description";
    private static final String SCENE7_IMAGE_LINK = "//s7d2.scene7.com/is/image/FGL/new-balance-logo";

    private static final String[] PREDEFINED_BRANDS = {"ADIDAS","ASICS","BAUER","COLUMBIA"};

    private Node brandNode;

    private Session session;

    @Mock
    private SlingRepository mockSlingRepository;
    @Mock
    private QueryBuilder mockQueryBuilder;
    @Mock
    private Query mockQuery;
    @Mock
    private SearchResult mockSearchResult;

    @InjectMocks
    private final BrandsDAO jcrBrandsDAO = new JcrBrandsDAOImpl();

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @Mock
    private ResolverHolder resolverHolder;

    @Mock
    private ResourceResolver resourceResolver;

    @Mock
    private Resource resource;

    @Mock
    private TagManager tagManager;

    @Mock
    private com.day.cq.tagging.Tag tag;

    @Before
    public void setUp() throws Exception {
        session = RepositoryProvider.instance().getRepository().loginAdministrative(null);
        RepositoryUtil.registerNodeType(session, getClass().getClassLoader().getResourceAsStream("cq.cnd"));

        brandNode = JcrUtil.createPath(PATH_TO_BRAND_TAGS, SLING_FOLDER, session);
        addBrands();
    }

    @After
    public void tearDown() {
        if ((session != null) && (session.isLive())) {
            session.logout();
        }
    }

    @Test
    public void shouldGetBrandTags() throws RepositoryException {
        // given
        given(mockSlingRepository.loginAdministrative(null)).willReturn(session);
        given(mockQueryBuilder.createQuery(Mockito.any(PredicateGroup.class), Mockito.any(Session.class))).willReturn(
                mockQuery);
        given(mockQuery.getResult()).willReturn(mockSearchResult);
        given(mockSearchResult.getNodes()).willReturn(brandNode.getNodes());
        // when
        Set<String> brands = jcrBrandsDAO.getSortedBrandTagsTitles();
        // then
        assertEquals("Wrong number of brands", brands.size(), 3);
    }

    @Test
    public void shouldGetBrands() throws javax.jcr.RepositoryException, LoginException {
        given(mockSlingRepository.loginAdministrative(null)).willReturn(session);
        given(resourceResolverFactory.getAdministrativeResourceResolver(null)).willReturn(resourceResolver);
        given(resolverHolder.getResolver()).willReturn(resourceResolver);
        given(resourceResolver.resolve(PATH_TO_BRAND_TAGS)).willReturn(resource);
        given(resource.adaptTo(Node.class)).willReturn(brandNode);

        List<Brand> brands = jcrBrandsDAO.getBrands();

        assertNotNull("Brands should be not null", brands);
        assertEquals("Brands size should be as expected", 3, brands.size());
    }

    @Test
    public void shouldGetAllBrands() throws javax.jcr.RepositoryException, LoginException {
        given(mockSlingRepository.loginAdministrative(null)).willReturn(session);
        given(resourceResolverFactory.getAdministrativeResourceResolver(null)).willReturn(resourceResolver);
        given(resolverHolder.getResolver()).willReturn(resourceResolver);
        given(resourceResolver.adaptTo(TagManager.class)).willReturn(tagManager);
        given(tagManager.resolve(anyString())).willReturn(tag, tag, tag, null);

        List<String> favBrands = Lists.newArrayList();
        List<Brand> brands = jcrBrandsDAO.getAllBrands(PREDEFINED_BRANDS, favBrands);

        assertNotNull("Brands should be not null", brands);
        assertEquals("Brands size should be as expected", 3, brands.size());
    }

    @Test
    public void shouldGetActiveBrands() throws javax.jcr.RepositoryException {
        given(mockSlingRepository.loginAdministrative(null)).willReturn(session);

        List<Brand> brands = jcrBrandsDAO.getActiveBrands(PREDEFINED_BRANDS, ImmutableList.<String>builder().add("ADIDAS").build());

        assertNotNull("Brands should be not null", brands);
        assertEquals("Brands size should be as expected", 1, brands.size());
    }

    @Test
    public void shouldGetBrandsWithAggregatedDataFromBrandPage() throws javax.jcr.RepositoryException, LoginException {
        Node pageContent = JcrUtil.createPath(ADIDAS_PAGE, "cq:PageContent", session);

        Node brandLogo = pageContent.addNode("brandLogo", JcrConstants.NT_UNSTRUCTURED);
        brandLogo.setProperty("description", ADIDAS_BRAND_DESCRIPTION);
        brandLogo.setProperty("s7_imageReference", SCENE7_IMAGE_LINK);

        given(mockSlingRepository.loginAdministrative(null)).willReturn(session);
        given(resourceResolverFactory.getAdministrativeResourceResolver(null)).willReturn(resourceResolver);
        given(resolverHolder.getResolver()).willReturn(resourceResolver);
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

    // this suit never test real RepositoryException, exception on getting session - incorrect,
    // because on runtime
    // you can get RepositoryException just on work with repository
    @Ignore
    @Test
    public void testRepositoryExceptionIsIgnored() throws RepositoryException {
        // given
        doThrow(new RepositoryException()).when(mockSlingRepository).loginAdministrative(null);
        // when
        jcrBrandsDAO.getSortedBrandTagsTitles();
    }


    private void addBrands() throws javax.jcr.RepositoryException {
        Node adidas = this.brandNode.addNode("ADIDAS", CQ_TAG);
        adidas.setProperty(Property.JCR_TITLE, "Adidas");
        adidas.setProperty("label", "Adidas");
        Node columbia = this.brandNode.addNode("COLUMBIA", CQ_TAG);
        columbia.setProperty(Property.JCR_TITLE, "COLUMBIA");
        columbia.setProperty("label", "COLUMBIA");
        Node nikeAthletic = this.brandNode.addNode("NIKE_ATHLETIC", CQ_TAG);
        nikeAthletic.setProperty(Property.JCR_TITLE, "NIKE_ATHLETIC");
        nikeAthletic.setProperty("label", "NIKE_ATHLETIC");
    }

}
