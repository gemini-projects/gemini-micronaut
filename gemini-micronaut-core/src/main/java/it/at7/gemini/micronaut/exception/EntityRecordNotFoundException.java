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

public class EntityRecordNotFoundException extends Exception {

    private final Entity entity;
    private final String id;

    public EntityRecordNotFoundException(Entity entity, String id) {
        super(String.format("EntityRecord with id %s not found for %s", id, entity.getName()));
        this.entity = entity;
        this.id = id;
    }

    public Entity getEntity() {
        return entity;
    }

    public String getId() {
        return id;
    }

    @Singleton
    @Requires(classes = {EntityRecordNotFoundException.class, ExceptionHandler.class})
    public static class ExcpetionHandler implements ExceptionHandler<EntityRecordNotFoundException, HttpResponse<GeminiHttpResponse>> {

        @Override
        public HttpResponse<GeminiHttpResponse> handle(HttpRequest request, EntityRecordNotFoundException e) {
            return HttpResponse.status(HttpStatus.NOT_FOUND)
                    .body(RequestUtils.errorResponseLogger(request, e.getMessage()));
        }
    }
}
