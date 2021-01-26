package it.at7.gemini.micronaut.core;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;
import it.at7.gemini.micronaut.schema.RawSchema;
import it.at7.gemini.micronaut.schema.SchemaLoader;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Context
public class EntityManagerImpl implements EntityManager {

    private Map<String, Entity> entityMap;
    private Map<String, RawSchema.Entity> rawSchemaEntities;
    private Map<String, EntityDataManager> entityDataManagerMap;

    /* @Value("${gemini.entity.schema.lkSingleRecord:}")
    private String lkSingleRecord; */


    @PostConstruct
    void init(ApplicationContext applicationContext, SchemaLoader schemaLoader) throws FileNotFoundException {
        /* if (lkSingleRecord != null && !lkSingleRecord.isEmpty())
            Configurations.setLkSingleRecord(lkSingleRecord); */
        Collection<RawSchema> rawSchemas = schemaLoader.load();
        this.rawSchemaEntities = rawSchemas.stream().filter(s -> s.type.equals(RawSchema.Type.ENTITY)).collect(Collectors.toMap(s -> Entity.normalizeName(s.entity.name), s -> s.entity));
        this.entityMap = Map.copyOf(buildEntities(rawSchemas));
        this.entityDataManagerMap = Map.copyOf(buildDataManagers(applicationContext, this.entityMap));
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
    public RawSchema.Entity getEntitySchema(String entityName) throws EntityNotFoundException {
        CheckArgument.notEmpty(entityName, "Entity must be a not empty string");
        RawSchema.Entity entity = this.rawSchemaEntities.get(Entity.normalizeName(entityName));
        if (entity == null)
            throw new EntityNotFoundException(entityName);
        return entity;
    }

    @Override
    public EntityDataManager getDataManager(Entity entity) {
        return this.entityDataManagerMap.get(entity.getName());
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

    private Map<String, EntityDataManager> buildDataManagers(ApplicationContext applicationContext, Map<String, Entity> entityMap) {
        Optional<PersistenceEntityDataManager> commonDataManager = applicationContext.findBean(PersistenceEntityDataManager.class, Qualifiers.byStereotype(DefaultEntityDataManager.class));
        if (commonDataManager.isEmpty()) {
            throw new RuntimeException("You must provide a default Entity Data Manager");
        }
        PersistenceEntityDataManager defaultPersistenceEntityDataManager = commonDataManager.get();
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
                new EntityDataManagerImpl(customManagers.getOrDefault(e.getKey(), defaultPersistenceEntityDataManager), e.getValue())
        ));
    }
}
