package it.at7.gemini.micronaut.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiHttpResponse {
    private String status;
    private Object data;
    private Object error;
    private Map<String, Object> meta;


    private GeminiHttpResponse() {
    }

    public GeminiHttpResponse(String status, Object data, Object error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public Object getError() {
        return error;
    }

    public GeminiHttpResponse addMeta(String metaProperty, Object value) {
        if (this.meta == null) {
            this.meta = new HashMap<>();
        }
        this.meta.put(metaProperty, value);
        return this;
    }

    public GeminiHttpResponse addElapsedTime(long msTime) {
        addMeta("elapsedTime", msTime + "ms");
        return this;
    }

    public static GeminiHttpResponse success(Object data) {
        return new GeminiHttpResponse("success", data, null);
    }

    public static GeminiHttpResponse error(String reason) {
        return new GeminiHttpResponse("error", null, Map.of("reason", reason));
    }

    public static GeminiHttpResponse error(String reason, @Nullable Map<String, Object> additionalData) {
        Map<String, Object> errorObk = new HashMap<>();
        errorObk.put("reason", reason);
        if (additionalData != null)
            errorObk.putAll(additionalData);
        return new GeminiHttpResponse("error", null, errorObk);
    }
}
