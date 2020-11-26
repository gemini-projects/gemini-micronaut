package it.at7.gemini.micronaut.api;

import com.fasterxml.jackson.core.JsonParseException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.server.exceptions.ExceptionHandler;

import javax.inject.Singleton;

public class ErrorHandlers {
   /*  @Singleton
    @Requires(classes = {CodecException.class, ExceptionHandler.class})
    public static class ExcpetionHandler implements ExceptionHandler<CodecException, HttpResponse<GeminiHttpResponse>> {

        @Override
        public HttpResponse<GeminiHttpResponse> handle(HttpRequest request, CodecException e) {
            return HttpResponse.status(HttpStatus.BAD_REQUEST)
                    .body(RequestUtils.errorResponseLogger(request, e.getMessage()));
        }
    } */

    @Singleton
    @Requires(classes = {JsonParseException.class, ExceptionHandler.class})
    public static class ExcpetionHandler implements ExceptionHandler<JsonParseException, HttpResponse<GeminiHttpResponse>> {

        @Override
        public HttpResponse<GeminiHttpResponse> handle(HttpRequest request, JsonParseException e) {
            return HttpResponse.status(HttpStatus.BAD_REQUEST)
                    .body(RequestUtils.errorResponseLogger(request, e.getMessage()));
        }
    }

    @Error
    public HttpResponse<GeminiHttpResponse> jsonError(HttpRequest request, CodecException e) {
        return HttpResponse.status(HttpStatus.BAD_REQUEST)
                .body(RequestUtils.errorResponseLogger(request, e.getMessage()));
    }

}