package it.at7.gemini.micronaut.exception;

import it.at7.gemini.micronaut.core.Entity;
import it.at7.gemini.micronaut.core.Field;

public class EntityFieldNotFoundException extends Exception {

    private final String stField;
    private final Entity entity;
    private final Field field;

    public EntityFieldNotFoundException(String stField, Entity entity) {
        super(String.format("Field %s not found for entity %s", stField, entity.getName()));
        this.stField = stField;
        this.entity = entity;
        this.field = null;
    }

    public EntityFieldNotFoundException(Field field, Entity entity) {
        super(String.format("Field %s not found for entity %s", field.toString(), entity.getName()));
        this.field = field;
        this.entity = entity;
        this.stField = field.getName();
    }

    public String getStField() {
        return stField;
    }

    public Field getField() {
        return field;
    }

    public Entity getEntity() {
        return entity;
    }
}
