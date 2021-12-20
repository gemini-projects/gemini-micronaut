package it.at7.gemini.micronaut.api.filters;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import it.at7.gemini.micronaut.api.GeminiHttpResponse;
import it.at7.gemini.micronaut.core.EntityDataManager;
import it.at7.gemini.micronaut.core.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@MicronautTest()
@EnabledIfSystemProperty(named = "gemini.test.entitymanager", matches = "full")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataControllerGET_FILTER_BOOL_Test {

    @Inject
    @Client("/data")
    HttpClient client;

    @Inject
    EntityManager entityManager;
    String randomUUID;

    @BeforeAll
    void addSomeData() throws Exception {
        EntityDataManager basetypes = entityManager.getDataManager("basetypes");
        // lk for get
        randomUUID = UUID.randomUUID().toString();
        basetypes.add(Map.of(
                "stringField", "lk" + randomUUID + "_1",
                "filterString", randomUUID,
                "booleanField", true
        ));
        basetypes.add(Map.of(
                "stringField", "lk" + randomUUID + "_2",
                "filterString", randomUUID,
                "booleanField", false
        ));
        basetypes.add(Map.of(
                "stringField", "lk" + randomUUID + "_3",
                "filterString", randomUUID,
                "booleanField", false
        ));
    }

    @Test
    void filterEqual() {
        String filter = "filterString=" + randomUUID + "&booleanField=true";
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.GET("/basetypes?" + filter), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        List<Map<String, Object>> records = (List<Map<String, Object>>) gr.getData();
        Assertions.assertEquals(1, records.size());
    }

    @Test
    void filterNotEqual() {
        String filter = "filterString=" + randomUUID + "&booleanField[NOT_EQUALS]=true";
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.GET("/basetypes?" + filter), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse gr = body.get();
        List<Map<String, Object>> records = (List<Map<String, Object>>) gr.getData();
        Assertions.assertEquals(2, records.size());

        filter = "filterString=" + randomUUID + "&booleanField[!]=true";
        resp = client.toBlocking().exchange(HttpRequest.GET("/basetypes?" + filter), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        records = (List<Map<String, Object>>) body.get().getData();
        Assertions.assertEquals(2, records.size());

        filter = "filterString=" + randomUUID + "&booleanField[NOT]=true";
        resp = client.toBlocking().exchange(HttpRequest.GET("/basetypes?" + filter), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        records = (List<Map<String, Object>>) body.get().getData();
        Assertions.assertEquals(2, records.size());
    }
}