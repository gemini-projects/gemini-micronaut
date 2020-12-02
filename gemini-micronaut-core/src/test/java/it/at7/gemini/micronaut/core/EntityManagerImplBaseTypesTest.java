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
class EntityManagerImplBaseTypesTest {

    @Inject
    EntityManager entityManager;

    @Test
    void testInsert() throws Exception {
        Entity basetypes = entityManager.get("BASETYPES");
        Assertions.assertNotNull(basetypes);

        EntityDataManager btManager = entityManager.getDataManager("BASETYPES");
        Map<String, Object> newRecFields = Map.of(
                "stringField", "lk",
                "booleanField", true,
                "enumField", "E1",
                "intField", 42,
                "objectField", Map.of("st", "inner string"),
                "dictField", Map.of("dictkey1", Map.of("st", "dictValueSt")),
                "selectField", "S1"
        );
        DataResult<EntityRecord> added = btManager.add(newRecFields);

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        Assertions.assertEquals(newRecFields, returnedMapConverted);
    }


    @Test
    void testRequiredException() throws EntityNotFoundException, FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException {
        EntityRecordValidationException resp = Assertions.assertThrows(EntityRecordValidationException.class, () -> {
            EntityDataManager dm = entityManager.getDataManager("TESTVALIDATION");
            dm.add(Map.of("stringField", "lk"));
        });
        EntityRecord.ValidationResult validation = resp.getValidation();
        Assertions.assertFalse(validation.isValid());
    }

}