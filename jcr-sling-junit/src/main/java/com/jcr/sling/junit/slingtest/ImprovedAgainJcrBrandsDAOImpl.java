package com.jcr.sling.junit.slingtest;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.TagManager;
import com.google.common.collect.Lists;
import com.jcr.sling.junit.slingtest.holders.ResolverHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
@Service(BrandsDAO.class)
public class ImprovedAgainJcrBrandsDAOImpl implements BrandsDAO {

    private static final String PATH_TO_BRAND_TAGS = "/etc/tags/sportchek/brands";
    private static final String BRAND_INFO_NODE = "/content/sportchek/en/brands/%s/jcr:content/brandLogo";
    private static final String DESCRIPTION = "description";
    private static final String SCENE7_IMAGE_REFERENCE = "s7_imageReference";
    private static final String HYPHEN = "-";
    private static final String UNDERSCORE = "_";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public Set<String> getSortedBrandTagsTitles() {
        Set<String> result = new TreeSet<>();

        try (ResolverHolder resolverHolder = new ResolverHolder(resourceResolverFactory)) {
            //* changed *//
            // so lets refactor a method and class to use only Resources instead of node.
            // in this way I decrease a FanOut complexity and make class robust.

            ResourceResolver resolver = resolverHolder.getResolver();
            Resource brandTagsRootResource = resolver.getResource(PATH_TO_BRAND_TAGS);
            final Iterator<Resource> resourceIterator = brandTagsRootResource.listChildren();

            while (resourceIterator.hasNext()) {
                Resource brandResource = resourceIterator.next();
                result.add(brandResource.adaptTo(ValueMap.class).get(JcrConstants.JCR_TITLE, StringUtils.EMPTY));
            }
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
            final Resource brandsResource = resolverHolder.getResolver().resolve(PATH_TO_BRAND_TAGS);
            final Iterable<Resource> brandsList = brandsResource.getChildren();
            for (Resource brandResource : brandsList) {
                String name = brandResource.adaptTo(ValueMap.class).get(JcrConstants.JCR_TITLE, brandResource.getName());
                final String id = brandResource.getName().replaceAll(HYPHEN, UNDERSCORE).toUpperCase();

                Brand brand = new Brand();
                brand.setId(id);
                brand.setName(name);
                populateBrandLogoInfo(brand, brandResource.getName().toLowerCase(), resolverHolder.getResolver());

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

            final ValueMap properties = brandLogoResource.adaptTo(ValueMap.class);
            brand.setDescription(properties.get(DESCRIPTION, StringUtils.EMPTY));
            brand.setImage(properties.get(SCENE7_IMAGE_REFERENCE, StringUtils.EMPTY));
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

