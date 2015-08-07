package com.jcr.sling.junit.slingtest.callback;

import org.apache.sling.commons.testing.jcr.RepositoryUtil;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.apache.sling.testing.mock.sling.junit.SlingContextCallback;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;

public class RegisterNodeTypes implements SlingContextCallback {

    @Override
    public void execute(SlingContext slingContext) throws IOException {
        try {
            final Session session = slingContext.resourceResolver().adaptTo(Session.class);
            RepositoryUtil.registerNodeType(session, getClass().getClassLoader().getResourceAsStream("cq.cnd"));
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
}
