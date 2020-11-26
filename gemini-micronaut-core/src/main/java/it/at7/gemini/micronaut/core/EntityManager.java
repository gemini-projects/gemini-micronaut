package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.EntityNotFoundException;
import it.at7.gemini.micronaut.schema.RawSchema;

import java.util.Collection;

/**
 * Service to work with Gemini Entities
 */
public interface EntityManager {

    /**
     * Return the {@link Entity} given its key name (case insensitive)
     * @param entityName the target entity name
     * @return Entity object
     * @throws EntityNotFoundException if entity is not found
     */
    Entity get(String entityName) throws EntityNotFoundException;


    /**
     * Get all the managed entities
     * @return entities
     */
    Collection<Entity> getEntities();

    default EntityDataManager getDataManager(String entityName) throws EntityNotFoundException {
        return getDataManager(get(entityName));
    }

    EntityDataManager getDataManager(Entity entity);

    RawSchema.Entity getEntitySchema(String entityName) throws EntityNotFoundException;
}
