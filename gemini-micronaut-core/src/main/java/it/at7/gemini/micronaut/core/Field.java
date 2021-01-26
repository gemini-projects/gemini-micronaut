package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.schema.RawSchema;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class Field {
    private final String name;
    private final Type type;
    private final List<String> enums;
    private final Map<String, Field> innerFields;
    private final Type arrayType;
    private final boolean required;
    private final int arrayDept;

    private Field(String name, Type type, boolean required, List<String> enums, List<Field> innerFields, Type arrayType, int arrayDept) {
        CheckArgument.notEmpty(name, "field name required");
        CheckArgument.isNotNull(type, "type name required");
        this.name = name;
        this.type = type;
        this.arrayType = arrayType;
        this.required = required;
        this.arrayDept = arrayDept;
        if (type.equals(Type.ENUM) || type.equals(Type.SELECT)) {
            CheckArgument.notEmpty(enums, "enums required for ENUM type");
        }
        if (type.equals(Type.OBJECT)) {
            CheckArgument.notEmpty(innerFields, "innerFields required for OBJECT type");

        }
        if (type.equals(Type.ARRAY)) {
            CheckArgument.isNotNull(arrayType, "arrayType required for ARRAY type");
            CheckArgument.isTrue(arrayDept > 0, "arrayDept must be > 0");
            if (arrayType.equals(Type.ENUM)) {
                CheckArgument.notEmpty(enums, "enums required for ENUM type");
            }
            if (arrayType.equals(Type.OBJECT)) {
                CheckArgument.notEmpty(innerFields, "innerFields required for OBJECT type");
            }
        }
        this.enums = enums != null ? List.copyOf(enums) : List.of();
        this.innerFields = innerFields != null ? innerFields.stream().collect(Collectors.toMap(Field::getName, f -> f)) : Map.of();
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public List<String> getEnums() {
        return enums;
    }

    public Map<String, Field> getInnerFields() {
        return innerFields;
    }

    public Type getArrayType() {
        return arrayType;
    }

    public int getArrayDept() {
        return arrayDept;
    }

    public static Field from(RawSchema.Entity.Field field) {
        Builder builder = new Builder(field.name);
        builder.required(field.required);
        switch (Type.valueOf(field.type.name())) {
            case STRING:
                builder.stringType();
                break;
            case INTEGER:
                builder.integerType();
                break;
            case DECIMAL:
                builder.decimalType();
                break;
            case DOUBLE:
                builder.doubleType();
                break;
            case BOOL:
                builder.boolType();
                break;
            case ENUM:
                builder.enumType(field.enums);
                break;
            case OBJECT:
                builder.objectType(field.object.fields.stream().map(Field::from).collect(Collectors.toList()));
                break;
            case ARRAY:
                builder.arrayType(field.array);
                break;
            case DICTIONARY:
                builder.dictionaryType(field.dict.fields.stream().map(Field::from).collect(Collectors.toList()));
                break;
            case SELECT:
                builder.selectType(field.select);
                break;
            case B64_IMAGE:
                builder.b64ImageType();
                break;
            default:
                throw new RuntimeException(String.format("Raw Field %s not convertible", field.name));
        }
        return builder.build();
    }

    public Optional<ValidationError> validate(Object value) {
        if (!validateRequired(value)) {
            return Optional.of(ValidationError.required());
        }
        if (type.equals(Type.OBJECT) && value != null) {
            Map<String, Object> cv = (Map<String, Object>) value;
            Map<String, ValidationError> ve = new HashMap<>();
            for (Field field : this.innerFields.values()) {
                field.validate(cv.get(field.getName())).ifPresent(e -> ve.put(field.getName(), e));
            }
            if (ve.isEmpty())
                return Optional.empty();
            return Optional.of(ValidationError.objectError(ve));

        }
        return Optional.empty();
    }

    private boolean validateRequired(Object value) {
        if (required) {
            switch (type) {
                case STRING:
                    return value != null && !((String) value).isEmpty();
                case BOOL:
                case OBJECT:
                case ENUM:
                    return value != null;
            }
        }
        return true;
    }

    public Map<String, Object> toJsonMap() {
        return null;
    }

    public static class ValidationError {
        final ErrorType error;
        final Map<String, ValidationError> object;

        public ValidationError(ErrorType type, Map<String, ValidationError> objectErrors) {
            error = type;
            object = Map.copyOf(objectErrors);
        }

        public enum ErrorType {
            REQUIRED, OBJECTERROR
        }

        public ErrorType getError() {
            return error;
        }

        @Nullable
        public Map<String, ValidationError> getObject() {
            return object;
        }

        public static ValidationError objectError(Map<String, ValidationError> ve) {
            return new ValidationError(ErrorType.OBJECTERROR, ve);
        }

        static ValidationError required() {
            return new ValidationError(ErrorType.REQUIRED, Map.of());
        }
    }

    public static class Builder {
        private String name;
        private Type type;
        private List<String> enums;
        private List<Field> innerFields;
        private boolean required;
        private Type arrayType;
        private int arrayDept = 0;

        public Builder(String name) {
            this.name = name;
        }

        public Builder stringType() {
            this.type = Type.STRING;
            return this;
        }

        public Builder integerType() {
            this.type = Type.INTEGER;
            return this;
        }

        public Builder doubleType() {
            this.type = Type.DOUBLE;
            return this;
        }

        public Builder boolType() {
            this.type = Type.BOOL;
            return this;
        }

        public Builder decimalType() {
            this.type = Type.DECIMAL;
            return this;
        }

        public Builder enumType(List<String> enums) {
            this.type = Type.ENUM;
            this.enums = enums.stream().map(String::toUpperCase).collect(Collectors.toList());
            return this;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder objectType(List<Field> innerFields) {
            this.type = Type.OBJECT;
            this.innerFields = innerFields;
            return this;
        }

        public Builder dictionaryType(List<Field> innerFields) {
            this.type = Type.DICTIONARY;
            this.innerFields = innerFields;
            return this;
        }

        public Builder arrayType(RawSchema.Entity.Field.ArrayType array) {
            this.arrayDept += 1;
            this.type = Type.ARRAY;
            this.arrayType = Field.Type.valueOf(array.type.name());
            switch (array.type) {
                case OBJECT:
                    this.innerFields = array.object.fields.stream().map(Field::from).collect(Collectors.toList());
                    break;
                case ENUM:
                    this.enums = array.enums.stream().map(String::toUpperCase).collect(Collectors.toList());
                    break;
                case SELECT:
                    this.enums = array.select.elems.stream().map(f -> f.value).collect(Collectors.toList());
                    break;
                case ARRAY:
                    arrayType(array.array);
                    break;
            }
            return this;
        }

        public Builder selectType(RawSchema.Entity.Field.Select select) {
            this.type = Type.SELECT;
            this.enums = select.elems.stream().map(f -> f.value).collect(Collectors.toList());
            return this;
        }

        public Builder b64ImageType() {
            this.type = Type.B64_IMAGE;
            return this;
        }

        public Field build() {
            return new Field(name, type, required, enums, innerFields, arrayType, arrayDept);
        }
    }

    public static String normalizeName(@NotNull String fieldName) {
        return fieldName.toLowerCase();
    }

    public enum Type {
        STRING, INTEGER, DECIMAL, DOUBLE, BOOL, OBJECT, ENUM, ARRAY, DICTIONARY, SELECT, B64_IMAGE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return Objects.equals(name, field.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
