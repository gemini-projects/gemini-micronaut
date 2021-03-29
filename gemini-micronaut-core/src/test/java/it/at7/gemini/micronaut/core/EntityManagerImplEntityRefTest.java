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
import java.util.Map;

import static java.util.Map.entry;

@MicronautTest
class EntityManagerImplEntityRefTest {

    @Inject
    EntityManager entityManager;

    @Test
    void testInsert() throws Exception {
        /*Entity category = entityManager.get("CATEGORY");
        EntityDataManager ctManager = entityManager.getDataManager("CATEGORY");
        ctManager.add(Map.of("id", "c1"));*/

        Entity ewf = entityManager.get("ENTITY_WITH_REF");
        Assertions.assertNotNull(ewf);

        EntityDataManager ewfManager = entityManager.getDataManager("ENTITY_WITH_REF");
        Map<String, Object> newRecFields = Map.ofEntries(
                entry("id", "lk_c1"),
                entry("category", "c1")
        );
        DataResult<EntityRecord> added = ewfManager.add(newRecFields);

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        Assertions.assertEquals(newRecFields, returnedMapConverted);
    }

}