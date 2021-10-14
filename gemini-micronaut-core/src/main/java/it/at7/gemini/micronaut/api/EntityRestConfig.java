package it.at7.gemini.micronaut.api;

import io.micronaut.core.annotation.Introspected;
import it.at7.gemini.micronaut.core.DataListRequest;

@Introspected
public class EntityRestConfig {
    private final String hash;
    private final RawEntityRestConfig.Config value;

    public EntityRestConfig(String hash, RawEntityRestConfig.Config config) {
        this.hash = hash;
        this.value = config;
    }

    public String getHash() {
        return hash;
    }

    public RawEntityRestConfig.Config getValue() {
        return value;
    }

    public DataListRequest checkAndValidate(DataListRequest listRequest) {
        // TODO update pagination limit and so on with RestCOnfiguration for the entity
        return listRequest;
    }
}
