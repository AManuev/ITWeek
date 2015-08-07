package com.jcr.sling.junit.slingtest.holders;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class SessionHolder implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SessionHolder.class);

    private Session session;

    public SessionHolder(final SlingRepository repository) {
        try {
            this.session = repository.loginAdministrative(null);
        } catch (RepositoryException e) {
            LOG.error("Can not create session in holder, authentication error: ", e);
        }
    }

    public SessionHolder(final SlingHttpServletRequest request) {
        this.session = request.getResourceResolver().adaptTo(Session.class);
    }

    
    public Session getSession() {
        return session;
    }

    @Override
    public void close() {
        if ((session != null) && (session.isLive())) {
            session.logout();
        }
    }
}
