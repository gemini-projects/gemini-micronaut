package it.at7.gemini.micronaut.api;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import it.at7.gemini.micronaut.core.*;
import it.at7.gemini.micronaut.exception.EntityFieldNotFoundException;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;
import it.at7.gemini.micronaut.schema.EntitySchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Controller("/entity")
public class EntityController {

    private static final Logger logger = LoggerFactory.getLogger(EntityController.class);

    @Inject
    EntityManager entityManager;

    @Inject
    RestEntityManager restEntityManager;

    @Get
    HttpResponse<GeminiHttpResponse> getList(HttpRequest httpRequest) throws EntityNotFoundException, FieldConversionException {
        RequestUtils.createAndSetTimeLogger(logger, httpRequest, "GET-ENTITIES", "");
        Map<String, Object> results = new HashMap<>();
        for (Entity e : this.entityManager.getEntities()) {
            EntitySchema entitySchema = this.entityManager.getEntitySchema(e.getName());
            EntityRestConfig restConfiguration = restEntityManager.getRestConfiguration(e.getName());
            results.put(e.getName(), Map.of("schema", entitySchema, "restConfig", restConfiguration));
        }
        return RequestUtils.readyResponse(results, httpRequest);
    }

    @Get("/{entity}")
    HttpResponse<GeminiHttpResponse> get(@PathVariable("entity") String entityName, HttpRequest httpRequest) throws EntityNotFoundException, FieldConversionException {
        RequestUtils.createAndSetTimeLogger(logger, httpRequest, "GET-ENTITY", entityName);
        EntitySchema entitySchema = this.entityManager.getEntitySchema(entityName);
        EntityRestConfig restConfiguration = restEntityManager.getRestConfiguration(entityName);
        return RequestUtils.readyResponse(Map.of("schema", entitySchema, "restConfig", restConfiguration), httpRequest);
    }

    @Get("/{entity}/recordCounts")
    HttpResponse<GeminiHttpResponse> recordCounts(@PathVariable("entity") String entityName, HttpRequest httpRequest) throws EntityNotFoundException, FieldConversionException, EntityFieldNotFoundException {
        RequestUtils.createAndSetTimeLogger(logger, httpRequest, "COUNT-ENTITY", entityName);
        EntityRestConfig restConfiguration = restEntityManager.getRestConfiguration(entityName);
        EntityDataManager entityDataManager = this.entityManager.getDataManager(entityName);
        DataListRequest listRequest = DataListRequest.from(httpRequest);
        DataListRequest managedListRequest = restConfiguration.checkAndValidate(listRequest);
        DataCountResult dataCountResult = entityDataManager.countRecords(managedListRequest);

        Map<String, Object> body = new HashMap<>();
        dataCountResult.getEstimateCount().ifPresent(count -> {
            body.put("estimatedCount", count);
        });
        dataCountResult.getCount().ifPresent(count -> {
            body.put("count", count);
        });

        return RequestUtils.readyResponse(body, httpRequest);
    }
}