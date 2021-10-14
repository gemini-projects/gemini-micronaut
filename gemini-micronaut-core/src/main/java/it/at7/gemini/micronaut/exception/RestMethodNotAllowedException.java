package it.at7.gemini.micronaut.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import it.at7.gemini.micronaut.api.GeminiHttpResponse;
import it.at7.gemini.micronaut.api.RequestUtils;

import javax.inject.Singleton;

public class RestMethodNotAllowedException extends Exception{
    private String method;
    private String entity;

    public RestMethodNotAllowedException(String entity, String method) {
        super(String.format("Method %s not allowed for Entity %s", method, entity));
        this.entity = entity;
        this.method = method;
    }

    public String getEntity() {
        return entity;
    }

    public String getMethod() {
        return method;
    }

    @Singleton
    @Requires(classes = {RestMethodNotAllowedException.class, ExceptionHandler.class})
    public static class EntityNotFoundExceptionHandler implements ExceptionHandler<RestMethodNotAllowedException, HttpResponse<GeminiHttpResponse>> {

        @Override
        public HttpResponse<GeminiHttpResponse> handle(HttpRequest request, RestMethodNotAllowedException e) {
            return HttpResponse.status(HttpStatus.FORBIDDEN)
                    .body(RequestUtils.errorResponseLogger(request, e.getMessage()));
        }
    }
}
