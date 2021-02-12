package it.at7.gemini.micronaut.schema;

import java.util.List;

public class RawSchema {
    public Type type;
    public Entity entity;

    public static class Entity {
        public String name;
        public List<String> lk;
        public String lkSeparator;
        public List<Field> fields;
        public String displayName;
        public boolean singleRecord;
        public String lkValue;
        public Tree tree;

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

            public enum Type {
                STRING, INTEGER, DECIMAL, DOUBLE, BOOL, OBJECT, DICTIONARY, ENUM, ARRAY, SELECT, B64_IMAGE, ANY
            }

            public static class ObjectType {
                public List<Field> fields;
            }

            public static class Dictionary {
                public List<Field> fields;
            }

            public static class ArrayType {
                public Type type;
                public ObjectType object;
                public ArrayType array;
                public List<String> enums;
                public Select select;
            }

            public static class Select {
                public List<SelectElem> elems;

                public static class SelectElem {
                    public String value;
                    public String displayName;
                    public List<String> driveFields;
                }
            }

            public static class Decimal {
            }

            public static class AnyType {
                public String typeField;
            }
        }

        public static class Tree {
            public boolean enabled;
            public String parentField;
        }
    }


    public enum Type {
        ENTITY
    }
}
