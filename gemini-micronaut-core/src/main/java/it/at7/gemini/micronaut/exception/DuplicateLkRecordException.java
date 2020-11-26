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

public class DuplicateLkRecordException extends Exception {

    private final Entity entity;
    private final String lk;

    public DuplicateLkRecordException(Entity entity, String lk) {
        super(String.format("Logical key %s for Entity %s already exists", lk, entity.getName()));
        this.entity = entity;
        this.lk = lk;
    }

    @Singleton
    @Requires(classes = {DuplicateLkRecordException.class, ExceptionHandler.class})
    public static class Handler implements ExceptionHandler<DuplicateLkRecordException, HttpResponse<GeminiHttpResponse>> {

        @Override
        public HttpResponse<GeminiHttpResponse> handle(HttpRequest request, DuplicateLkRecordException e) {
            return HttpResponse.status(HttpStatus.BAD_REQUEST)
                    .body(RequestUtils.errorResponseLogger(request, e.getMessage()));
        }
    }
}
