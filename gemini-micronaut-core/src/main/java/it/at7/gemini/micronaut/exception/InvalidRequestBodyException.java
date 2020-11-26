package it.at7.gemini.micronaut.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import it.at7.gemini.micronaut.api.GeminiHttpResponse;
import it.at7.gemini.micronaut.api.RequestUtils;

import javax.inject.Singleton;

public class InvalidRequestBodyException extends Exception {

    public InvalidRequestBodyException(String message) {
        super(message);
    }

    @Singleton
    @Requires(classes = {InvalidRequestBodyException.class, ExceptionHandler.class})
    public static class ExcpetionHandler implements ExceptionHandler<InvalidRequestBodyException, HttpResponse<GeminiHttpResponse>> {

        @Override
        public HttpResponse<GeminiHttpResponse> handle(HttpRequest request, InvalidRequestBodyException e) {
            return HttpResponse.status(HttpStatus.BAD_REQUEST)
                    .body(RequestUtils.errorResponseLogger(request, e.getMessage()));
        }
    }
}
