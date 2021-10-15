package it.at7.gemini.micronaut.core;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import it.at7.gemini.micronaut.exception.DuplicateLkRecordException;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;
import it.at7.gemini.micronaut.exception.EntityRecordValidationException;
import it.at7.gemini.micronaut.exception.FieldConversionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static java.util.Map.entry;

@MicronautTest
class EntityManagerImplDateTimeTest {

    @Inject
    EntityManager entityManager;

    @Test
    void testInsertLocalDateTime() throws Exception {
        Entity basetypes = entityManager.get("BASETYPES");
        Assertions.assertNotNull(basetypes);

        EntityDataManager btManager = entityManager.getDataManager("BASETYPES");
        Map<String, Object> newRecFields = Map.ofEntries(
                entry("stringField", "lk" + UUID.randomUUID().toString()),
                entry("dateTimeField", LocalDateTime.parse("2011-12-03T10:15:30"))
        );
        DataResult<EntityRecord> added = btManager.add(newRecFields);

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        Assertions.assertEquals(newRecFields, returnedMapConverted);
    }

    @Test
    void testInsertStringLocalDateTime() throws Exception {
        Entity basetypes = entityManager.get("BASETYPES");
        Assertions.assertNotNull(basetypes);

        EntityDataManager btManager = entityManager.getDataManager("BASETYPES");
        Map<String, Object> newRecFields = Map.ofEntries(
                entry("stringField", "lk" + UUID.randomUUID().toString()),
                entry("dateTimeField", "2011-12-03T10:15:30")
        );
        DataResult<EntityRecord> added = btManager.add(newRecFields);

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        Assertions.assertEquals(newRecFields.get("dateTimeField"), returnedMapConverted.get("dateTimeField").toString());
    }

    @Test
    void testInsertStringWithOffset() throws Exception {
        Entity basetypes = entityManager.get("BASETYPES");
        Assertions.assertNotNull(basetypes);

        EntityDataManager btManager = entityManager.getDataManager("BASETYPES");
        Map<String, Object> newRecFields = Map.ofEntries(
                entry("stringField", "lk" + UUID.randomUUID().toString()),
                entry("dateTimeField", "2011-12-03T10:15:30+01:00")
        );
        DataResult<EntityRecord> added = btManager.add(newRecFields);

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        // expected UTC so 1h minus
        Assertions.assertEquals("2011-12-03T09:15:30", returnedMapConverted.get("dateTimeField").toString());
    }

    @Test
    void testInsertStringWithOffsetZone() throws Exception {
        Entity basetypes = entityManager.get("BASETYPES");
        Assertions.assertNotNull(basetypes);

        EntityDataManager btManager = entityManager.getDataManager("BASETYPES");
        Map<String, Object> newRecFields = Map.ofEntries(
                entry("stringField", "lk" + UUID.randomUUID().toString()),
                entry("dateTimeField", "2011-12-03T10:15:30+01:00[Europe/Paris]")
        );
        DataResult<EntityRecord> added = btManager.add(newRecFields);

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        // expected UTC so 1h minus
        Assertions.assertEquals("2011-12-03T09:15:30", returnedMapConverted.get("dateTimeField").toString());
    }
}