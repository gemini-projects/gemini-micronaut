package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.DuplicateLkRecordException;
import it.at7.gemini.micronaut.exception.EntityFieldNotFoundException;
import it.at7.gemini.micronaut.exception.EntityRecordNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;

import java.util.Map;

public interface PersistenceEntityDataManager {
    DataListResult<EntityRecord> getRecords(Entity entity, DataListRequest dataListRequest) throws EntityFieldNotFoundException, FieldConversionException;

    DataResult<EntityRecord> getRecord(Entity entity, String lk, DataRequest dataRequest) throws EntityRecordNotFoundException, FieldConversionException;

    DataResult<EntityRecord> add(EntityRecord entityRecord) throws FieldConversionException, DuplicateLkRecordException;

    DataResult<EntityRecord> update(EntityRecord entityRecord) throws FieldConversionException;

    DataResult<EntityRecord> delete(EntityRecord entityRecord) throws FieldConversionException;

    Map<String, EntityTimes> times();
}
