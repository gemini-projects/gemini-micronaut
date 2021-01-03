package it.at7.gemini.micronaut.core;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;

@MicronautTest
public class CustomEntityDataManagerTest {

    @Inject
    EntityManager entityManager;

    @Test
    void testInsert() throws Exception {
        Entity basetypes = entityManager.get("CUSTOMDATAMANAGER");
        Assertions.assertNotNull(basetypes);

        EntityDataManager btManager = entityManager.getDataManager("BASETYPES");
        Map<String, Object> newRecFields = Map.of(
                "stringField", "lk"
        );
        DataResult<EntityRecord> added = btManager.add(newRecFields);

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        Assertions.assertEquals(newRecFields, returnedMapConverted);
    }
}
