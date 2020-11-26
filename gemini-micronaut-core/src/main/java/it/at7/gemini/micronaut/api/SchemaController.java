package it.at7.gemini.micronaut.api;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import it.at7.gemini.micronaut.core.Entity;
import it.at7.gemini.micronaut.core.EntityManager;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;
import it.at7.gemini.micronaut.schema.RawSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/schema")
public class SchemaController {

    private static final Logger logger = LoggerFactory.getLogger(SchemaController.class);

    @Inject
    EntityManager entityManager;

    @Get
    HttpResponse<GeminiHttpResponse> getList(HttpRequest httpRequest) throws EntityNotFoundException, FieldConversionException {
        RequestUtils.crateAndSetTimeLogger(logger, httpRequest, "GET-SCHEMAS", "");
        List<RawSchema.Entity> entities = new ArrayList<>();
        for (Entity e : this.entityManager.getEntities()) {
            RawSchema.Entity entitySchema = this.entityManager.getEntitySchema(e.getName());
            entities.add(entitySchema);
        }
        return RequestUtils.readyResponse(entities, httpRequest);
    }

    @Get("/{entity}")
    HttpResponse<GeminiHttpResponse> get(@PathVariable("entity") String entityName, HttpRequest httpRequest) throws EntityNotFoundException, FieldConversionException {
        RequestUtils.crateAndSetTimeLogger(logger, httpRequest, "GET-SCHEMA", entityName);
        RawSchema.Entity entitySchema = this.entityManager.getEntitySchema(entityName);
        return RequestUtils.readyResponse(entitySchema, httpRequest);
    }
}
