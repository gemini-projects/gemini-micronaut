package it.at7.gemini.micronaut.api;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MicronautTest;
import it.at7.gemini.micronaut.core.EntityDataManager;
import it.at7.gemini.micronaut.core.EntityManager;
import it.at7.gemini.micronaut.exception.DuplicateLkRecordException;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;
import it.at7.gemini.micronaut.exception.EntityRecordValidationException;
import it.at7.gemini.micronaut.exception.FieldConversionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataControllerGET_Test {


    @Inject
    @Client("/data")
    HttpClient client;

    @Inject
    EntityManager entityManager;

    Map<String, Object> newRecFields;
    Map<String, Object> newRecMultipleLkFields;

    @BeforeAll
    void addSomeData() throws Exception {
        EntityDataManager basetypes = entityManager.getDataManager("basetypes");
        // lk for get
        newRecFields = Map.of(
                "stringField", "lk",
                "booleanField", true,
                "enumField", "E1",
                "intField", 42,
                "objectField", Map.of("st", "inner string"),
                "dictField", Map.of("dictkey1", Map.of("st", "dictValueSt")),
                "selectField", "S1"
        );
        basetypes.add(newRecFields);

        EntityDataManager multiplelk = entityManager.getDataManager("MULTIPLELK");
        newRecMultipleLkFields = Map.of(
                "id1", "lk",
                "id2", "lk2"
        );
        multiplelk.add(newRecMultipleLkFields);
    }

    @Test
    void getListOfRecords() {
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.GET("/basetypes"), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        List<Map<String, Object>> records = (List<Map<String, Object>>) gr.getData();
        Assertions.assertEquals(1, records.size());
        Optional<Map<String, Object>> lkRec = records.stream().filter(r -> r.get("stringField").equals("lk")).findFirst();
        Assertions.assertTrue(lkRec.isPresent());
        Map<String, Object> restDataRes = lkRec.get();
        Assertions.assertEquals(newRecFields, restDataRes);
    }


    @Test
    void getById() {
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.GET("/basetypes/lk"), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals(newRecFields, record);
    }

    @Test
    void getByIdMultipleLk() {
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.GET("/multiplelk/lk_lk2"), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        Map<String, Object> record = (Map<String, Object>) gr.getData();
        Assertions.assertNotNull(record);
        Assertions.assertEquals(newRecMultipleLkFields, record);
    }

    @Test
    void getByIdNotFound() {
        HttpClientResponseException resp = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.GET("/basetypes/unknown"), GeminiHttpResponse.class);
        });
        Assertions.assertEquals(resp.getStatus(), HttpStatus.NOT_FOUND);
    }
}