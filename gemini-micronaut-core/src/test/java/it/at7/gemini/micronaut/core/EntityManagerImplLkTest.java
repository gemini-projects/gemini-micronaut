package it.at7.gemini.micronaut.core;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import it.at7.gemini.micronaut.exception.DuplicateLkRecordException;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;
import it.at7.gemini.micronaut.exception.EntityRecordValidationException;
import it.at7.gemini.micronaut.exception.FieldConversionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;

@MicronautTest
class EntityManagerImplLkTest {

    @Inject
    EntityManager entityManager;

    @Test
    void testInsert() throws Exception {
        Entity basetypes = entityManager.get("MULTIPLELK");
        Assertions.assertNotNull(basetypes);

        EntityDataManager btManager = entityManager.getDataManager("MULTIPLELK");
        Map<String, Object> newRecFields = Map.of(
                "id1", "lk",
                "id2", "lk2"
        );
        DataResult<EntityRecord> added = btManager.add(newRecFields);
        Assertions.assertEquals("lk_lk2",added.getData().getLkString());

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        Assertions.assertEquals(newRecFields, returnedMapConverted);
    }


}