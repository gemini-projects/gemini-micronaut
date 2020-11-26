package it.at7.gemini.micronaut.schema;

import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.io.ResourceLoader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Prototype
public class SchemaLoader {

    @Value("${gemini.entity.schema.resource:entity_schema.yaml}")
    String entityResource;

    @Inject
    ResourceLoader resourceLoader;

    public Collection<RawSchema> load() {
        Optional<InputStream> resourceOpt = resourceLoader.getResourceAsStream(entityResource);
        if (resourceOpt.isEmpty()) {
            throw new RuntimeException(String.format("Unable to load Entity Schema: %s", entityResource));
        }
        InputStream schemaInputStream = resourceOpt.get();
        List<RawSchema> result = new ArrayList<>();
        Iterable<Object> rawSchemas = new Yaml(new Constructor(RawSchema.class)).loadAll(schemaInputStream);
        for (Object rawSchemaO : rawSchemas) {
            assert rawSchemaO instanceof RawSchema;
            result.add((RawSchema) rawSchemaO);
        }
        return result;
    }

}
