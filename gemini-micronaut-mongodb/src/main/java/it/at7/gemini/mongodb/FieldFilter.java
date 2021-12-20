package it.at7.gemini.mongodb;

import com.mongodb.client.model.Filters;
import it.at7.gemini.micronaut.core.DataListRequest;
import it.at7.gemini.micronaut.core.Entity;
import it.at7.gemini.micronaut.core.Field;
import it.at7.gemini.micronaut.exception.EntityFieldNotFoundException;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.stream.Collectors;

public class FieldFilter {

    public static Bson fieldFilter(Entity entity, DataListRequest.Filter filter) throws EntityFieldNotFoundException {
        String fieldName = filter.getFieldName();
        List<Field> fields = entity.getFieldsFromPath(fieldName);
        Field field = fields.get(fields.size() - 1);
        switch (field.getType()) {
            case STRING:
                return stringFilter(fields, filter);
            case ENTITY_REF:
                return entityRefField(fields, filter);
            case ARRAY:
                // return arrayFilter( fields, filter);
            case INTEGER:
            case DOUBLE:
                return numericField(fields, filter);
            case BOOL:
                return boolField(fields, filter);
            case OBJECT:
            case ENUM:
            case DICTIONARY:
            case SELECT:
                // TODO
                break;
        }
        return new Document();
    }


    /*  private static Bson arrayFilter(Query collection, List<Field> fields, DataListRequest.Filter filter) {
         switch (filter.getOperation()) {
             case EQUALS:
                 // TODO
                 break;
             case CONTAINS:
                 if (String.class.isAssignableFrom(filter.getValue().getClass()))
                     return collection.whereArrayContains(fields.stream().map(Field::getName).collect(Collectors.joining(".")), filter.getValue());
                 break;
         }
         return collection;
     } */
    private static Bson stringFilter(List<Field> fields, DataListRequest.Filter filter) {
        String fieldName = fields.stream().map(Field::getName).collect(Collectors.joining("."));
        String stringValue = filter.getValue().toString();
        switch (filter.getOperation()) {
            case EQUALS:
                return Filters.eq(fieldName, stringValue);
            case NOT_EQUALS:
                return Filters.ne(fieldName, stringValue);
            case CONTAINS:
                return Filters.regex(fieldName, stringValue);
        }
        return new Document();
    }

    private static Bson entityRefField(List<Field> fields, DataListRequest.Filter filter) {
        String fieldName = fields.stream().map(Field::getName).collect(Collectors.joining("."));
        String stringValue = filter.getValue().toString();
        switch (filter.getOperation()) {
            case EQUALS:
                return Filters.eq(fieldName, stringValue);
            case CONTAINS:
                throw new UnsupportedOperationException("contains filter not allowed for Entity Ref types");
        }
        return new Document();
    }

    private static Bson numericField(List<Field> fields, DataListRequest.Filter filter) {
        String fieldName = fields.stream().map(Field::getName).collect(Collectors.joining("."));
        Field field = fields.get(fields.size() - 1);
        Object value = null;
        if (field.getType().equals(Field.Type.INTEGER))
            value = Integer.parseInt(filter.getValue().toString());
        if (field.getType().equals(Field.Type.DOUBLE))
            value = Double.parseDouble(filter.getValue().toString());
        if (value == null)
            throw new RuntimeException("target filter value is not a right type");
        switch (filter.getOperation()) {
            case EQUALS:
                return Filters.eq(fieldName, value);
            case NOT_EQUALS:
                return Filters.ne(fieldName, value);
            case GTE:
                return Filters.gte(fieldName, value);
            case GT:
                return Filters.gt(fieldName, value);
            case LTE:
                return Filters.lte(fieldName, value);
            case LT:
                return Filters.lt(fieldName, value);
            default:
                throw new UnsupportedOperationException("contains filter not allowed for Entity Ref types");
        }
    }

    private static Bson boolField(List<Field> fields, DataListRequest.Filter filter) {
        String fieldName = fields.stream().map(Field::getName).collect(Collectors.joining("."));
        String s = filter.getValue().toString();
        boolean value = Boolean.parseBoolean(s);
        switch (filter.getOperation()) {
            case EQUALS:
                return Filters.eq(fieldName, value);
            case NOT_EQUALS:
                return Filters.ne(fieldName, value);
            default:
                throw new UnsupportedOperationException("contains filter not allowed for Entity Ref types");
        }
    }
}
