package it.at7.gemini.micronaut.api;

import io.micronaut.core.annotation.Introspected;

import java.util.Map;

@Introspected
public class DataRequest {

    Map<String, Object> data;

    public DataRequest() {
    }

    public DataRequest(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
