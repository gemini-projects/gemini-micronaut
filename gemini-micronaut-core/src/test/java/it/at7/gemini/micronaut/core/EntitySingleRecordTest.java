package it.at7.gemini.micronaut.core;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import it.at7.gemini.micronaut.exception.EntitySingleRecordException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;

@MicronautTest
public class EntitySingleRecordTest {
    @Inject
    EntityManager entityManager;


    @Test
    void testAddNewRecord() {
        EntitySingleRecordException resp = Assertions.assertThrows(EntitySingleRecordException.class, () -> {
            EntityDataManager dm = entityManager.getDataManager("SINGLEREC");
            dm.add(Map.of("stringField", "somestring"));
        });
        Assertions.assertTrue(resp.getMessage().contains("Adding records is not allowed"));
    }

    @Test
    void testDeleteARecord() {
        EntitySingleRecordException resp = Assertions.assertThrows(EntitySingleRecordException.class, () -> {
            EntityDataManager dm = entityManager.getDataManager("SINGLEREC");
            dm.delete("some_ids");
        });
        Assertions.assertTrue(resp.getMessage().contains("Delete record is not allowed"));
    }

    @Test
    void testUpdateAndGet() throws Exception {
        Entity singlerecEntity = entityManager.get("SINGLEREC");
        Assertions.assertNotNull(singlerecEntity);

        EntityDataManager btManager = entityManager.getDataManager("SINGLEREC");
        Map<String, Object> updateFields = Map.of(
                "stringField", "afield"
        );
        EntityRecord entityRecord = new EntityRecord(singlerecEntity);
        entityRecord.set(updateFields);
        DataResult<EntityRecord> added = btManager.update(entityRecord);

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        Assertions.assertEquals(updateFields, returnedMapConverted);

        DataResult<EntityRecord> singleRecordGet = btManager.getSingleRecord();
        EntityRecord getRec = singleRecordGet.getData();
        Assertions.assertEquals(updateFields, getRec.getData());
    }

    @Test
    void testPartialFullUpdate() throws Exception {
        Entity singlerecEntity = entityManager.get("SINGLEREC");
        Assertions.assertNotNull(singlerecEntity);

        EntityDataManager btManager = entityManager.getDataManager("SINGLEREC");
        Map<String, Object> updateFields = Map.of(
                "stringField", "afield",
                "booleanField", true,
                "enumField", "E1"
        );
        EntityRecord entityRecord = new EntityRecord(singlerecEntity);
        entityRecord.set(updateFields);
        btManager.update(entityRecord);

        DataResult<EntityRecord> partialUpdateRES = btManager.partialUpdate(entityRecord.getLkString(), Map.of("enumField", "E2"));
        EntityRecord partialUpdate = partialUpdateRES.getData();
        Assertions.assertEquals(Map.of(
                "stringField", "afield",
                "booleanField", true,
                "enumField", "E2"
        ), partialUpdate.getData());

        DataResult<EntityRecord> fullUpdateRES = btManager.fullUpdate(entityRecord.getLkString(), Map.of("stringField", "singleField"));
        EntityRecord fullUpdate = fullUpdateRES.getData();
        Assertions.assertEquals(Map.of("stringField", "singleField"), fullUpdate.getData());
    }

}
