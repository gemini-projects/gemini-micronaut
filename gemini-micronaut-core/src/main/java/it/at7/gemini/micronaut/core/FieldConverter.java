package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.FieldConversionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldConverter {
    public static Object toValue(Field field, Object value) throws FieldConversionException {
        if (value == null) {
            return handleToValueNull(field);
        }
        switch (field.getType()) {
            case STRING:
                return String.valueOf(value);
            case INTEGER:
                return integerValue(value);
            case BOOL:
                return boolValue(value);
            case OBJECT:
                return objectValue(field, value);
            case DICTIONARY:
                return dictionaryValue(field, value);
            case ENUM:
                return enumValue(field, value);
            case ARRAY: {
                return arrayValue(field, value);
            }
        }
        throw new FieldConversionException(field, value);
    }

    private static Integer integerValue(Object value) {
        if (value instanceof Number)
            return ((Number) value).intValue();
        else {
            return Integer.parseInt(String.valueOf(value));
        }
    }

    private static Boolean boolValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            String stValue = String.valueOf(value);
            return Boolean.parseBoolean(stValue);
        }
    }

    private static Object objectValue(Field field, Object value) throws FieldConversionException {
        if (Map.class.isAssignableFrom(value.getClass())) {
            Map<String, Object> mapValue = (Map<String, Object>) value;
            if(mapValue.isEmpty())
                return null;
            Map<String, Object> objVal = new HashMap<>();
            for (Map.Entry<String, Field> entry : field.getInnerFields().entrySet()) {
                objVal.put(entry.getKey(), toValue(entry.getValue(), mapValue.get(entry.getKey())));
            }
            return objVal;
        }
        throw new FieldConversionException(field, value);
    }

    private static Object dictionaryValue(Field field, Object value) throws FieldConversionException {
        if (Map.class.isAssignableFrom(value.getClass())) {
            Map<String, Object> mapValue = (Map<String, Object>) value;
            if(mapValue.isEmpty())
                return null;
            Map<String, Object> dictRetValue = new HashMap<>();
            for (Map.Entry<String, Object> entry : mapValue.entrySet()) {
                Map<String, Object> dictNewObjVal = new HashMap<>();
                Map<String, Object> mapObjValue = (Map<String, Object>) entry.getValue();
                for (Map.Entry<String, Field> objField : field.getInnerFields().entrySet()) {
                    dictNewObjVal.put(objField.getKey(), toValue(objField.getValue(), mapObjValue.get(objField.getKey())));
                }
                dictRetValue.put(entry.getKey(), dictNewObjVal);
            }
            return dictRetValue;
        }
        throw new FieldConversionException(field, value);
    }

    private static String enumValue(Field field, Object value) throws FieldConversionException {
        String stValue = String.valueOf(value);
        if (field.getEnums().contains(stValue.toUpperCase()))
            return stValue;
        throw new FieldConversionException(field, value);
    }

    private static Object arrayValue(Field field, Object value) throws FieldConversionException {
        if (List.class.isAssignableFrom(value.getClass())) {
            List<Object> arrayVal = (List<Object>) value;
            List<Object> retValue = new ArrayList<>();
            for (Object singleVal : arrayVal) {
                Object toIns = null;
                switch (field.getArrayType()) {
                    case STRING:
                        toIns = String.valueOf(singleVal);
                        break;
                    case BOOL:
                        toIns = boolValue(singleVal);
                        break;
                    case OBJECT:
                        toIns = objectValue(field, singleVal);
                        break;
                    case ENUM:
                        toIns = enumValue(field, singleVal);
                        break;
                    case ARRAY:
                        throw new UnsupportedOperationException("Wrong type");
                }
                if (toIns == null)
                    throw new FieldConversionException(field, value);
                retValue.add(toIns);
            }
            return retValue;
        }
        throw new FieldConversionException(field, value);
    }

    private static Object handleToValueNull(Field field) {
        switch (field.getType()) {
            case BOOL:
                return false;
        }
        return null;
    }

    public static String toStringValue(Field field, Object value, String separator) throws FieldConversionException {
        if (value == null)
            throw new FieldConversionException(field, null);
        switch (field.getType()) {
            case STRING:
            case BOOL:
            case ENUM: {
                return String.valueOf(value);
            }
            case OBJECT: {
                if (Map.class.isAssignableFrom(value.getClass())) {
                    Map<String, Object> mapValue = (Map<String, Object>) value;
                    StringBuilder buff = new StringBuilder();
                    for (Map.Entry<String, Field> entry : field.getInnerFields().entrySet()) {
                        buff.append(toStringValue(entry.getValue(), mapValue.get(entry.getKey()), separator)).append(separator);
                    }
                    return buff.toString();
                }
            }
        }
        throw new FieldConversionException(field, value);
    }

}
