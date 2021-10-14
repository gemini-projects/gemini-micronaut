package it.at7.gemini.micronaut.api;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import it.at7.gemini.micronaut.core.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.util.Map;

@MicronautTest(environments = {"alwm"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataControllerAllowedMethods_Test {

    @Inject
    @Client("/data")
    HttpClient client;


    @Test
    void newNotAllowed() {
        HttpClientResponseException resp = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.POST("/basetypes",
                    Map.of("data", Map.of())), GeminiHttpResponse.class);
        });
        Assertions.assertEquals(resp.getStatus(), HttpStatus.FORBIDDEN);
    }

    @Test
    void getListNotAllowed() {
        HttpClientResponseException resp = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.GET("/basetypes"), GeminiHttpResponse.class);
        });
        Assertions.assertEquals(resp.getStatus(), HttpStatus.FORBIDDEN);
    }

    @Test
    void getByIdNotAllowed() {
        HttpClientResponseException resp = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.GET("/basetypes/someid"), GeminiHttpResponse.class);
        });
        Assertions.assertEquals(resp.getStatus(), HttpStatus.FORBIDDEN);
    }

    @Test
    void updateNotAllowed() {
        HttpClientResponseException resp = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.PATCH("/basetypes/patch_lk",
                    Map.of("data", Map.of("stringField", "patch_lk",
                            "enumField", "E2",
                            "booleanField", false))), GeminiHttpResponse.class);
        });
        Assertions.assertEquals(resp.getStatus(), HttpStatus.FORBIDDEN);

        HttpClientResponseException resp2 = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.PUT("/basetypes/put_lk",
                    Map.of("data", Map.of("stringField", "put_lk",
                            "enumField", "E2",
                            "booleanField", false))), GeminiHttpResponse.class);
        });
        Assertions.assertEquals(resp2.getStatus(), HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteNotAllowed() {
        HttpClientResponseException resp = Assertions.assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.DELETE("/basetypes/some", Map.of()), GeminiHttpResponse.class);
        });
        Assertions.assertEquals(resp.getStatus(), HttpStatus.FORBIDDEN);
    }

}