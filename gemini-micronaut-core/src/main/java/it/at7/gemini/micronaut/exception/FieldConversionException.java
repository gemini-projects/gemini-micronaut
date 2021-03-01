package it.at7.gemini.micronaut.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import it.at7.gemini.micronaut.api.GeminiHttpResponse;
import it.at7.gemini.micronaut.api.RequestUtils;
import it.at7.gemini.micronaut.core.Field;

import javax.annotation.Nullable;
import javax.inject.Singleton;

public class FieldConversionException extends Exception {
    private final Field field;
    private final Object value;

    public FieldConversionException(Field field, @Nullable Object value) {
        super(String.format("Unable to convert field %s - %s - for value %s", field.getName(), field.getType(), value));
        this.field = field;
        this.value = value;
    }

    public FieldConversionException(Field field, @Nullable Object value, String additionalMessage) {
        super(String.format("Unable to convert field %s - %s - for value %s - %s", field.getName(), field.getType(), value, additionalMessage));
        this.field = field;
        this.value = value;
    }

    public Field getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    @Singleton
    @Requires(classes = {FieldConversionException.class, ExceptionHandler.class})
    public static class ExcpetionHandler implements ExceptionHandler<FieldConversionException, HttpResponse<GeminiHttpResponse>> {

        @Override
        public HttpResponse<GeminiHttpResponse> handle(HttpRequest request, FieldConversionException e) {
            return HttpResponse.status(HttpStatus.BAD_REQUEST)
                    .body(RequestUtils.errorResponseLogger(request, e));
        }
    }
}
