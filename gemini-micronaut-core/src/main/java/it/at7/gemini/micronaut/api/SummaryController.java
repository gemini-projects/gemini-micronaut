package it.at7.gemini.micronaut.api;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import it.at7.gemini.micronaut.core.EntityManager;
import it.at7.gemini.micronaut.core.EntityTimes;
import it.at7.gemini.micronaut.exception.EntityNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("/summary")
public class SummaryController {
    private static final Logger logger = LoggerFactory.getLogger(SummaryController.class);

    @Inject
    EntityManager entityManager;

    @Get
    HttpResponse<GeminiHttpResponse> getList(HttpRequest httpRequest) throws EntityNotFoundException, FieldConversionException {
        RequestUtils.crateAndSetTimeLogger(logger, httpRequest, "SUMMARY", "");

        Map<String, EntityTimes> times = this.entityManager.getEntitiesTimes();
        Map<String, EntitySummary> entities = times.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            EntityTimes value = e.getValue();
            EntitySummary entitySummary = new EntitySummary();
            entitySummary.lastCreateTimeUnix = value.getLastCreateTimeUnix();
            entitySummary.lastCreateTimeISO = Instant.ofEpochMilli(value.getLastCreateTimeUnix()).toString();
            entitySummary.lastDeleteTimeUnix = value.getLastDeleteTimeUnix();
            entitySummary.lastDeleteTimeISO = Instant.ofEpochMilli(value.getLastDeleteTimeUnix()).toString();
            entitySummary.lastUpdateTimeUnix = value.getLastUpdateTimeUnix();
            entitySummary.lastUpdateTimeISO = Instant.ofEpochMilli(value.getLastUpdateTimeUnix()).toString();
            return entitySummary;
        }));


        SchemaSummary schemaSummary = new SchemaSummary();
        schemaSummary.hash = entityManager.getLoadedSchema().getSchemaHash();

        SummaryData summaryData = new SummaryData();
        summaryData.schema = schemaSummary;
        summaryData.entities = entities;

        return RequestUtils.readyResponse(summaryData, httpRequest);
    }


    @Introspected
    public class SummaryData {
        public SchemaSummary schema;
        public Map<String, EntitySummary> entities;
    }

    @Introspected
    public class SchemaSummary {
        public String hash;
    }

    @Introspected
    public class EntitySummary {
        public String lastUpdateTimeISO;
        public Long lastUpdateTimeUnix;

        public String lastCreateTimeISO;
        public Long lastCreateTimeUnix;

        public String lastDeleteTimeISO;
        public Long lastDeleteTimeUnix;
    }
}
