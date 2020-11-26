package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.*;

import java.util.Map;

public interface EntityDataManager {

    Entity getEntity();

    DataListResult<EntityRecord> getRecords(DataListRequest dataListRequest) throws FieldConversionException;

    default DataResult<EntityRecord> getRecord(String id) throws EntityRecordNotFoundException, FieldConversionException {
        return getRecord(id, null);
    }

    DataResult<EntityRecord> getRecord(String id, DataRequest dataRequest) throws EntityRecordNotFoundException, FieldConversionException;

    DataResult<EntityRecord> add(Map<String, Object> data) throws FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException;

    DataResult<EntityRecord> add(EntityRecord er) throws FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException;

    DataResult<EntityRecord> fullUpdate(String id, Map<String, Object> data) throws EntityRecordNotFoundException, FieldConversionException, EntityFieldNotFoundException;

    DataResult<EntityRecord> partialUpdate(String lk, Map<String, Object> data) throws FieldConversionException, EntityRecordNotFoundException, EntityFieldNotFoundException;

    DataResult<EntityRecord> update(EntityRecord record) throws FieldConversionException, EntityRecordNotFoundException, EntityFieldNotFoundException;

    DataResult<EntityRecord> delete(String id) throws EntityRecordNotFoundException, FieldConversionException;
}
