package it.at7.gemini.micronaut.api;

import it.at7.gemini.micronaut.core.EntityRecord;
import it.at7.gemini.micronaut.core.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseConverter {
    public static Map<String, Object> convert(EntityRecord data) {
        Map<String, Object> mapData = data.getData();
        return (Map<String, Object>) convertField(mapData);
    }

    private static Object convertField(Object value) {

        if (value instanceof LocalDate) {
            LocalDate ld = (LocalDate) value;
            return ld.format(DateTimeFormatter.ISO_DATE);
        }

        if (value instanceof LocalDateTime) {
            LocalDateTime dateVal = (LocalDateTime) value;
            return OffsetDateTime.of(dateVal, ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        if (value instanceof List) {
            List listValue = (List) value;
            List<Object> listResult = new ArrayList<>();
            for (Object listVal : listValue) {
                listResult.add(convertField(listVal));
            }
            return listResult;
        }

        if (value instanceof Map) {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> mapData = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : mapData.entrySet()) {
                String fieldName = entry.getKey();
                Object entryVal = entry.getValue();
                result.put(fieldName, convertField(entryVal));
            }
            return result;
        }

        return value;
    }
}