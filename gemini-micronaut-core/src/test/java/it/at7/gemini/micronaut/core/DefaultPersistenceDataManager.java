package it.at7.gemini.micronaut.core;

import io.micronaut.context.annotation.Requires;

@DefaultEntityDataManager
@Requires(property = "gemini.test-module", value = "core")
public class DefaultPersistenceDataManager extends PersistenceEntityDataManagerImpl implements PersistenceEntityDataManager {
}
