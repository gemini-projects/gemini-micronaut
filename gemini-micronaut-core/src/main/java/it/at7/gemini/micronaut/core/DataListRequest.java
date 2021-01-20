package it.at7.gemini.micronaut.core;

import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DataListRequest {
    private final List<Filter> filters;

    public DataListRequest(List<Filter> filters) {
        this.filters = filters == null ? List.of() : Collections.unmodifiableList(filters);
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public static DataListRequest from(HttpRequest httpRequest) {
        HttpParameters parameters = httpRequest.getParameters();
        Builder builder = new Builder();
        for (Map.Entry<String, List<String>> param : parameters.asMap().entrySet()) {
            String key = param.getKey();
            List<String> value = param.getValue();
            String field = key.endsWith("[]") ? key.substring(0, key.length() - 2) : key;
            if (!field.startsWith("_")) {
                for (String sval : value) {
                    OPE_TYPE ope_type = OPE_TYPE.EQUALS;
                    // TODO check sval operators
                    builder.addFilter(field, ope_type, sval);
                }
            }
            // TODO handle pagination and sorting
        }

        return builder.build();
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private List<Filter> filters = new ArrayList<>();

        private Builder(){}

        public Builder addFilter(String field, OPE_TYPE ope_type, String sval) {
            filters.add(Filter.of(field, ope_type, sval));
            return this;
        }

        public DataListRequest build() {
            return new DataListRequest(filters);
        }
    }

    public static class Filter {
        final private String fieldName;
        final private OPE_TYPE operation;
        final private Object value;

        public Filter(String fieldName, OPE_TYPE operation, Object value) {
            this.fieldName = fieldName;
            this.operation = operation;
            this.value = value;
        }

        public String getFieldName() {
            return fieldName;
        }

        public OPE_TYPE getOperation() {
            return operation;
        }

        public Object getValue() {
            return value;
        }

        public static Filter of(String fieldName, OPE_TYPE operation, Object value) {
            return new Filter(fieldName, operation, value);
        }
    }

    public enum OPE_TYPE {
        EQUALS,
        CONTAINS
    }
}
