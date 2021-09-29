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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityRecordListValidationException extends Exception {
    private final List<EntityRecord> ers;
    private final List<EntityRecord.ValidationResult> validations;

    public EntityRecordListValidationException(List<EntityRecord> ers, List<EntityRecord.ValidationResult> validations) {
        super("EntityRecords are invalid");
        this.ers = ers;
        this.validations = validations;
    }

    public List<EntityRecord> getEntityRecordList() {
        return ers;
    }

    public List<EntityRecord.ValidationResult> getValidations() {
        return validations;
    }

    @Singleton
    @Requires(classes = {EntityRecordListValidationException.class, ExceptionHandler.class})
    public static class ExcpetionHandler implements ExceptionHandler<EntityRecordListValidationException, HttpResponse<GeminiHttpResponse>> {

        @Override
        public HttpResponse<GeminiHttpResponse> handle(HttpRequest request, EntityRecordListValidationException e) {
            // EntityRecord er = e.getEntityRecord();
            List<Map<?, ?>> errors = e.getValidations().stream().map(validationResult -> validationResult.isValid() ? Map.of() : validationResult.getErrors()).collect(Collectors.toList());

            return HttpResponse.status(HttpStatus.BAD_REQUEST)
                    .body(RequestUtils.errorResponseLogger(request, e.getMessage(), Map.of("recordList", e.getEntityRecordList(), "recordValidationErrors", errors), e));
        }
    }
}
