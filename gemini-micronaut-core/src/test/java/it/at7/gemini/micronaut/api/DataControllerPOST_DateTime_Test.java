package it.at7.gemini.micronaut.api;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Map.entry;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataControllerPOST_DateTime_Test {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/data")
    HttpClient client;

    Map<String, Object> newRecFields;

    @Test
    void postLocalDateTime() {
        newRecFields = Map.ofEntries(
                entry("stringField", "lk" + UUID.randomUUID().toString()),
                entry("dateTimeField", "2011-12-03T10:15:30")
        );
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.POST("/basetypes",
                Map.of("data", newRecFields)), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse geminiHttpResponse = body.get();
        Map<String, Object> data = (Map<String, Object>) geminiHttpResponse.getData();
        Assertions.assertEquals(List.of(2011, 12, 3, 10, 15, 30), data.get("dateTimeField"));
    }

    @Test
    void postOffsetDateTime() {
        newRecFields = Map.ofEntries(
                entry("stringField", "lk" + UUID.randomUUID().toString()),
                entry("dateTimeField", "2011-12-03T10:15:30+01:00")
        );
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.POST("/basetypes",
                Map.of("data", newRecFields)), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse geminiHttpResponse = body.get();
        Map<String, Object> data = (Map<String, Object>) geminiHttpResponse.getData();
        // hour is 9 because we POST +01 time zone, and the result is always in UTC
        Assertions.assertEquals(List.of(2011, 12, 3, 9, 15, 30), data.get("dateTimeField"));
    }
}