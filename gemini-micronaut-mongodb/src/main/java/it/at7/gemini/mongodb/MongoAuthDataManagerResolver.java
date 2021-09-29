package it.at7.gemini.mongodb;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import it.at7.gemini.micronaut.auth.AuthDataManagerResolver;
import it.at7.gemini.micronaut.core.Entity;
import it.at7.gemini.micronaut.core.EntityDataManager;
import it.at7.gemini.micronaut.core.EntityDataManagerImpl;
import it.at7.gemini.micronaut.schema.LoadedSchema;
import it.at7.gemini.micronaut.schema.RawSchema;
import it.at7.gemini.micronaut.schema.SchemaLoader;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Requires(property = "gemini.auth.datamanager", value = "custom")
@Requires(property = "gemini.auth.datamanager.driver", value = "mongodb", defaultValue = "mongodb")
public class MongoAuthDataManagerResolver implements AuthDataManagerResolver {

    private Map<String, EntityDataManager> entityDataManagerMap;

    @Value("${gemini.auth.datamanger.schema:gemini_auth_entityschema.yaml}")
    String authSchema;

    @Value("${gemini.auth.datamanager.mongodb.url:}")
    String authUrl;

    @Value("${gemini.auth.datamanager.mongodb.db:}")
    String authDB;

    @Value("${gemini.mongodb.url}")
    String defaultUrl;

    @Value("${gemini.mongodb.db}")
    String defaultDbName;

    @PostConstruct
    void init(SchemaLoader schemaLoader) throws IOException {
        Map<String, Entity> entityMap = new HashMap<>();
        LoadedSchema loaded = schemaLoader.load(authSchema);
        Collection<RawSchema> rawSchemas = loaded.getRawSchemas();
        for (RawSchema rawSchema : rawSchemas) {
            if (rawSchema.type == RawSchema.Type.ENTITY) {
                entityMap.put(rawSchema.entity.name.toUpperCase(), Entity.from(rawSchema));
            }
        }

        String actualUrl = authUrl.isEmpty() ? defaultUrl : authUrl;
        String actualDb = authDB.isEmpty() ? defaultDbName : authDB;
        MongoPersistenceEntityDataManager persistenceEntityDataManager = new MongoPersistenceEntityDataManager(actualUrl, actualDb);
        this.entityDataManagerMap = entityMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e ->
                new EntityDataManagerImpl(persistenceEntityDataManager, e.getValue())
        ));
    }

    @Override
    public EntityDataManager getUserDataManager() {
        return this.entityDataManagerMap.get("USER");
    }

    @Override
    public EntityDataManager getProfileDataManager() {
        return this.entityDataManagerMap.get("PROFILE");
    }

    @Override
    public EntityDataManager getNSBasicAuthDataManager() {
        return this.entityDataManagerMap.get("NSBASICAUTH");
    }
}
