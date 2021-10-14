package it.at7.gemini.micronaut.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Value;
import it.at7.gemini.micronaut.core.CheckArgument;
import it.at7.gemini.micronaut.core.DataListRequest;
import it.at7.gemini.micronaut.core.Entity;
import it.at7.gemini.micronaut.core.EntityManager;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Context
public class RestEntityManagerImpl implements RestEntityManager {

    @Nullable
    @Value("${gemini.entity.restConfig.resources}")
    List<String> restConfigResources;

    @Value("${gemini.entity.restConfig.defaultGetListStrategy:ALL}")
    RawEntityRestConfig.GetListStrategy defaultGetListStrategy;

    @Value("${gemini.entity.restConfig.defaultAllowedMethods:GET_LIST,GET_BYID,NEW,UPDATE,DELETE}")
    List<RawEntityRestConfig.AllowedMethod> defaultAllowedMethods;

    @Inject
    EntityManager entityManager;

    private LoadedRestConfigs loadedConfigs;
    private RawEntityRestConfig.Config defaultConfig;
    private String configHash;

    @PostConstruct
    void init(ApplicationContext applicationContext, RESTConfigLoader restConfigLoader) throws IOException {
        if (restConfigResources != null)
            loadedConfigs = restConfigLoader.load(restConfigResources);
        defaultConfig = makeDefaultConfig();


        ObjectMapper objectMapper = new ObjectMapper();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (loadedConfigs != null)
                md.update(loadedConfigs.getHash().getBytes());
            md.update(objectMapper.writeValueAsString(defaultConfig).getBytes());
            BigInteger bigInt = new BigInteger(1, md.digest());
            configHash = bigInt.toString(16);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public EntityRestConfig getRestConfiguration(String entityName) {
        RawEntityRestConfig.Config defaultConfig = makeDefaultConfig();
        CheckArgument.notEmpty(entityName, "Entity must be a not empty string");
        if (loadedConfigs != null) {
            RawEntityRestConfig.Config config = loadedConfigs.getConfigs().get(Entity.normalizeName(entityName));
            if (config != null) {
                defaultConfig.allowedMethods = config.allowedMethods == null ? defaultConfig.allowedMethods : config.allowedMethods;
                defaultConfig.getListStrategy = config.getListStrategy == null ? defaultConfig.getListStrategy : config.getListStrategy;
                return new EntityRestConfig(configHash, defaultConfig);
            }
        }
        return new EntityRestConfig(configHash, defaultConfig);
    }

    @Override
    public String getConfigHash() {
        return configHash;
    }


    private RawEntityRestConfig.Config makeDefaultConfig() {
        RawEntityRestConfig.Config config = new RawEntityRestConfig.Config();
        config.getListStrategy = defaultGetListStrategy;
        config.allowedMethods = defaultAllowedMethods;
        return config;
    }
}
