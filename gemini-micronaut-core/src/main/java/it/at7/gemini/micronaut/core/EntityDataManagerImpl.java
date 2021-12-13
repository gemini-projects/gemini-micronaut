package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityDataManagerImpl implements EntityDataManager {

    private PersistenceEntityDataManager persistenceEntityDataManager;
    private Entity entity;

    public EntityDataManagerImpl(PersistenceEntityDataManager persistenceEntityDataManager, Entity entity) {
        this.persistenceEntityDataManager = persistenceEntityDataManager;
        this.entity = entity;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public DataCountResult countRecords(DataListRequest dataListRequest) throws EntityFieldNotFoundException {
        return this.persistenceEntityDataManager.countRecords(entity, dataListRequest);
    }

    @Override
    public DataListResult<EntityRecord> getRecords(DataListRequest dataListRequest) throws FieldConversionException, EntityFieldNotFoundException {
        return this.persistenceEntityDataManager.getRecords(this.entity, dataListRequest);
    }

    @Override
    public DataResult<EntityRecord> getRecord(String lk, DataRequest dataRequest) throws EntityRecordNotFoundException, FieldConversionException {
        return this.persistenceEntityDataManager.getRecord(this.entity, lk, dataRequest);
    }

    @Override
    public DataResult<EntityRecord> add(Map<String, Object> data) throws FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException, EntitySingleRecordException {
        if (entity.isSingleRecord())
            throw new EntitySingleRecordException(entity, "Adding records is not allowed for single record entities");
        EntityRecord entityRecord = EntityRecord.fromDataMap(getEntity(), data);
        return this.add(entityRecord);
    }

    @Override
    public DataResult<EntityRecord> add(EntityRecord er) throws FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException, EntitySingleRecordException {
        if (entity.isSingleRecord())
            throw new EntitySingleRecordException(entity, "Adding records is not allowed for single record entities");
        EntityRecord.ValidationResult validation = er.validate();
        if (!validation.isValid())
            throw new EntityRecordValidationException(er, validation);
        // TODO for entity ref add a MODE to check if all the references exist (also in persistence manager)
        return this.persistenceEntityDataManager.add(er);
    }

    @Override
    public DataListResult<EntityRecord> addAll(Collection<Map<String, Object>> dataList) throws EntitySingleRecordException, EntityRecordListValidationException, FieldConversionException {
        if (entity.isSingleRecord()) {
            throw new EntitySingleRecordException(entity, "Adding records is not allowed for single record entities");
        }
        List<EntityRecord> erl = new ArrayList<>();
        for (Map<String, Object> erm : dataList) {
            erl.add(EntityRecord.fromDataMap(getEntity(), erm));
        }
        return this.addAll(erl);
    }

    @Override
    public DataListResult<EntityRecord> addAll(List<EntityRecord> entityRecordList) throws EntitySingleRecordException, EntityRecordListValidationException, FieldConversionException {
        if (entity.isSingleRecord())
            throw new EntitySingleRecordException(entity, "Adding records is not allowed for single record entities");

        List<EntityRecord.ValidationResult> validationsResults = entityRecordList.stream().map(EntityRecord::validate).collect(Collectors.toList());
        Boolean isValid = validationsResults.stream().reduce(false, (acc, current) -> current.isValid(), Boolean::logicalOr);

        if (!isValid) {
            throw new EntityRecordListValidationException(entityRecordList, validationsResults);
        }
        return this.persistenceEntityDataManager.addAll(getEntity(), entityRecordList);
    }

    @Override
    public DataResult<EntityRecord> fullUpdate(String lk, Map<String, Object> data) throws EntityRecordNotFoundException, FieldConversionException, EntityFieldNotFoundException, EntityRecordValidationException {
        DataResult<EntityRecord> record = getRecord(lk);
        EntityRecord er = record.getData();
        er.set(data);

        List<String> updatedField = new ArrayList<>();
        Entity entity = er.getEntity();
        for (String s : data.keySet()) {
            Field field = entity.getField(s);
            updatedField.add(field.getName());
        }
        for (String f : er.getEntity().getFields().values().stream().map(Field::getName).collect(Collectors.toList())) {
            if (!updatedField.contains(f)) {
                er.remove(f);
            }
        }

        EntityRecord.ValidationResult validation = er.validate();
        if (!validation.isValid())
            throw new EntityRecordValidationException(er, validation);

        return update(er);
    }

    @Override
    public DataResult<EntityRecord> partialUpdate(String lk, Map<String, Object> data) throws FieldConversionException, EntityRecordNotFoundException {
        DataResult<EntityRecord> record = getRecordCeckingSingleEntityRec(lk);
        EntityRecord er = record.getData();
        er.set(data);
        return update(er);
    }

    @Override
    public DataResult<EntityRecord> update(EntityRecord record) throws FieldConversionException {
        // TODO for entity ref add a MODE to check if all the references exist (also in persistence manager)
        return this.persistenceEntityDataManager.update(record);
    }

    @Override
    public DataResult<EntityRecord> delete(String id) throws EntityRecordNotFoundException, FieldConversionException, EntitySingleRecordException {
        if (entity.isSingleRecord())
            throw new EntitySingleRecordException(entity, "Delete record is not allowed for single record entities");
        DataResult<EntityRecord> record = getRecord(id, null);
        EntityRecord er = record.getData();
        return this.persistenceEntityDataManager.delete(er);
    }

    private DataResult<EntityRecord> getRecordOrCreate(String lk) throws FieldConversionException, EntityRecordNotFoundException {
        try {
            return getRecord(lk, null);
        } catch (EntityRecordNotFoundException e) {
            if (entity.isSingleRecord() && !entity.getLkSingleRecValue().equals(lk))
                throw e;
            return DataResult.from(new EntityRecord(entity));
        }
    }

    private DataResult<EntityRecord> getRecordCeckingSingleEntityRec(String lk) throws FieldConversionException, EntityRecordNotFoundException {
        try {
            return getRecord(lk, null);
        } catch (EntityRecordNotFoundException e) {
            if (entity.isSingleRecord())
                return DataResult.from(new EntityRecord(entity));
            throw e;
        }
    }
}
