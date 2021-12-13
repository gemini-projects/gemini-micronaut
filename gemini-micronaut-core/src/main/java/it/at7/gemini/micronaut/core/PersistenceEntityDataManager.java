package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.DuplicateLkRecordException;
import it.at7.gemini.micronaut.exception.EntityFieldNotFoundException;
import it.at7.gemini.micronaut.exception.EntityRecordNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface PersistenceEntityDataManager {

    DataCountResult countRecords(Entity entity, DataListRequest dataListRequest) throws EntityFieldNotFoundException;

    DataListResult<EntityRecord> getRecords(Entity entity, DataListRequest dataListRequest) throws EntityFieldNotFoundException, FieldConversionException;

    DataResult<EntityRecord> getRecord(Entity entity, String lk, DataRequest dataRequest) throws EntityRecordNotFoundException, FieldConversionException;

    DataResult<EntityRecord> add(EntityRecord entityRecord) throws FieldConversionException, DuplicateLkRecordException;

    DataListResult<EntityRecord> addAll(Entity entity, List<EntityRecord> entityRecordList) throws FieldConversionException;

    DataResult<EntityRecord> update(EntityRecord entityRecord) throws FieldConversionException;

    DataResult<EntityRecord> delete(EntityRecord entityRecord) throws FieldConversionException;

    @Nullable
    Map<String, EntityTimes> times();
}
