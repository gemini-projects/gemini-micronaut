package it.at7.gemini.micronaut.schema;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class EntitySchema {
    private final RawSchema.Entity value;
    private String hash;

    public EntitySchema(String schemaHash, RawSchema.Entity entity) {
        this.hash = schemaHash;
        this.value = entity;
    }

    public RawSchema.Entity getValue() {
        return value;
    }

    public String getHash() {
        return hash;
    }
}
