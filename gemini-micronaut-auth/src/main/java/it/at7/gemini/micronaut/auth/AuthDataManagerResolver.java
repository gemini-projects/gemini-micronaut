package it.at7.gemini.micronaut.auth;

import it.at7.gemini.micronaut.core.EntityDataManager;

public interface AuthDataManagerResolver {

    /**
     * Get the User Data Manager
     */
    EntityDataManager getUserDataManager();

    /**
     * Get the Profile Data Manager
     */
    EntityDataManager getProfileDataManager();

    /**
     * Get the Namespace Basic Auth Data Manager
     */
    EntityDataManager getNSBasicAuthDataManager();
}
