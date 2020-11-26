package it.at7.gemini.micronaut.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import it.at7.gemini.micronaut.api.GeminiHttpResponse;
import it.at7.gemini.micronaut.api.RequestUtils;
import it.at7.gemini.micronaut.core.EntityRecord;

import javax.inject.Singleton;
import java.util.Map;

public class EntityRecordValidationException extends Exception {
    private final EntityRecord er;
    private final EntityRecord.ValidationResult validation;

    public EntityRecordValidationException(EntityRecord er, EntityRecord.ValidationResult validation) throws FieldConversionException {
        super(String.format("EntityRecord of %s with id %s invalid", er.getEntity().getName(), er.getLkString()));
        this.er = er;
        this.validation = validation;
    }

    public EntityRecord getEntityRecord() {
        return er;
    }

    public EntityRecord.ValidationResult getValidation() {
        return validation;
    }

    @Singleton
    @Requires(classes = {EntityRecordValidationException.class, ExceptionHandler.class})
    public static class ExcpetionHandler implements ExceptionHandler<EntityRecordValidationException, HttpResponse<GeminiHttpResponse>> {

        @Override
        public HttpResponse<GeminiHttpResponse> handle(HttpRequest request, EntityRecordValidationException e) {
            // EntityRecord er = e.getEntityRecord();
            EntityRecord.ValidationResult validation = e.getValidation();
            return HttpResponse.status(HttpStatus.BAD_REQUEST)
                    .body(RequestUtils.errorResponseLogger(request, e.getMessage(), Map.of("fieldValidationErrors", validation.getErrors())));
        }
    }
}
