package com.jcr.sling.junit.slingtest;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.tagging.TagManager;
import com.google.common.collect.Lists;
import com.jcr.sling.junit.slingtest.constants.FglPathConstants;
import com.jcr.sling.junit.slingtest.holders.ResolverHolder;
import com.jcr.sling.junit.slingtest.holders.SessionHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.util.*;

import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * The type Jcr brands dAO impl.
 */
@Component
@Service(BrandsDAO.class)
public class JcrBrandsDAOImpl implements BrandsDAO {

    private static final Logger LOG = LoggerFactory.getLogger(JcrBrandsDAOImpl.class);

    private static final String PATH_TO_BRAND_TAGS = "/etc/tags/sportchek/brands";

    private static final String BRAND_INFO_NODE = "/content/sportchek/en/brands/%s/jcr:content/brandLogo";
    private static final String DESCRIPTION = "description";
    private static final String SCENE7_IMAGE_REFERENCE = "s7_imageReference";
    private static final String HYPHEN = "-";
    private static final String UNDERSCORE = "_";

    @Reference
    private SlingRepository repository;

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getSortedBrandTagsTitles() {
        Set<String> result = new TreeSet<>();

        try (SessionHolder sessionHolder = new SessionHolder(repository)) {
            Session session = sessionHolder.getSession();
            Node brandTagsRootNode = session.getNode(PATH_TO_BRAND_TAGS);
            NodeIterator nodeIterator = brandTagsRootNode.getNodes();
            while (nodeIterator.hasNext()) {
                Node brandNode = nodeIterator.nextNode();
                result.add(JcrUtils.getStringProperty(brandNode, JcrConstants.JCR_TITLE, StringUtils.EMPTY));
            }
        } catch (RepositoryException e) {
            LOG.error("Failed to read brand list from repository:  ", e);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Brand> getActiveBrands(String[] filterArray, List<String> favouriteBrands) {
        List<Brand> result = Lists.newArrayList();
        for (String item : favouriteBrands) {
            final String label = item.replaceAll(UNDERSCORE, " ");
            final Brand brand = getBrand(label, item, true);
            result.add(brand);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Brand> getAllBrands(final String[] filterArray, final List<String> favouriteBrands) {
        List<Brand> result = Lists.newArrayList();

        try (ResolverHolder resolverHolder = new ResolverHolder(resourceResolverFactory)) {
            final TagManager tagManager = resolverHolder.getResolver().adaptTo(TagManager.class);
            for (String id : filterArray) {
                final String tagName = PATH_TO_BRAND_TAGS + "/" + id.replaceAll(UNDERSCORE, HYPHEN);
                final com.day.cq.tagging.Tag tag = tagManager.resolve(tagName);
                if (tag != null) {
                    final String name = tag.getTitle();
                    final boolean isSubscribed = favouriteBrands.contains(id);
                    final Brand brand = getBrand(name, id, isSubscribed);
                    result.add(brand);
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Brand> getBrands() throws RepositoryException {
        List<Brand> result = Lists.newArrayList();
        try (ResolverHolder resolverHolder = new ResolverHolder(resourceResolverFactory)) {
            final Node brandsNode = resolverHolder.getResolver().resolve(PATH_TO_BRAND_TAGS).adaptTo(Node.class);
            final Iterable<Node> brandsList = JcrUtils.getChildNodes(brandsNode);
            for (Node brandNode : brandsList) {
                final String name = JcrUtils.getStringProperty(brandNode, JcrConstants.JCR_TITLE, brandNode.getName());
                final String id = brandNode.getName().replaceAll(HYPHEN, UNDERSCORE).toUpperCase();

                Brand brand = new Brand();
                brand.setId(id);
                brand.setName(name);
                populateBrandLogoInfo(brand, brandNode.getName().toLowerCase(), resolverHolder.getResolver());

                result.add(brand);
            }
        }
        return result;
    }

    private void populateBrandLogoInfo(final Brand brand, final String brandName, final ResourceResolver resolver)
            throws RepositoryException {
        final String brandLogoPath = String.format(BRAND_INFO_NODE, brandName);

        final Resource brandLogoResource = resolver.resolve(brandLogoPath);
        if (!(brandLogoResource == null || brandLogoResource instanceof NonExistingResource)) {
            final Node brandLogoNode = brandLogoResource.adaptTo(Node.class);
            final String description = JcrUtils.getStringProperty(brandLogoNode, DESCRIPTION, EMPTY);
            final String scene7Image = JcrUtils.getStringProperty(brandLogoNode, SCENE7_IMAGE_REFERENCE, EMPTY);
            brand.setDescription(description);
            brand.setImage(scene7Image);
        }
    }

    private Brand getBrand(final String label, final String id, final boolean isSubscribed) {
        final Brand brand = new Brand();
        brand.setName(label);
        brand.setId(id);
        brand.setSubscribed(isSubscribed);
        return brand;
    }

    private List<Tag> searchBrandTagsList(final Session session) throws RepositoryException {
        List<Tag> result = new LinkedList<>();
        Map<String, String> map = createBrandSearchPredicates();

        Iterator<Node> iterator = searchForNodes(map, session);
        while (iterator.hasNext()) {
            Node tagNode = iterator.next();
            Tag tag = createTag(tagNode);
            result.add(tag);
        }

        return result;
    }

    private static Map<String, String> createBrandSearchPredicates() {
        Map<String, String> map = new HashMap<>();

        map.put("path", FglPathConstants.BRAND_PATH_PREFIX);
        map.put("p.offset", "0");
        map.put("p.limit", "10000");
        map.put("orderby",  "@jcr:title");
        map.put("orderby.sort", "asc");

        return map;
    }

    private static Tag createTag(final Node tagNode) throws RepositoryException {
        Tag tag = new Tag();
        tag.setTitle(tagNode.getProperty(Property.JCR_TITLE).getString());
        return tag;
    }

    private Iterator<Node> searchForNodes(final Map<String, String> predicates, final Session session) {
        Query query = queryBuilder.createQuery(PredicateGroup.create(predicates), session);
        SearchResult searchResult = query.getResult();
        return searchResult.getNodes();
    }
}
