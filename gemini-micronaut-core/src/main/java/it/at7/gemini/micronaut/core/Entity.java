package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.EntityFieldNotFoundException;
import it.at7.gemini.micronaut.schema.RawSchema;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class Entity {
    private final String name;
    private final List<String> lk;
    private final String lkSeparator;
    private final Map<String, Field> fields;
    private final boolean singleRecord;
    private final String lkSingleRecValue;
    private final boolean tree;
    private final String treeParentField;

    private Entity(String name, List<String> lk, String lkSeparator, Map<String, Field> fields, boolean singleRecord, String lkSingleRecValue, boolean tree, String treeParentField) {
        CheckArgument.notEmpty(name, "name required");
        CheckArgument.notEmpty(fields, "fields required");
        if (!singleRecord) {
            CheckArgument.notEmpty(lk, "This entity " + name + " must have at least one logical key field");
            lk.forEach(l -> CheckArgument.isTrue(fields.containsKey(normalizeFieldName(l)), "Fields must have " + l));
        }
        if (singleRecord) {
            CheckArgument.notEmpty(lkSingleRecValue, "Single Record Entity must have logical key value");
        }
        if (tree) {
            CheckArgument.notEmpty(treeParentField, "Tree Entity must require a parent field");
            CheckArgument.isTrue(lk.size() == 1, "Tree Entity must have a single logical key");
            CheckArgument.isTrue(fields.get(normalizeFieldName(treeParentField)).getType().equals(fields.get(normalizeFieldName(lk.get(0))).getType()), "Parent and lk types must be the same");
            CheckArgument.isTrue(List.of(Field.Type.STRING, Field.Type.INTEGER).contains(fields.get(normalizeFieldName(lk.get(0))).getType()), "Lk and parent type must be STRING or INTEGER");
        }
        this.name = name;
        this.lk = singleRecord ? List.of() : List.copyOf(lk);
        this.lkSeparator = lkSeparator;
        this.fields = Map.copyOf(fields);
        this.singleRecord = singleRecord;
        this.lkSingleRecValue = lkSingleRecValue;
        this.tree = tree;
        this.treeParentField = treeParentField;
    }

    public String getName() {
        return name;
    }

    public String getLkSeparator() {
        return lkSeparator;
    }

    public List<Field> getLkFields() {
        return this.lk.stream().map(f -> this.fields.get(normalizeFieldName(f))).collect(Collectors.toList());
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public boolean isSingleRecord() {
        return singleRecord;
    }

    public String getLkSingleRecValue() {
        return lkSingleRecValue;
    }

    public boolean isTree() {
        return tree;
    }

    public String getTreeParentField() {
        return treeParentField;
    }

    static Entity from(RawSchema rawSchema) {
        RawSchema.Entity entity = rawSchema.entity;

        Builder builder = new Builder(entity.name);
        for (RawSchema.Entity.Field field : entity.fields) {
            builder.addField(field);
        }
        return builder.setLk(entity.lk)
                .setLKSeparator(rawSchema.entity.lkSeparator)
                .setSingleRecord(rawSchema.entity.singleRecord, rawSchema.entity.lkValue)
                .setTree(rawSchema.entity.tree)
                .build();
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

    public List<Field> getFieldsFromPath(String fieldName) throws EntityFieldNotFoundException {
        String[] splitted = fieldName.split("\\.");
        List<Field> ret = new ArrayList<>();
        Field lastField = getField(splitted[0]);
        ret.add(lastField);
        for (int i = 1; i < splitted.length; i++) {
            switch (lastField.getType()) {
                case OBJECT:
                    lastField = getField(lastField.getInnerFields(), splitted[i]);
                    ret.add(lastField);
                    break;
            }
        }
        return ret;
    }

    public static class Builder {

        private String name;
        private List<String> lk;
        private String lkSeparator;
        private Map<String, Field> fields = new HashMap<>();
        private boolean singleRecord = false;
        private String lkSingleRecValue;
        private boolean tree = false;
        private String treeParentField;

        public Builder(String name) {
            this.name = normalizeName(name);
        }

        private Builder setLk(List<String> lk) {
            if (lk != null)
                this.lk = lk.stream().map(Entity::normalizeFieldName).collect(Collectors.toList());
            return this;
        }

        private Builder addField(RawSchema.Entity.Field field) {
            Field entityField = Field.from(field);
            this.fields.put(normalizeFieldName(entityField.getName()), entityField);
            return this;
        }

        public Builder setLKSeparator(String lkSeparator) {
            this.lkSeparator = lkSeparator == null ? "" : lkSeparator;
            return this;
        }

        public Builder setSingleRecord(boolean singleRecord, String lkValue) {
            this.singleRecord = singleRecord;
            this.lkSingleRecValue = lkValue != null && !lkValue.isEmpty() ? lkValue : Configurations.getLkSingleRecord();
            return this;
        }

        public Builder setTree(RawSchema.Entity.Tree tree) {
            if (tree != null) {
                this.tree = tree.enabled;
                this.treeParentField = tree.parentField == null ? "parent" : tree.parentField;
            }
            return this;
        }

        public Entity build() {
            return new Entity(name, lk, lkSeparator, fields, singleRecord, lkSingleRecValue, tree, treeParentField);
        }
    }

    private Field getField(Map<String, Field> fields, String field) throws EntityFieldNotFoundException {
        CheckArgument.notEmpty(field, "Entity: please provide a valid field");
        Field f = fields.get(normalizeFieldName(field));
        if (f == null)
            throw new EntityFieldNotFoundException(field, this);
        return f;
    }


    public static String normalizeName(@NotNull String entityName) {
        return entityName.toUpperCase();
    }

    private static String normalizeFieldName(@NotNull String fieldName) {
        return fieldName.toLowerCase();
    }
}
