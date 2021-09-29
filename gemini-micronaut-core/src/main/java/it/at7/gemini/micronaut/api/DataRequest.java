package it.at7.gemini.micronaut.api;

import io.micronaut.core.annotation.Introspected;

import java.util.Map;

@Introspected
public class DataRequest {

    Object data;

    public DataRequest() {
    }

    public DataRequest(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }
}
