package com.jcr.sling.junit.slingtest;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.TagManager;
import com.google.common.collect.Lists;
import com.jcr.sling.junit.slingtest.holders.ResolverHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * The type Jcr brands dAO impl.
 */
@Component
@Service(BrandsDAO.class)
public class ImprovedJcrBrandsDAOImpl implements BrandsDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ImprovedJcrBrandsDAOImpl.class);

    private static final String PATH_TO_BRAND_TAGS = "/etc/tags/sportchek/brands";

    private static final String BRAND_INFO_NODE = "/content/sportchek/en/brands/%s/jcr:content/brandLogo";
    private static final String DESCRIPTION = "description";
    private static final String SCENE7_IMAGE_REFERENCE = "s7_imageReference";
    private static final String HYPHEN = "-";
    private static final String UNDERSCORE = "_";

    //1 Remove Sling repository or resourceResolverFactory.
    //They both do the same thing.

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public Set<String> getSortedBrandTagsTitles() {
        Set<String> result = new TreeSet<>();

        try (ResolverHolder resolverHolder = new ResolverHolder(resourceResolverFactory)) {
            //* changed *//
            Session session = resolverHolder.getResolver().adaptTo(Session.class);
            // ** //
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

}
