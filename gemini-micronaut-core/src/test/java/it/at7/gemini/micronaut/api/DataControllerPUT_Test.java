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
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@MicronautTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataControllerPUT_Test {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/data")
    HttpClient client;

    @Inject
    EntityManager entityManager;

    @BeforeAll
    void addSomeData() throws Exception {
        EntityDataManager basetypes = entityManager.getDataManager("basetypes");

        // other entry to put
        basetypes.add(Map.of("stringField", "put_lk",
                "enumField", "E1",
                "booleanField", true));

        // other entry to put
        basetypes.add(Map.of("stringField", "put_changelk",
                "enumField", "E1",
                "booleanField", true));
    }

    @Test
    void putById_allFields() {

        // put change all field (not logical key)
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.PUT("/basetypes/put_lk",
                Map.of("data", Map.of("stringField", "put_lk",
                        "enumField", "E2",
                        "booleanField", false))), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals("put_lk", record.get("stringField"));
        Assertions.assertEquals("E2", record.get("enumField"));
        Assertions.assertEquals(false, record.get("booleanField"));
    }

    @Test
    void putById_SomeFields() {
        // put with partial data - but full update -> field to null

        // put change all field (not logical key)
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.PUT("/basetypes/put_lk",
                Map.of("data", Map.of("stringField", "put_lk",
                        "booleanField", true))), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals("put_lk", record.get("stringField"));
        Assertions.assertNull(record.get("enumField"));
        Assertions.assertEquals(true, record.get("booleanField"));

    }

    @Test
    void putById_ChangeLK() throws EntityRecordNotFoundException, FieldConversionException, EntityNotFoundException {
        // put changing also lk
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.PUT("/basetypes/put_changelk",
                Map.of("data", Map.of("stringField", "newLk",
                        "booleanField", false))), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals("newLk", record.get("stringField"));
        Assertions.assertNull(record.get("enumField"));
        Assertions.assertEquals(false, record.get("booleanField"));

        EntityDataManager basetypes = entityManager.getDataManager("basetypes");

        Assertions.assertThrows(EntityRecordNotFoundException.class, () -> basetypes.getRecord("put_changelk"));

        DataResult<EntityRecord> newLk = basetypes.getRecord("newLk");
        Assertions.assertNotNull(newLk);
    }

    @Test
    void putByIdNotFound() {
        HttpClientResponseException resp = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.PUT("/basetypes/unknown", Map.of("data", Map.of("field", "a"))), GeminiHttpResponse.class);
        });
        Assertions.assertEquals(resp.getStatus(), HttpStatus.NOT_FOUND);
    }

}