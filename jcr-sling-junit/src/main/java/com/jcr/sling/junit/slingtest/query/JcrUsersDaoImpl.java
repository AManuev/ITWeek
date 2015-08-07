package com.jcr.sling.junit.slingtest.query;

import com.adobe.granite.security.user.UserProperties;
import com.adobe.granite.security.user.UserPropertiesManager;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * The Class JcrUsersDaoImpl represents user display name by its userId.
 */
@Component
@Service(UsersDao.class)
public class JcrUsersDaoImpl implements UsersDao {

    private static final String PROFILE_NODE = "profile";

    private static final Logger LOGGER = LoggerFactory.getLogger(JcrUsersDaoImpl.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /*
     * (non-Javadoc)
     *
     * @see com.fglsports.wcm.dao.UsersDao#getUserDisplayName(java.lang.String)
     */
    @Override
    public String getUserDisplayName(final String userId) {
        String result = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(userId)) {
            ResourceResolver administrativeResourceResolver = null;
            try {
                administrativeResourceResolver = resourceResolverFactory
                        .getAdministrativeResourceResolver(null);
                Session session = administrativeResourceResolver
                        .adaptTo(Session.class);
                if ((session instanceof JackrabbitSession)) {
                    UserManager userManager = ((JackrabbitSession) session)
                            .getUserManager();
                    Authorizable user = userManager.getAuthorizable(userId);
                    result = getName(administrativeResourceResolver, user,
                            userId);
                }
            } catch (RepositoryException | LoginException ex) {
                LOGGER.error("Error to get user displayName", ex);
            } finally {
                if (administrativeResourceResolver != null
                        && administrativeResourceResolver.isLive()) {
                    administrativeResourceResolver.close();
                }
            }
        }
        return result;
    }

    private String getName(
            final ResourceResolver administrativeResourceResolver,
            final Authorizable user, final String userId)
            throws RepositoryException {
        String result = StringUtils.EMPTY;
        Resource resource = administrativeResourceResolver.resolve(user
                .getPath());
        if (!ResourceUtil.isNonExistingResource(resource)) {
            UserPropertiesManager userPropertiesManager = resource
                    .adaptTo(UserPropertiesManager.class);
            UserProperties userProperties = userPropertiesManager
                    .getUserProperties(userId, PROFILE_NODE);
            result = userProperties.getDisplayName();
        }
        return result;
    }
}
