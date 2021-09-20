package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EntityDataManager {

    Entity getEntity();

    DataListResult<EntityRecord> getRecords(DataListRequest dataListRequest) throws FieldConversionException, EntityFieldNotFoundException;

    default DataResult<EntityRecord> getSingleRecord() throws EntitySingleRecordException, EntityRecordNotFoundException, FieldConversionException {
        if (!getEntity().isSingleRecord())
            throw new EntitySingleRecordException(getEntity(), "get Single Record allowed only for Single Record Entitites");
        return getRecord(getEntity().getLkSingleRecValue());
    }

    default DataResult<EntityRecord> getRecord(String id) throws EntityRecordNotFoundException, FieldConversionException {
        return getRecord(id, null);
    }

    DataResult<EntityRecord> getRecord(String id, DataRequest dataRequest) throws EntityRecordNotFoundException, FieldConversionException;

    DataResult<EntityRecord> add(Map<String, Object> data) throws FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException, EntitySingleRecordException;

    DataListResult<EntityRecord> addAll(Collection<Map<String, Object>> dataList) throws DuplicateLkRecordException, EntityRecordValidationException, EntitySingleRecordException, EntityRecordListValidationException, FieldConversionException;

    DataResult<EntityRecord> add(EntityRecord er) throws FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException, EntitySingleRecordException;

    DataListResult<EntityRecord> addAll(List<EntityRecord> entityRecordList) throws DuplicateLkRecordException, EntitySingleRecordException, EntityRecordListValidationException, FieldConversionException;

    DataResult<EntityRecord> fullUpdate(String lk, Map<String, Object> data) throws EntityRecordNotFoundException, FieldConversionException, EntityFieldNotFoundException;

    DataResult<EntityRecord> partialUpdate(String lk, Map<String, Object> data) throws FieldConversionException, EntityRecordNotFoundException, EntityFieldNotFoundException;

    DataResult<EntityRecord> update(EntityRecord record) throws FieldConversionException, EntityRecordNotFoundException, EntityFieldNotFoundException;

    DataResult<EntityRecord> delete(String id) throws EntityRecordNotFoundException, FieldConversionException, EntitySingleRecordException;
}
