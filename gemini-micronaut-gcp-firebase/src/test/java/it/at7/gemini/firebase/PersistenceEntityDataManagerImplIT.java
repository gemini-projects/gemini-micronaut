package it.at7.gemini.firebase;

import io.micronaut.test.annotation.MicronautTest;
import it.at7.gemini.micronaut.core.*;
import it.at7.gemini.micronaut.exception.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersistenceEntityDataManagerImplIT {

    @Inject
    PersistenceEntityDataManager persistenceEntityDataManager;

    @Inject
    EntityManager entityManager;

    String dateTest;

    @BeforeAll
    void addSomeData() throws EntityNotFoundException, FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException {
        EntityDataManager basetypes = entityManager.getDataManager("BASETYPES");
        dateTest = new Date().toInstant().toString();
        basetypes.add(Map.of("stringField", "lkf1" + dateTest,
                "filterString", "F1" + dateTest));

        basetypes.add(Map.of("stringField", "lkf2" + dateTest,
                "filterString", "F1" + dateTest));

        basetypes.add(Map.of("stringField", "lkf3" + dateTest,
                "filterString", "F2" + dateTest));
    }

    @Test
    void getWithFilter() throws EntityNotFoundException, FieldConversionException, EntityFieldNotFoundException {
        EntityDataManager btManager = entityManager.getDataManager("BASETYPES");
        DataListResult<EntityRecord> records = btManager.getRecords(DataListRequest.builder()
                .addFilter("filterString", DataListRequest.OPE_TYPE.EQUALS, "F1" + dateTest)
                .build());
        List<EntityRecord> data = records.getData();
        Assertions.assertEquals(2, data.size());
    }

    @Test
    void add() throws EntityNotFoundException, FieldConversionException, EntityRecordValidationException, DuplicateLkRecordException {
        Entity basetypes = entityManager.get("BASETYPES");
        Assertions.assertNotNull(basetypes);

        String lk = new Date().toInstant().toString();

        EntityDataManager btManager = entityManager.getDataManager("BASETYPES");
        Map<String, Object> newRecFields = Map.of(
                "stringField", lk,
                "booleanField", true,
                "enumField", "E1",
                "intField", 42,
                "objectField", Map.of("st", "inner string"),
                "dictField", Map.of("dictkey1", Map.of("st", "dictValueSt"))
        );
        DataResult<EntityRecord> added = btManager.add(newRecFields);

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        Assertions.assertEquals(newRecFields, returnedMapConverted);

        Assertions.assertThrows(DuplicateLkRecordException.class, () -> {
            btManager.add(newRecFields);
        });
    }

    @Test
    void update() throws EntityNotFoundException, FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException, EntityRecordNotFoundException, EntityFieldNotFoundException {
        Entity basetypes = entityManager.get("BASETYPES");
        Assertions.assertNotNull(basetypes);

        String lk = new Date().toInstant().toString();

        EntityDataManager btManager = entityManager.getDataManager("BASETYPES");
        Map<String, Object> newRecFields = Map.of(
                "stringField", lk,
                "booleanField", true,
                "enumField", "E1",
                "intField", 42,
                "objectField", Map.of("st", "inner string"),
                "dictField", Map.of("dictkey1", Map.of("st", "dictValueSt"))
        );
        DataResult<EntityRecord> added = btManager.add(newRecFields);
        EntityRecord newRec = added.getData();

        newRec.set("booleanField", false);
        newRec.set("enumField", "E2");
        DataResult<EntityRecord> updatedRed = btManager.update(newRec);
        EntityRecord updRec = updatedRed.getData();
        Assertions.assertEquals(updRec.getData(), Map.of(
                "stringField", lk,
                "booleanField", false,
                "enumField", "E2",
                "intField", 42,
                "objectField", Map.of("st", "inner string"),
                "dictField", Map.of("dictkey1", Map.of("st", "dictValueSt"))
        ));

    }

    @Test
    void delete() throws FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException, EntityRecordNotFoundException, EntityNotFoundException {
        Entity basetypes = entityManager.get("BASETYPES");
        Assertions.assertNotNull(basetypes);

        String lk = new Date().toInstant().toString();

        EntityDataManager btManager = entityManager.getDataManager("BASETYPES");

        Map<String, Object> newRecFields = Map.of(
                "stringField", lk);
        btManager.add(newRecFields);

        DataResult<EntityRecord> record = btManager.delete(lk);

        Assertions.assertThrows(EntityRecordNotFoundException.class, () -> {
            btManager.getRecord(lk);
        });
    }
}