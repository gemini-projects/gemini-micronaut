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
            case ARRAY:
                // return arrayFilter( fields, filter);
            case INTEGER:
            case BOOL:
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
        switch (filter.getOperation()) {
            case EQUALS:
                // TODO check value string ??
                return Filters.eq(fields.stream().map(Field::getName).collect(Collectors.joining(".")), filter.getValue());
        }
        return new Document();
    }
}
