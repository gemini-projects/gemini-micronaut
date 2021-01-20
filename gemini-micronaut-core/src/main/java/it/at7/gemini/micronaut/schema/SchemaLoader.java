package it.at7.gemini.micronaut.schema;

import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.io.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Prototype
public class SchemaLoader {
    private static Logger logger = LoggerFactory.getLogger(SchemaLoader.class);

    @Value("${gemini.entity.schema.resources:entity_schema.yaml}")
    List<String> entityResources;

    @Inject
    ResourceLoader resourceLoader;

    public Collection<RawSchema> load() {
        return load(entityResources);
    }

    public Collection<RawSchema> load(String schemaresource) {
        return load(List.of(schemaresource));
    }

    public Collection<RawSchema> load(List<String> entityResources) {
        Map<String, RawSchema> result = new HashMap<>();

        if (entityResources.isEmpty()) {
            throw new RuntimeException("You must provide at least one Schema");
        }

        for (String entityResource : entityResources) {

            Optional<InputStream> resourceOpt = resourceLoader.getResourceAsStream(entityResource);
            if (resourceOpt.isEmpty()) {
                throw new RuntimeException(String.format("Unable to load Entity Schema: %s", entityResource));
            }
            logger.info("Loaded Schema: " + entityResource);
            InputStream schemaInputStream = resourceOpt.get();
            Iterable<Object> rawSchemas = new Yaml(new Constructor(RawSchema.class)).loadAll(schemaInputStream);
            for (Object rawSchemaO : rawSchemas) {
                assert rawSchemaO instanceof RawSchema;
                RawSchema rSchema = (RawSchema) rawSchemaO;
                String entityName = rSchema.entity.name;
                if (result.containsKey(entityName)) {
                    throw new RuntimeException("Found two Entities with the same name in different schema files: " + entityName);
                }
                result.put(entityName, rSchema);
            }
        }
        return result.values();
    }

    Optional<InputStream> getStream(URL url) {
        try {
            return Optional.of(url.openStream());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

}
