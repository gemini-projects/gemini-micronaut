package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.EntityFieldNotFoundException;
import it.at7.gemini.micronaut.schema.RawSchema;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Entity {
    private String name;
    private final List<String> lk;
    private final Map<String, Field> fields;

    private Entity(String name, List<String> lk, Map<String, Field> fields) {
        CheckArgument.notEmpty(name, "name required");
        CheckArgument.notEmpty(fields, "fields required");
        lk.forEach(l -> CheckArgument.isTrue(fields.containsKey(normalizeFieldName(l)), "Fields must have " + l));
        this.name = name;
        this.lk = List.copyOf(lk);
        this.fields = Map.copyOf(fields);
    }

    public String getName() {
        return name;
    }

    public List<Field> getLkFields() {
        return this.lk.stream().map(f -> this.fields.get(normalizeFieldName(f))).collect(Collectors.toList());
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    static Entity from(RawSchema rawSchema) {
        RawSchema.Entity entity = rawSchema.entity;

        Builder builder = new Builder(entity.name);
        for (RawSchema.Entity.Field field : entity.fields) {
            builder.addField(field);
        }
        builder.setLk(entity.lk);
        return builder.build();
    }

    public Field getField(String field) throws EntityFieldNotFoundException {
        CheckArgument.notEmpty(field, "Entity: please provide a valid field");
        Field f = this.fields.get(normalizeFieldName(field));
        if (f == null)
            throw new EntityFieldNotFoundException(field, this);
        return f;
    }

    public Field getField(Field field) throws EntityFieldNotFoundException {
        Optional<Field> fieldOpt = getFieldOpt(field);
        if (fieldOpt.isEmpty())
            throw new EntityFieldNotFoundException(field, this);
        return fieldOpt.get();
    }

    public Optional<Field> getFieldOpt(Field field) {
        CheckArgument.isNotNull(field, "Entity: please provide a valid field");
        return this.fields.values().stream().filter(f -> f.equals(field)).findFirst();
    }

    public Optional<Field> getFieldOpt(String field) {
        CheckArgument.notEmpty(field, "Entity: please provide a valid field");
        return Optional.ofNullable(this.fields.get(field.toLowerCase()));
    }

    public boolean isLk(Field ft) {
        return this.lk.contains(ft.getName());
    }

    public boolean isLk(String field) {
        return this.lk.contains(normalizeFieldName(field));
    }

    public static class Builder {

        private String name;
        private List<String> lk;
        private Map<String, Field> fields = new HashMap<>();

        public Builder(String name) {
            this.name = normalizeName(name);
        }

        private void setLk(List<String> lk) {
            this.lk = lk.stream().map(Entity::normalizeFieldName).collect(Collectors.toList());
        }

        private void addField(RawSchema.Entity.Field field) {
            Field entityField = Field.from(field);
            this.fields.put(normalizeFieldName(entityField.getName()), entityField);
        }

        public Entity build() {
            return new Entity(name, lk, fields);
        }
    }

    public static String normalizeName(@NotNull String entityName) {
        return entityName.toUpperCase();
    }

    private static String normalizeFieldName(@NotNull String fieldName) {
        return fieldName.toLowerCase();
    }
}
