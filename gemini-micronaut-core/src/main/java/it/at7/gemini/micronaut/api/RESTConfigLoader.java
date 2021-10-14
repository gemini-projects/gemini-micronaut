package it.at7.gemini.micronaut.api;

import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.io.ResourceLoader;
import it.at7.gemini.micronaut.core.Entity;
import it.at7.gemini.micronaut.schema.LoadedSchema;
import it.at7.gemini.micronaut.schema.RawSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.Null;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Prototype
public class RESTConfigLoader {
    private static Logger logger = LoggerFactory.getLogger(RESTConfigLoader.class);

    @Inject
    ResourceLoader resourceLoader;

    @Nullable
    public LoadedRestConfigs load(String restConfigResource) throws IOException {
        return load(List.of(restConfigResource));
    }

    @Nullable
    public LoadedRestConfigs load(List<String> restConfigResource) {
        Map<String, RawEntityRestConfig.Config> result = new HashMap<>();
        List<String> hashedConfig = new ArrayList<>();
        if (restConfigResource.isEmpty()) {
            logger.warn("No REST Entity Configuration provided");
            return null;
        }

        try {
            for (String entityRestResource : restConfigResource) {

                // step 1 try to get the file, if not found try to get the resource

                InputStream schemaInputStream;
                try {
                    schemaInputStream = new FileInputStream(entityRestResource);
                } catch (FileNotFoundException fnot) {
                    logger.info(fnot.getMessage());
                    Optional<InputStream> resourceOpt = resourceLoader.getResourceAsStream(entityRestResource);
                    if (resourceOpt.isEmpty()) {
                        throw new RuntimeException(String.format("Unable to load Entity REST Configuration from File or Resource: %s", entityRestResource));
                    }
                    schemaInputStream = resourceOpt.get();
                }

                logger.info("Loaded REST Configuration: " + entityRestResource);

                MessageDigest md = MessageDigest.getInstance("MD5");

                DigestInputStream dis = new DigestInputStream(schemaInputStream, md);

                Iterable<Object> rawRestConfigs = new Yaml(new Constructor(RawEntityRestConfig.class)).loadAll(dis);
                for (Object rawConfig : rawRestConfigs) {
                    assert rawConfig instanceof RawEntityRestConfig;
                    RawEntityRestConfig rConfig = (RawEntityRestConfig) rawConfig;

                    List<String> entities = List.of();
                    switch (rConfig.type) {
                        case MULTIPLE_ENTITIES:
                            entities = rConfig.entities;
                            break;
                        case ENTITY:
                            entities = List.of(rConfig.entity);
                            break;
                    }

                    for (String entity : entities) {
                        String normalizedEntity = Entity.normalizeName(entity);
                        if (result.containsKey(normalizedEntity)) {
                            throw new RuntimeException("Found two Configuration for the Entity: " + entity);
                        }
                        result.put(normalizedEntity, rConfig.config);
                    }

                }
                md.update(RawEntityRestConfig.version.getBytes());
                BigInteger bigInt = new BigInteger(1, md.digest());
                hashedConfig.add(bigInt.toString(16));
            }

            StringBuilder stFinal = new StringBuilder(RawEntityRestConfig.version);
            for (String s : hashedConfig) {
                stFinal.append(s);
            }
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(stFinal.toString().getBytes());
            BigInteger bigInt = new BigInteger(1, md.digest());
            String finalHash = bigInt.toString(16);
            return new LoadedRestConfigs(result, finalHash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not found");
        }

    }
}
