package it.at7.gemini.micronaut.schema;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class EntitySchema {
    private String schemaHash;
    private RawSchema.Entity entity;

    public EntitySchema(String schemaHash, RawSchema.Entity entity) {
        this.schemaHash = schemaHash;
        this.entity = entity;
    }

    public String getSchemaHash() {
        return schemaHash;
    }

    public RawSchema.Entity getEntity() {
        return entity;
    }
}
