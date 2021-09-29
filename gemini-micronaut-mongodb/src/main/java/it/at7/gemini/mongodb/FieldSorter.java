package it.at7.gemini.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import it.at7.gemini.micronaut.core.DataListRequest;
import it.at7.gemini.micronaut.core.Entity;
import it.at7.gemini.micronaut.core.Field;
import it.at7.gemini.micronaut.exception.EntityFieldNotFoundException;
import org.bson.Document;

import java.util.List;

public class FieldSorter {

    public static void fieldSorter(FindIterable<Document> documents, Entity entity, DataListRequest.Order order) throws EntityFieldNotFoundException {
        String fieldName = order.getFieldName();
        List<Field> fields = entity.getFieldsFromPath(fieldName);
        Field field = fields.get(fields.size() - 1);
        switch (field.getType()) {
            case STRING:
            case DATE:
            case INTEGER:
            case SELECT:
                stringSorter(documents, fields, order);
                return;
            case ARRAY:
                // return arrayFilter( fields, filter);
            case BOOL:
            case OBJECT:
            case ENUM:
            case DICTIONARY:
                // TODO
                break;
        }
    }

    private static void stringSorter(FindIterable<Document> documents, List<Field> fields, DataListRequest.Order order) {
        for (Field field : fields) {
            documents.sort(new BasicDBObject(field.getName(), order.getType() == DataListRequest.ORDER_TYPE.ASC ? 1 : -1));
        }
    }

}
