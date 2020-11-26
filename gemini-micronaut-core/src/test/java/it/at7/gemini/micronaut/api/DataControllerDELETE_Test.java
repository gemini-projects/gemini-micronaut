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
class DataControllerDELETE_Test {

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

        basetypes.add(Map.of("stringField", "delete_lk",
                "enumField", "E1",
                "booleanField", true));
    }

    @Test
    void deleteById_allFields() throws EntityNotFoundException {

        // put change all field (not logical key)
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.DELETE("/basetypes/delete_lk"), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals("delete_lk", record.get("stringField"));
        Assertions.assertEquals("E1", record.get("enumField"));
        Assertions.assertEquals(true, record.get("booleanField"));

        EntityDataManager basetypes = entityManager.getDataManager("basetypes");
        Assertions.assertThrows(EntityRecordNotFoundException.class, () -> basetypes.getRecord("delete_lk"));
    }

    @Test
    void deleteByIdNotFound() {
        HttpClientResponseException resp = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.DELETE("/basetypes/unknown", Map.of()), GeminiHttpResponse.class);
        });
        Assertions.assertEquals(resp.getStatus(), HttpStatus.NOT_FOUND);
    }
}