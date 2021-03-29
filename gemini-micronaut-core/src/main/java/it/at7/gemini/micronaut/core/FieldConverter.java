package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.FieldConversionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
            case DECIMAL:
                return decimalValue(field, value);
            case DOUBLE:
                return doubleValue(value);
            case BOOL:
                return boolValue(value);
            case OBJECT:
                return objectValue(field, value);
            case DICTIONARY:
                return dictionaryValue(field, value);
            case ENUM:
                return enumValue(field, value);
            case SELECT:
                return selectValue(field, value);
            case ARRAY:
                return arrayValue(field, value);
            case B64_IMAGE:
                return b64Image(field, value);
            case DATE:
                return dateValue(field, value);
            case ENTITY_REF:
                return entityRefValue(field, value);
            case ANY:
                return value;
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

    private static String enumValue(Field field, Object value) throws FieldConversionException {
        String stValue = String.valueOf(value);
        if (field.getEnums().contains(stValue.toUpperCase()))
            return stValue;
        throw new FieldConversionException(field, value);
    }

    private static String selectValue(Field field, Object value) throws FieldConversionException {
        String stValue = String.valueOf(value);
        if (field.getEnums().contains(stValue))
            return stValue;
        throw new FieldConversionException(field, value);
    }

    private static Object dateValue(Field field, Object value) throws FieldConversionException {
        if (value instanceof LocalDate)
            return value;
        if (List.class.isAssignableFrom(value.getClass())) {
            try {
                List<Integer> lo = (List<Integer>) value;
                return LocalDate.of(lo.get(0), lo.get(1), lo.get(2));
            } catch (ClassCastException e) {
                throw new FieldConversionException(field, value, e.getMessage());
            }
        }
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (RuntimeException re) {
            throw new FieldConversionException(field, value, re.getMessage());
        }

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
                    case SELECT:
                        toIns = selectValue(field, singleVal);
                        break;
                    case ENTITY_REF:
                        toIns = entityRefValue(field, singleVal);
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

    private static Object b64Image(Field field, Object value) throws FieldConversionException {
        if (value instanceof String) {
            String stValue = (String) value;

            //  Pattern.matches("data:image\/[^;]+;base64[^\"]+", stValue);
            boolean matches = Pattern.matches("data:image/[^;]+;base64[^\"]+", stValue);
            if (matches)
                return value;
        }
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
