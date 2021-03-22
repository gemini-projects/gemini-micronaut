package it.at7.gemini.micronaut.schema;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.TypeHint;

import java.util.List;

@TypeHint(
        value = {
                RawSchema.class,
                RawSchema.Type.class,
                RawSchema.Entity.class,
                RawSchema.Entity.Tree.class,
                RawSchema.Entity.Field.class,


                RawSchema.Entity.Field.ObjectType.class,
                RawSchema.Entity.Field.Decimal.class,
                RawSchema.Entity.Field.AnyType.class,
                RawSchema.Entity.Field.ArrayType.class,
                RawSchema.Entity.Field.ObjectType.class,
                RawSchema.Entity.Field.Dictionary.class,
                RawSchema.Entity.Field.Select.class,
                RawSchema.Entity.Field.Select.SelectElem.class,
                RawSchema.Entity.Field.Type.class,
        },
        accessType = {TypeHint.AccessType.ALL_PUBLIC}
)
@Introspected
public class RawSchema {
    public Type type;
    public Entity entity;

    @Introspected
    public static class Entity {
        public String name;
        public List<String> lk;
        public String lkSeparator;
        public List<Field> fields;
        public String displayName;
        public boolean singleRecord;
        public String lkValue;
        public Tree tree;

        @Introspected
        public static class Field {
            public String name;
            public String displayName;
            public Type type;
            public List<String> enums;
            public ObjectType object;
            public boolean required;
            public Dictionary dict;
            public ArrayType array;
            public Select select;
            public AnyType any;

            @Introspected
            public enum Type {
                STRING, INTEGER, DECIMAL, DOUBLE, BOOL, DATE, OBJECT, DICTIONARY, ENUM, ARRAY, SELECT, B64_IMAGE, ANY
            }

            @Introspected
            public static class ObjectType {
                public List<Field> fields;
            }

            @Introspected
            public static class Dictionary {
                public List<Field> fields;
            }

            @Introspected
            public static class ArrayType {
                public Type type;
                public ObjectType object;
                public ArrayType array;
                public List<String> enums;
                public Select select;
            }

            @Introspected
            public static class Select {
                public List<SelectElem> elems;

                @Introspected
                public static class SelectElem {
                    public String value;
                    public String displayName;
                    public List<String> driveFields;
                }
            }

            @Introspected
            public static class Decimal {
            }

            @Introspected
            public static class AnyType {
                public String typeField;
            }
        }

        @Introspected
        public static class Tree {
            public boolean enabled;
            public String parentField;
        }
    }

    @Introspected
    public enum Type {
        ENTITY
    }
}
