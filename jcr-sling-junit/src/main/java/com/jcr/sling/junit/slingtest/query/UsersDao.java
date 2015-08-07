package com.jcr.sling.junit.slingtest.query;

/**
 * Created by Andrii_Manuiev on 7/2/2015.
 */
public interface UsersDao {

    /**
     * Gets the user display name by its userId.
     *
     * @param userId the user id
     * @return the user display name
     */
    String getUserDisplayName(final String userId);
}
