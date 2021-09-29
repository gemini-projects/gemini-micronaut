package it.at7.gemini.micronaut.api;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import it.at7.gemini.micronaut.core.DataResult;
import it.at7.gemini.micronaut.core.EntityDataManager;
import it.at7.gemini.micronaut.core.EntityManager;
import it.at7.gemini.micronaut.core.EntityRecord;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;
import it.at7.gemini.micronaut.exception.EntityRecordNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataControllerPUT_Test {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/data")
    HttpClient client;

    @Inject
    EntityManager entityManager;

    String random;

    @BeforeAll
    void addSomeData() throws Exception {
        EntityDataManager basetypes = entityManager.getDataManager("basetypes");

        random = UUID.randomUUID().toString();

        // other entry to put
        basetypes.add(Map.of("stringField", "put_lk" + random,
                "enumField", "E1",
                "booleanField", true));

        // other entry to put
        basetypes.add(Map.of("stringField", "put_changelk" + random,
                "enumField", "E1",
                "booleanField", true));
    }

    @Test
    void putById_allFields() {

        // put change all field (not logical key)
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.PUT("/basetypes/put_lk" + random,
                Map.of("data", Map.of("stringField", "put_lk" + random,
                        "enumField", "E2",
                        "booleanField", false))), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals("put_lk" + random, record.get("stringField"));
        Assertions.assertEquals("E2", record.get("enumField"));
        Assertions.assertEquals(false, record.get("booleanField"));
    }

    @Test
    void putById_SomeFields() {
        // put with partial data - but full update -> field to null

        // put change all field (not logical key)
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.PUT("/basetypes/put_lk" + random,
                Map.of("data", Map.of("stringField", "put_lk" + random,
                        "booleanField", true))), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals("put_lk" + random, record.get("stringField"));
        Assertions.assertNull(record.get("enumField"));
        Assertions.assertEquals(true, record.get("booleanField"));

    }

    @Test
    void putById_ChangeLK() throws EntityRecordNotFoundException, FieldConversionException, EntityNotFoundException {
        // put changing also lk
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.PUT("/basetypes/put_changelk" + random,
                Map.of("data", Map.of("stringField", "newLk" + random,
                        "booleanField", false))), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals("newLk" + random, record.get("stringField"));
        Assertions.assertNull(record.get("enumField"));
        Assertions.assertEquals(false, record.get("booleanField"));

        EntityDataManager basetypes = entityManager.getDataManager("basetypes");

        Assertions.assertThrows(EntityRecordNotFoundException.class, () -> basetypes.getRecord("put_changelk" + random));

        DataResult<EntityRecord> newLk = basetypes.getRecord("newLk" + random);
        Assertions.assertNotNull(newLk);
    }

    @Test
    void putByIdNewRecord() {
        // put change all field (not logical key)
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.PUT("/basetypes/put_lk_NEW" + random,
                Map.of("data", Map.of("stringField", "put_lk_NEW" + random,
                        "enumField", "E2",
                        "booleanField", false))), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals("put_lk_NEW" + random, record.get("stringField"));
        Assertions.assertEquals("E2", record.get("enumField"));
        Assertions.assertEquals(false, record.get("booleanField"));
    }

}