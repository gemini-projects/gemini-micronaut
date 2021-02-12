package it.at7.gemini.micronaut.auth;

import it.at7.gemini.micronaut.core.EntityDataManager;

public interface AuthDataManagerResolver {

    EntityDataManager getUserDataManager();

    EntityDataManager getProfileDataManager();
}
