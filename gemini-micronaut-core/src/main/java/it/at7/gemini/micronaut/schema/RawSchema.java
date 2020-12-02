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

            public enum Type {
                STRING, INTEGER, BOOL, OBJECT, DICTIONARY, ENUM, ARRAY, SELECT
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
            }

            public static class Select {
                public List<SelectElem> elems;

                public static class SelectElem {
                    public String value;
                    public String displayName;
                    public List<String> driveFields;
                }
            }
        }
    }


    public enum Type {
        ENTITY
    }
}
