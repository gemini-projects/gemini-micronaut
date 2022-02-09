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
        DataListRequest.Builder builder = DataListRequest.builder(listRequest);
        if (value.getListStrategy.equals(RawEntityRestConfig.GetListStrategy.START_LIMIT)) {
            if (listRequest.getLimit() == 0)
                builder.addLimit(value.defaultLimit);
            if (value.maxWindow != null && listRequest.getStart() > value.maxWindow)
                throw new RuntimeException("start > maxWindow");
        }
        return builder.build();
    }
}
