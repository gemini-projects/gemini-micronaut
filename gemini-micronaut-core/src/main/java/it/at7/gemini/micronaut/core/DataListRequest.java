package it.at7.gemini.micronaut.core;

import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DataListRequest {
    private final List<Filter> filters;
    private final List<Order> orders;
    private final int start;
    private final int limit;

    public DataListRequest(List<Filter> filters, List<Order> orders, int start, int limit) {
        this.filters = filters == null ? List.of() : Collections.unmodifiableList(filters);
        this.orders = orders == null ? List.of() : Collections.unmodifiableList(orders);
        this.start = start;
        this.limit = limit;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public int getStart() {
        return start;
    }

    public int getLimit() {
        return limit;
    }

    public static DataListRequest from(HttpRequest httpRequest) {
        HttpParameters parameters = httpRequest.getParameters();
        Builder builder = new Builder();
        for (Map.Entry<String, List<String>> param : parameters.asMap().entrySet()) {
            String key = param.getKey();
            List<String> value = param.getValue();
            String field = key.endsWith("[]") ? key.substring(0, key.length() - 2) : key; // for multi value parameters
            //if (!field.startsWith("_")) {

            if (key.equals("start")) {
                String s = value.get(0);
                int start = Integer.parseInt(s);
                builder.addStart(start);
                continue;
            }

            if (key.equals("limit")) {
                String s = value.get(0);
                int limit = Integer.parseInt(s);
                builder.addLimit(limit);
                continue;
            }

            if (key.equals("orderBy")) {
                String sortField = value.get(0);
                ORDER_TYPE order = ORDER_TYPE.ASC;

                if (sortField.charAt(0) == '-') {
                    sortField = sortField.substring(1);
                    order = ORDER_TYPE.DESC;
                } else if (sortField.charAt(0) == '+') {
                    sortField = sortField.substring(1);
                }
                builder.addOrderBy(sortField, order);
                continue;
            }

            for (String sval : value) {
                OPE_TYPE ope_type = OPE_TYPE.EQUALS;
                // TODO check sval operators
                builder.addFilter(field, ope_type, sval);
                continue;
            }
        }

        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<Filter> filters = new ArrayList<>();
        private int limit = 0;
        private List<Order> orders = new ArrayList<>();
        private int start = 0;

        private Builder() {
        }

        public Builder addFilter(String field, OPE_TYPE ope_type, String sval) {
            filters.add(Filter.of(field, ope_type, sval));
            return this;
        }

        public void addStart(int start) {
            this.start = start;
        }

        public void addLimit(int limit) {
            this.limit = limit;
        }

        public void addOrderBy(String sortField, ORDER_TYPE order) {
            orders.add(Order.of(sortField, order));
        }

        public DataListRequest build() {
            return new DataListRequest(filters, orders, start, limit);
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

    public static class Order {
        private final String fieldName;
        private final ORDER_TYPE type;

        public Order(String fieldName, ORDER_TYPE type) {
            this.fieldName = fieldName;
            this.type = type;
        }

        public String getFieldName() {
            return fieldName;
        }

        public ORDER_TYPE getType() {
            return type;
        }

        public static Order of(String fieldName, ORDER_TYPE operation) {
            return new Order(fieldName, operation);
        }

    }

    public enum OPE_TYPE {
        EQUALS,
        CONTAINS
    }

    public enum ORDER_TYPE {
        ASC,
        DESC
    }
}
