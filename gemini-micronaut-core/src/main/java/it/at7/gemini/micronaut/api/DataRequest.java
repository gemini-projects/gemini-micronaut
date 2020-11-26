package it.at7.gemini.micronaut.api;

import java.util.Map;

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
