package it.at7.gemini.micronaut.core;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;

import static java.util.Map.entry;

@MicronautTest
class EntityManagerImplGeohashTest {

    @Inject
    EntityManager entityManager;

    @Test
    void testInsert() throws Exception {
        /*Entity category = entityManager.get("CATEGORY");
        EntityDataManager ctManager = entityManager.getDataManager("CATEGORY");
        ctManager.add(Map.of("id", "c1"));*/

        Entity egh = entityManager.get("ENTITY_WITH_GEOHASH");
        Assertions.assertNotNull(egh);

        EntityDataManager eghfManager = entityManager.getDataManager("ENTITY_WITH_GEOHASH");
        Map<String, Object> newRecFields = Map.ofEntries(
                entry("id", "lk_g1"),
                entry("location", Map.of(
                        "lat", 43.7085300,
                        "lng", 10.4036000,
                        "geohash", "spz2sszstkw3"

                ))
        );
        DataResult<EntityRecord> added = eghfManager.add(newRecFields);

        EntityRecord data = added.getData();
        Map<String, Object> returnedMapConverted = data.getData();
        Assertions.assertEquals(newRecFields, returnedMapConverted);
    }

    @Test
    void testInvalidTypes() throws EntityNotFoundException {
        Entity egh = entityManager.get("ENTITY_WITH_GEOHASH");
        Assertions.assertNotNull(egh);

        EntityDataManager eghfManager = entityManager.getDataManager("ENTITY_WITH_GEOHASH");
        Map<String, Object> newRecInvalidGeoHash = Map.ofEntries(
                entry("id", "lk_g1"),
                entry("location", Map.of(
                        "lat", 43.7085300,
                        "lng", 10.4036000,
                        "geohash", 12

                ))
        );
        Map<String, Object> newRecInvalidlat = Map.ofEntries(
                entry("id", "lk_g1"),
                entry("location", Map.of(
                        "lat", "ab",
                        "lng", 10.4036000,
                        "geohash", "spz2sszstkw3"

                ))
        );
        Map<String, Object> newRecInvalidLong = Map.ofEntries(
                entry("id", "lk_g1"),
                entry("location", Map.of(
                        "lat", 43.7085300,
                        "lng", "cd",
                        "geohash", "spz2sszstkw3"

                ))
        );
        FieldConversionException resp = Assertions.assertThrows(FieldConversionException.class, () -> {
            eghfManager.add(newRecInvalidGeoHash);
        });
        FieldConversionException resp1 = Assertions.assertThrows(FieldConversionException.class, () -> {
            eghfManager.add(newRecInvalidlat);
        });
        FieldConversionException resp2 = Assertions.assertThrows(FieldConversionException.class, () -> {
            eghfManager.add(newRecInvalidLong);
        });
        Assertions.assertEquals("location", resp.getField().getName());
        Assertions.assertEquals("location", resp1.getField().getName());
        Assertions.assertEquals("location", resp2.getField().getName());
    }

}