package it.at7.gemini.micronaut.auth;

import io.micronaut.context.annotation.Requires;
import it.at7.gemini.micronaut.core.EntityDataManager;
import it.at7.gemini.micronaut.core.EntityManager;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Requires(property = "gemini.auth.datamanager", value = "default", defaultValue = "default")
public class DefaultAuthDataManagerResolver implements AuthDataManagerResolver {

    @Inject
    EntityManager entityManager;

    EntityDataManager userManager;
    EntityDataManager profileManager;
    EntityDataManager nsBasicAuthManager;

    @PostConstruct
    void init() throws EntityNotFoundException {
        userManager = entityManager.getDataManager("USER");
        profileManager = entityManager.getDataManager("PROFILE");
        nsBasicAuthManager = entityManager.getDataManager("NSBASICAUTH");
    }

    @Override
    public EntityDataManager getUserDataManager() {
        return userManager;
    }

    @Override
    public EntityDataManager getProfileDataManager() {
        return profileManager;
    }

    @Override
    public EntityDataManager getNSBasicAuthDataManager() {
        return nsBasicAuthManager;
    }
}
