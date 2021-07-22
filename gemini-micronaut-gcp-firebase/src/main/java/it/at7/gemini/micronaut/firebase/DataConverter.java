package it.at7.gemini.micronaut.firebase;

import it.at7.gemini.micronaut.core.Entity;
import it.at7.gemini.micronaut.core.Field;
import it.at7.gemini.micronaut.exception.FieldConversionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DataConverter {


    public static Map<String, Object> toDataStoreMap(Entity entity, Map<String, Object> geminiMap) throws FieldConversionException {
        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : geminiMap.entrySet()) {
            Optional<Field> fieldOpt = entity.getFieldOpt(entry.getKey());
            if (fieldOpt.isPresent())
                ret.put(entry.getKey(), toValue(fieldOpt.get(), entry.getValue()));
        }
        return ret;
    }

    private static Object toValue(Field field, Object value) throws FieldConversionException {
        if (value == null) {
            return handleToValueNull(field);
        }
        switch (field.getType()) {
            case STRING:
            case INTEGER:
            case DECIMAL:
            case DOUBLE:
            case BOOL:
            case ENUM:
            case SELECT:
            case GEOHASH_LOCATION:
            case B64_IMAGE:
            case ARRAY:
            case ANY:
                return value;
            case OBJECT:
                return objectValue(field, value);
            case DICTIONARY:
                return dictionaryValue(field, value);
            case DATE:
                return dateValue(field, value);
            case ENTITY_REF:
                return entityRefValue(field, value);
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

    private static Object doubleValue(Object value) {
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        else
            return Double.parseDouble(String.valueOf(value));
    }

    private static Object decimalValue(Field field, Object value) {
        // TODO add bigdecimal specific fields conversion ??
        if (value instanceof Number)
            return BigDecimal.valueOf(((Number) value).doubleValue());
        else
            return new BigDecimal(String.valueOf(value));
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
            if (mapValue.isEmpty())
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
            if (mapValue.isEmpty())
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


    private static Object dateValue(Field field, Object value) throws FieldConversionException {
        if (value instanceof LocalDate)
            return ((LocalDate) value).toString();

        throw new FieldConversionException(field, value);
    }


    private static Object entityRefValue(Field field, Object value) {
        // TODO any type of check ?? Probably when we introduce the serial ID data type or custom reference fields
        return value;
    }


    private static Object handleToValueNull(Field field) {
        switch (field.getType()) {
            case BOOL:
                return false;
        }
        return null;
    }
}


