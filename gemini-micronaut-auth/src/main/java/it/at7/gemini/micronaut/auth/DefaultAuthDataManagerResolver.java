package it.at7.gemini.micronaut.auth;

import it.at7.gemini.micronaut.core.EntityDataManager;
import it.at7.gemini.micronaut.core.EntityManager;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultAuthDataManagerResolver implements AuthDataManagerResolver {

    @Inject
    EntityManager entityManager;

    EntityDataManager userManager;
    EntityDataManager profileManager;

    @PostConstruct
    void init() throws EntityNotFoundException {
        userManager = entityManager.getDataManager("USER");
        profileManager = entityManager.getDataManager("PROFILE");
    }

    @Override
    public EntityDataManager getUserDataManager() {
        return userManager;
    }

    @Override
    public EntityDataManager getProfileDataManager() {
        return profileManager;
    }
}
