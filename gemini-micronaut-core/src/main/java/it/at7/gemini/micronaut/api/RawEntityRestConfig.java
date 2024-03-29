package it.at7.gemini.micronaut.api;

import io.micronaut.core.annotation.TypeHint;

import java.util.List;

@TypeHint(
        value = {
                RawEntityRestConfig.class,
                RawEntityRestConfig.Type.class,
                RawEntityRestConfig.Config.class,
                RawEntityRestConfig.AllowedMethod.class,
                RawEntityRestConfig.GetListStrategy.class
        },
        accessType = {TypeHint.AccessType.ALL_PUBLIC}
)

public class RawEntityRestConfig {
    public static final String version = "1";

    public Type type;
    public List<String> entities;
    public String entity;
    public Config config;

    public static class Config {
        public List<AllowedMethod> allowedMethods;
        public List<String> allowedOrderingFields;
        public GetListStrategy getListStrategy;
        public Integer defaultLimit;
        public Integer maxWindow; // max start + limit window allowed by the API
        public List<String> defaultOrder;
        // TODO quickFilterAPI
    }


    public enum GetListStrategy {
        ALL, START_LIMIT
    }

    public enum Type {
        MULTIPLE_ENTITIES,
        ENTITY
    }

    public enum AllowedMethod {
        GET_LIST,
        GET_BYID,
        NEW,
        UPDATE,
        DELETE
    }
}
