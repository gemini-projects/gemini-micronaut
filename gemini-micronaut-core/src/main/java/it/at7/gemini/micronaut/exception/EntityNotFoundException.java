package it.at7.gemini.micronaut.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import it.at7.gemini.micronaut.api.GeminiHttpResponse;
import it.at7.gemini.micronaut.api.RequestUtils;

import javax.inject.Singleton;

public class EntityNotFoundException extends Exception {
    private String entity;

    public EntityNotFoundException(String entity) {
        super(String.format("Entity %s not found", entity));
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }

    @Singleton
    @Requires(classes = {EntityNotFoundException.class, ExceptionHandler.class})
    public static class EntityNotFoundExceptionHandler implements ExceptionHandler<EntityNotFoundException, HttpResponse<GeminiHttpResponse>> {

        @Override
        public HttpResponse<GeminiHttpResponse> handle(HttpRequest request, EntityNotFoundException e) {
            return HttpResponse.status(HttpStatus.NOT_FOUND)
                    .body(RequestUtils.errorResponseLogger(request, e.getMessage()));
        }
    }
}
