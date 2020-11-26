package it.at7.gemini.micronaut.schema;

import java.util.List;

public class RawSchema {
    public Type type;
    public Entity entity;

    public static class Entity {
        public String name;
        public List<String> lk;
        public List<Field> fields;
        public String displayName;

        public static class Field {
            public String name;
            public String displayName;
            public Type type;
            public List<String> enums;
            public ObjectType object;
            public boolean required;
            public Dictionary dict;
            public ArrayType array;

            public enum Type {
                STRING, INTEGER, BOOL, OBJECT, DICTIONARY, ENUM, ARRAY
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
        }
    }


    public enum Type {
        ENTITY
    }
}
