package it.at7.gemini.micronaut.api;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import io.micronaut.http.codec.CodecException;
import it.at7.gemini.micronaut.core.*;
import it.at7.gemini.micronaut.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

@Controller("/data/{entity}")
public class DataController {
    private static final Logger logger = LoggerFactory.getLogger(DataController.class);

    @Inject
    EntityManager entityManager;

    @Get
    HttpResponse<GeminiHttpResponse> get(@PathVariable("entity") String entityName, HttpRequest httpRequest) throws EntityNotFoundException, FieldConversionException, EntityFieldNotFoundException {
        RequestUtils.crateAndSetTimeLogger(logger, httpRequest, "GET-ENTITY", entityName);
        EntityDataManager entityDataManager = this.entityManager.getDataManager(entityName);
        DataListResult<EntityRecord> records = entityDataManager.getRecords(DataListRequest.from(httpRequest));
        return RequestUtils.readyResponse(records, httpRequest);
    }

    @Get("/{+id}")
    HttpResponse<GeminiHttpResponse> getById(@PathVariable("entity") String entityName,
                                             @PathVariable("id") String id,
                                             HttpRequest httpRequest) throws EntityNotFoundException, EntityRecordNotFoundException, FieldConversionException {
        RequestUtils.crateAndSetTimeLogger(logger, httpRequest, "GET-ENTITY-BYID", entityName + " " + id);
        EntityDataManager entityDataManager = this.entityManager.getDataManager(entityName);
        DataResult<EntityRecord> record = entityDataManager.getRecord(id, null);
        return RequestUtils.readyResponse(record, httpRequest);
    }

    @Post
    HttpResponse<GeminiHttpResponse> post(@PathVariable("entity") String entityName,
                                          @Body DataRequest body,
                                          HttpRequest<Map<String, Object>> httpRequest) throws EntityNotFoundException, FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException {
          RequestUtils.crateAndSetTimeLogger(logger, httpRequest, "POST-ENTITY", entityName);
        EntityDataManager entityDataManager = this.entityManager.getDataManager(entityName);
        Map<String, Object> data = RequestUtils.getRequestData(body);
        DataResult<EntityRecord> record = entityDataManager.add(data);
        return RequestUtils.readyResponse(record, httpRequest);
    }

    @Put("/{+id}")
    HttpResponse<GeminiHttpResponse> putById(@PathVariable("entity") String entityName,
                                             @PathVariable("id") String id,
                                             @Body DataRequest body,
                                             HttpRequest httpRequest) throws EntityNotFoundException, EntityRecordNotFoundException, FieldConversionException, EntityFieldNotFoundException {
        RequestUtils.crateAndSetTimeLogger(logger, httpRequest, "PUT-ENTITY-BYID", entityName + " " + id);

        EntityDataManager entityDataManager = this.entityManager.getDataManager(entityName);
        Map<String, Object> data = RequestUtils.getRequestData(body);
        DataResult<EntityRecord> record = entityDataManager.fullUpdate(id, data);
        return RequestUtils.readyResponse(record, httpRequest);
    }

    @Patch("/{+id}")
    HttpResponse<GeminiHttpResponse> patchById(@PathVariable("entity") String entityName,
                                               @PathVariable("id") String id,
                                               @Body DataRequest body,
                                               HttpRequest httpRequest) throws EntityNotFoundException, EntityFieldNotFoundException, FieldConversionException, EntityRecordNotFoundException {
        RequestUtils.crateAndSetTimeLogger(logger, httpRequest, "PATCH-ENTITY-BYID", entityName + " " + id);
        EntityDataManager entityDataManager = this.entityManager.getDataManager(entityName);

        Map<String, Object> data = RequestUtils.getRequestData(body);
        DataResult<EntityRecord> record = entityDataManager.partialUpdate(id, data);
        return RequestUtils.readyResponse(record, httpRequest);
    }

    @Delete("/{+id}")
    HttpResponse<GeminiHttpResponse> delete(@PathVariable("entity") String entityName,
                                            @PathVariable("id") String id,
                                            HttpRequest httpRequest) throws EntityNotFoundException, EntityFieldNotFoundException, FieldConversionException, EntityRecordNotFoundException {
        RequestUtils.crateAndSetTimeLogger(logger, httpRequest, "DELETE-ENTITY-BYID", entityName + " " + id);
        EntityDataManager entityDataManager = this.entityManager.getDataManager(entityName);

        DataResult<EntityRecord> record = entityDataManager.delete(id);
        return RequestUtils.readyResponse(record, httpRequest);
    }


    @Error
    public HttpResponse<GeminiHttpResponse> jsonParseExcepitons(HttpRequest request, CodecException e) {
        return HttpResponse.status(HttpStatus.BAD_REQUEST)
                .body(RequestUtils.errorResponseLogger(request, e.getMessage()));
    }

    /* @Error(global = true)
    public HttpResponse<GeminiHttpResponse> genericGlobalError(HttpRequest request, Throwable e) {
        String message = e.getMessage() == null ? e.toString() : e.getMessage();
        return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RequestUtils.errorResponseLogger(request, message));
    } */

    @Error(global = true, status = HttpStatus.NOT_FOUND)
    public HttpResponse<GeminiHttpResponse> notFoundError(HttpRequest request) {
        return HttpResponse.status(HttpStatus.NOT_FOUND)
                .body(RequestUtils.errorResponseLogger(request, HttpStatus.NOT_FOUND.getReason()));
    }

    @Error(global = true, status = HttpStatus.INTERNAL_SERVER_ERROR)
    public HttpResponse<GeminiHttpResponse> genericGlobalIEError(HttpRequest request, Throwable e) {
        String message = e.getMessage() == null ? e.toString() : e.getMessage();
        return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RequestUtils.errorResponseLogger(request, message));
    }

}
