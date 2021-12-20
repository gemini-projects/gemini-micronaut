package it.at7.gemini.micronaut.core;

import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;

import java.util.*;

public class DataListRequest {
    private final List<Filter> filters;
    private final List<Order> orders;
    private final int start;
    private final int limit;
    private final String quickFilter;
    private final List<String> quickFilterFields;

    public DataListRequest(List<Filter> filters, List<Order> orders, int start, int limit, String quickFilter, List<String> quickFilterFields) {
        this.filters = filters == null ? List.of() : Collections.unmodifiableList(filters);
        this.orders = orders == null ? List.of() : Collections.unmodifiableList(orders);
        this.start = start;
        this.limit = limit;
        this.quickFilter = quickFilter;
        this.quickFilterFields = quickFilterFields;
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

    public Optional<String> getQuickFilter() {
        return Optional.ofNullable(quickFilter);
    }

    public List<String> getQuickFilterFields() {
        return quickFilterFields;
    }

    public static ExtractedOperation extractFieldAndOperator(String key) {
        String field = key;
        OPE_TYPE ope_type = OPE_TYPE.EQUALS;
        if (key.endsWith("]")) {
            StringBuilder ope = new StringBuilder();
            int i;
            for (i = key.length() - 2; i >= 0; i--) {
                char c = key.charAt(i);
                if (c == '[') {
                    break;
                }
                ope.append(c);
            }
            field = key.substring(0, i);
            String opeSt = ope.reverse().toString();
            if (!opeSt.isEmpty()) {
                // TODO maybe arrays ?
                try {
                    ope_type = OPE_TYPE.valueOf(opeSt.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // do nothing, ignore the filter if wrong
                    if (opeSt.equals("!") || opeSt.toUpperCase().equals("NOT"))
                        ope_type = OPE_TYPE.NOT_EQUALS;
                }
            }
        }
        return ExtractedOperation.from(field, ope_type);
    }

    public static DataListRequest from(HttpRequest httpRequest) {
        HttpParameters parameters = httpRequest.getParameters();
        Builder builder = new Builder();
        for (Map.Entry<String, List<String>> param : parameters.asMap().entrySet()) {
            String key = param.getKey();
            List<String> value = param.getValue();
            ExtractedOperation extractedOperation = extractFieldAndOperator(key);
            String field = extractedOperation.field;
            OPE_TYPE ope_type = extractedOperation.ope_type;

            if (field.equals("quickFilter")) {
                String filter = value.get(0);
                builder.addQuickFilter(filter);
                continue;
            }

            if (field.equals("quickFilterFields")) {
                builder.addQuickFilterField(value.get(0));
                continue;
            }

            if (field.equals("start")) {
                String s = value.get(0);
                int start = Integer.parseInt(s);
                builder.addStart(start);
                continue;
            }

            if (field.equals("limit")) {
                String s = value.get(0);
                int limit = Integer.parseInt(s);
                builder.addLimit(limit);
                continue;
            }

            if (field.equals("orderBy")) {
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
                builder.addFilter(field, ope_type, sval);
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
        private String quickFilter = null;
        private List<String> quickFilterFields = new ArrayList<>();

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

        public void addQuickFilter(String filter) {
            this.quickFilter = filter;
        }

        public void addQuickFilterField(String s) {
            this.quickFilterFields.add(s);
        }

        public DataListRequest build() {
            return new DataListRequest(filters, orders, start, limit, quickFilter, quickFilterFields);
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

    private static class ExtractedOperation {
        public String field;
        public OPE_TYPE ope_type;

        public static ExtractedOperation from(String field, OPE_TYPE ope_type) {
            ExtractedOperation extractedOperation = new ExtractedOperation();
            extractedOperation.field = field;
            extractedOperation.ope_type = ope_type;
            return extractedOperation;
        }
    }

    public enum OPE_TYPE {
        EQUALS,
        NOT_EQUALS,
        CONTAINS,
        NOT_CONTAINS,
        GTE,
        GT,
        LTE,
        LT
    }

    public enum ORDER_TYPE {
        ASC,
        DESC
    }
}
