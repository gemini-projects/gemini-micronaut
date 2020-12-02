package it.at7.gemini.micronaut.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import it.at7.gemini.micronaut.api.GeminiHttpResponse;
import it.at7.gemini.micronaut.api.RequestUtils;
import it.at7.gemini.micronaut.core.Entity;

import javax.inject.Singleton;

public class EntitySingleRecordException extends Exception {
    private final Entity entity;

    public EntitySingleRecordException(Entity entity, String message) {
        super(message);
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    @Singleton
    @Requires(classes = {EntitySingleRecordException.class, ExceptionHandler.class})
    public static class EntityNotFoundExceptionHandler implements ExceptionHandler<EntitySingleRecordException, HttpResponse<GeminiHttpResponse>> {

        @Override
        public HttpResponse<GeminiHttpResponse> handle(HttpRequest request, EntitySingleRecordException e) {
            return HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body(RequestUtils.errorResponseLogger(request, e.getMessage()));
        }
    }

}
