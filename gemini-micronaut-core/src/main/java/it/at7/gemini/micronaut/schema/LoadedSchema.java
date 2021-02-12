package it.at7.gemini.micronaut.schema;

import java.util.Collection;

public class LoadedSchema {
    private Collection<RawSchema> rawSchemas;
    private String schemaHash;

    public LoadedSchema(Collection<RawSchema> rawSchema, String schemaHash) {
        this.rawSchemas = rawSchema;
        this.schemaHash = schemaHash;
    }

    public Collection<RawSchema> getRawSchemas() {
        return rawSchemas;
    }

    public String getSchemaHash() {
        return schemaHash;
    }
}
