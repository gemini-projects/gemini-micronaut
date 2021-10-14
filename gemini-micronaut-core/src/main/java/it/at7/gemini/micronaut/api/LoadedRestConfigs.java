package it.at7.gemini.micronaut.api;

import java.util.Map;

public class LoadedRestConfigs {
    private final Map<String, RawEntityRestConfig.Config> configs;
    private String hash;

    public LoadedRestConfigs(Map<String, RawEntityRestConfig.Config> configs, String finalHash) {
        this.configs = configs;
        this.hash = finalHash;
    }

    public Map<String, RawEntityRestConfig.Config> getConfigs() {
        return configs;
    }

    public String getHash() {
        return hash;
    }
}
