package it.at7.gemini.micronaut.core;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;
import it.at7.gemini.micronaut.schema.EntitySchema;
import it.at7.gemini.micronaut.schema.LoadedSchema;
import it.at7.gemini.micronaut.schema.RawSchema;
import it.at7.gemini.micronaut.schema.SchemaLoader;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Context
public class EntityManagerImpl implements EntityManager {

    private Map<String, Entity> entityMap;
    private Map<String, RawSchema.Entity> rawSchemaEntities;
    private Map<String, EntityDataManager> entityDataManagerMap;
    private PersistenceEntityDataManager defaultPersistenceManager;
    private List<PersistenceEntityDataManager> customPersistenceManagers;
    private LoadedSchema loadedSchema;

    /* @Value("${gemini.entity.schema.lkSingleRecord:}")
    private String lkSingleRecord; */


    @PostConstruct
    void init(ApplicationContext applicationContext, SchemaLoader schemaLoader) throws IOException {
        /* if (lkSingleRecord != null && !lkSingleRecord.isEmpty())
            Configurations.setLkSingleRecord(lkSingleRecord); */
        this.loadedSchema = schemaLoader.load();
        Collection<RawSchema> rawSchemas = loadedSchema.getRawSchemas();
        this.rawSchemaEntities = rawSchemas.stream().filter(s -> s.type.equals(RawSchema.Type.ENTITY)).collect(Collectors.toMap(s -> Entity.normalizeName(s.entity.name), s -> s.entity));
        this.entityMap = Map.copyOf(buildEntities(rawSchemas));
        this.defaultPersistenceManager = this.getCommonPersistenceManager(applicationContext);
        this.customPersistenceManagers = this.getCustomPersistenceManagers(applicationContext);
        this.entityDataManagerMap = Map.copyOf(buildDataManagers(applicationContext, entityMap));
    }

    @Override
    public Collection<Entity> getEntities() {
        return this.entityMap.values();
    }

    @Override
    public Entity get(String entityName) throws EntityNotFoundException {
        CheckArgument.notEmpty(entityName, "Entity must be a not empty string");
        Entity entity = this.entityMap.get(Entity.normalizeName(entityName));
        if (entity == null)
            throw new EntityNotFoundException(entityName);
        return entity;
    }

    @Override
    public EntitySchema getEntitySchema(String entityName) throws EntityNotFoundException {
        CheckArgument.notEmpty(entityName, "Entity must be a not empty string");
        RawSchema.Entity entity = this.rawSchemaEntities.get(Entity.normalizeName(entityName));
        if (entity == null)
            throw new EntityNotFoundException(entityName);
        return new EntitySchema(this.loadedSchema.getSchemaHash(), entity);
    }

    @Override
    public EntityDataManager getDataManager(Entity entity) {
        return this.entityDataManagerMap.get(entity.getName());
    }

    @Override
    public Map<String, EntityTimes> getEntitiesTimes() {
        Map<String, EntityTimes> entityTimes = new HashMap<>();
        for (PersistenceEntityDataManager customPersistenceManager : customPersistenceManagers) {
            entityTimes.putAll(customPersistenceManager.times());
        }
        Set<String> computed = entityTimes.keySet();
        if (!computed.equals(this.entityMap.keySet()))
            entityTimes.putAll(defaultPersistenceManager.times());
        return entityTimes;
    }

    @Override
    public LoadedSchema getLoadedSchema() {
        return loadedSchema;
    }

    private Map<String, Entity> buildEntities(Iterable<RawSchema> rawSchemas) {
        Map<String, Entity> entityMap = new HashMap<>();
        for (RawSchema rawSchema : rawSchemas) {
            switch (rawSchema.type) {
                case ENTITY:
                    entityMap.put(rawSchema.entity.name.toUpperCase(), Entity.from(rawSchema));
            }
        }
        return entityMap;
    }

    private PersistenceEntityDataManager getCommonPersistenceManager(ApplicationContext applicationContext) {
        Optional<PersistenceEntityDataManager> commonDataManager = applicationContext.findBean(PersistenceEntityDataManager.class, Qualifiers.byStereotype(DefaultEntityDataManager.class));
        if (commonDataManager.isEmpty()) {
            throw new RuntimeException("You must provide a default Entity Data Manager");
        }
        return commonDataManager.get();
    }

    private List<PersistenceEntityDataManager> getCustomPersistenceManagers(ApplicationContext applicationContext) {
        Collection<BeanDefinition<?>> definitions = applicationContext.getBeanDefinitions(Qualifiers.byStereotype(EntityData.class));
        List<PersistenceEntityDataManager> customManagers = new ArrayList<>();

        for (BeanDefinition definition : definitions) {
            PersistenceEntityDataManager dataBean = applicationContext.getBean((BeanDefinition<PersistenceEntityDataManager>) definition);
            customManagers.add(dataBean);
        }
        return customManagers;
    }

    private Map<String, EntityDataManager> buildDataManagers(ApplicationContext applicationContext, Map<String, Entity> entityMap) {
        Collection<BeanDefinition<?>> definitions = applicationContext.getBeanDefinitions(Qualifiers.byStereotype(EntityData.class));
        Map<String, PersistenceEntityDataManager> customManagers = new HashMap<>();
        for (BeanDefinition definition : definitions) {
            PersistenceEntityDataManager dataBean = applicationContext.getBean((BeanDefinition<PersistenceEntityDataManager>) definition);
            AnnotationValue<EntityData> controllerAnn = definition.getAnnotation(EntityData.class);
            Optional<String[]> value = controllerAnn.getValue(String[].class);
            if (value.isEmpty()) {
                throw new RuntimeException("You must provide the name for tne Persistence Entity Data Manager");
            }
            String[] entities = value.get();
            for (String entityName : entities) {
                if (!entityMap.containsKey(entityName.toUpperCase())) {
                    throw new RuntimeException(String.format("Entity %s not found in schema", entityName));
                }
                customManagers.put(entityName.toUpperCase(), dataBean);
            }
        }

        return entityMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e ->
                new EntityDataManagerImpl(customManagers.getOrDefault(e.getKey(), defaultPersistenceManager), e.getValue())
        ));
    }
}
