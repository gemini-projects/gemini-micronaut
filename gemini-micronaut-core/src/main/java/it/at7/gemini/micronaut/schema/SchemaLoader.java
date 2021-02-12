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
import java.math.BigInteger;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Prototype
public class SchemaLoader {
    private static Logger logger = LoggerFactory.getLogger(SchemaLoader.class);

    @Value("${gemini.entity.schema.resources:entity_schema.yaml}")
    List<String> entityResources;

    @Inject
    ResourceLoader resourceLoader;

    public LoadedSchema load() throws IOException {
        return load(entityResources);
    }

    public LoadedSchema load(String schemaresource) throws IOException {
        return load(List.of(schemaresource));
    }

    public LoadedSchema load(List<String> entityResources) throws IOException {
        Map<String, RawSchema> result = new HashMap<>();
        List<String> hashedSchema = new ArrayList<>();
        if (entityResources.isEmpty()) {
            throw new RuntimeException("You must provide at least one Schema");
        }

        try {
            for (String entityResource : entityResources) {

                Optional<InputStream> resourceOpt = resourceLoader.getResourceAsStream(entityResource);
                if (resourceOpt.isEmpty()) {
                    throw new RuntimeException(String.format("Unable to load Entity Schema: %s", entityResource));
                }
                logger.info("Loaded Schema: " + entityResource);
                InputStream schemaInputStream = resourceOpt.get();
                MessageDigest md = MessageDigest.getInstance("MD5");

                DigestInputStream dis = new DigestInputStream(schemaInputStream, md);

                Iterable<Object> rawSchemas = new Yaml(new Constructor(RawSchema.class)).loadAll(dis);
                for (Object rawSchemaO : rawSchemas) {
                    assert rawSchemaO instanceof RawSchema;
                    RawSchema rSchema = (RawSchema) rawSchemaO;
                    String entityName = rSchema.entity.name;
                    if (result.containsKey(entityName)) {
                        throw new RuntimeException("Found two Entities with the same name in different schema files: " + entityName);
                    }
                    result.put(entityName, rSchema);
                }

                BigInteger bigInt = new BigInteger(1, md.digest());
                hashedSchema.add(bigInt.toString(16));
            }

            StringBuilder stFinal = new StringBuilder();
            for (String s : hashedSchema) {
                stFinal.append(s);
            }
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(stFinal.toString().getBytes());
            BigInteger bigInt = new BigInteger(1, md.digest());
            String finalHash = bigInt.toString(16);
            return new LoadedSchema(result.values(), finalHash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not found");
        }

    }

    Optional<InputStream> getStream(URL url) {
        try {
            return Optional.of(url.openStream());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

}
