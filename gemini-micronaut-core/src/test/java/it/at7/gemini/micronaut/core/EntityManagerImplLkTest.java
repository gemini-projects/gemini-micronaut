package it.at7.gemini.micronaut.core;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@MicronautTest
class EntityManagerImplLkTest {

    @Inject
    EntityManager entityManager;

    @Test
    void testInsert() throws Exception {
        Entity basetypes = entityManager.get("MULTIPLELK");
        Assertions.assertNotNull(basetypes);
        String uuid = UUID.randomUUID().toString();
        EntityDataManager btManager = entityManager.getDataManager("MULTIPLELK");
        Map<String, Object> newRecFields = Map.of(
                "id1", "lk" ,
                "id2", "lk2"+uuid
        );
        DataResult<EntityRecord> added = btManager.add(newRecFields);
        Assertions.assertEquals("lk_lk2" + uuid, added.getData().getLkString());

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        Assertions.assertEquals(newRecFields, returnedMapConverted);
    }

    @Test
    void lkEmptyString() throws Exception {
        Entity basetypes = entityManager.get("MULTIPLELK");
        Assertions.assertNotNull(basetypes);

        EntityRecord entityRecord = new EntityRecord(basetypes);
        entityRecord.set(Map.of(
                "id1", "",
                "id2", "lk2"
        ));

        String lkString = entityRecord.getLkString();
        Assertions.assertEquals("lk2", lkString);
    }
}