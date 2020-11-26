package it.at7.gemini.micronaut.api;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@MicronautTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataControllerPOST_Test {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/data")
    HttpClient client;

    Map<String, Object> newRecFields;

    @Test
    void post() {
        newRecFields = Map.of(
                "stringField", "lk",
                "booleanField", true,
                "enumField", "E1",
                "intField", 42,
                "objectField", Map.of("st", "inner string"),
                "dictField", Map.of("dictkey1", Map.of("st", "dictValueSt"))
        );
        HttpResponse<GeminiHttpResponse> resp = client.toBlocking().exchange(HttpRequest.POST("/basetypes",
                Map.of("data", newRecFields)), GeminiHttpResponse.class);
        Assertions.assertEquals(resp.getStatus(), HttpStatus.OK);
        Optional<GeminiHttpResponse> body = resp.getBody();
        Assertions.assertTrue(body.isPresent());
        GeminiHttpResponse geminiHttpResponse = body.get();
        Map<String, Object> data = (Map<String, Object>) geminiHttpResponse.getData();
        Assertions.assertEquals(newRecFields, data);
    }

    @Test
    void postExceptionMissingDataField() {
        HttpClientResponseException httpClientResponseException = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            HttpResponse<Object> exchange = client.toBlocking().exchange(HttpRequest.POST("/basetypes",
                    Map.of("unk", "unk")));
        });
        Assertions.assertEquals(httpClientResponseException.getStatus(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void postExceptionMissingRequiredField() {
        HttpClientResponseException resp = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.POST("/testvalidation",
                    Map.of("data", Map.of("stringField", "lk",
                            "booleanField", true))), Argument.of(GeminiHttpResponse.class), Argument.of(GeminiHttpResponse.class));

        });

        Optional<GeminiHttpResponse> body = resp.getResponse().getBody(GeminiHttpResponse.class);
        GeminiHttpResponse geminiHttpResponse = body.get();
        Map<String, Object> error = (Map<String, Object>) geminiHttpResponse.getError();
        Map<String, Object> fieldErrors = (Map<String, Object>) error.get("fieldValidationErrors");
        Map<String, Object> enumfield = (Map<String, Object>) fieldErrors.get("enumField");
        Assertions.assertEquals("REQUIRED", enumfield.get("error"));
    }
}