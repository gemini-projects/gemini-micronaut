package it.at7.gemini.micronaut.api;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import it.at7.gemini.micronaut.core.DataResult;
import it.at7.gemini.micronaut.core.EntityDataManager;
import it.at7.gemini.micronaut.core.EntityManager;
import it.at7.gemini.micronaut.core.EntityRecord;
import it.at7.gemini.micronaut.exception.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@MicronautTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataControllerPATCH_Test {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/data")
    HttpClient client;

    @Inject
    EntityManager entityManager;

    @BeforeAll
    void addSomeData() throws EntityNotFoundException, FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException {
        EntityDataManager basetypes = entityManager.getDataManager("basetypes");

        basetypes.add(Map.of("stringField", "patch_lk",
                "enumField", "E1",
                "booleanField", true));

        basetypes.add(Map.of("stringField", "patch_lk_partial",
                "enumField", "E1",
                "booleanField", true));

        basetypes.add(Map.of("stringField", "patch_lkchange",
                "enumField", "E1",
                "booleanField", true));
    }

    @Test
    void patchById_allFields() {

        // put change all field (not logical key)
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.PATCH("/basetypes/patch_lk",
                Map.of("data", Map.of("stringField", "patch_lk",
                        "enumField", "E2",
                        "booleanField", false))), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals("patch_lk", record.get("stringField"));
        Assertions.assertEquals("E2", record.get("enumField"));
        Assertions.assertEquals(false, record.get("booleanField"));
    }

    @Test
    void patchById_SomeFields() {
        // put with partial data - but full update -> field to null

        // put change all field (not logical key)
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.PATCH("/basetypes/patch_lk_partial",
                Map.of("data", Map.of(
                        "booleanField", true))), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals("patch_lk_partial", record.get("stringField"));
        Assertions.assertEquals("E1", record.get("enumField"));
        Assertions.assertEquals(true, record.get("booleanField"));

    }

    @Test
    void patchtById_ChangeLK() throws EntityRecordNotFoundException, FieldConversionException, EntityNotFoundException {
        // put changing also lk
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.PATCH("/basetypes/patch_lkchange",
                Map.of("data", Map.of("stringField", "patch_lkchange_newLK"
                        ))), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals("patch_lkchange_newLK", record.get("stringField"));
        Assertions.assertEquals("E1", record.get("enumField"));
        Assertions.assertEquals(true, record.get("booleanField"));

        EntityDataManager basetypes = entityManager.getDataManager("basetypes");

        Assertions.assertThrows(EntityRecordNotFoundException.class, () -> basetypes.getRecord("patch_lkchange"));

        DataResult<EntityRecord> newLk = basetypes.getRecord("patch_lkchange_newLK");
        Assertions.assertNotNull(newLk);
    }

    @Test
    void patchByIdNotFound() {
        HttpClientResponseException resp = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.PATCH("/basetypes/unknown",  Map.of("data", Map.of("field", "a"))), GeminiHttpResponse.class);
        });
        Assertions.assertEquals(resp.getStatus(), HttpStatus.NOT_FOUND);
    }

}