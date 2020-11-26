package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.*;

import java.util.ArrayList;
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
    public DataListResult<EntityRecord> getRecords(DataListRequest dataListRequest) throws FieldConversionException {
        return this.persistenceEntityDataManager.getRecords(this.entity, dataListRequest);
    }

    @Override
    public DataResult<EntityRecord> getRecord(String lk, DataRequest dataRequest) throws EntityRecordNotFoundException, FieldConversionException {
        return this.persistenceEntityDataManager.getRecord(this.entity, lk, dataRequest);
    }

    @Override
    public DataResult<EntityRecord> add(Map<String, Object> data) throws FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException {
        EntityRecord entityRecord = EntityRecord.fromDataMap(getEntity(), data);
        return this.add(entityRecord);
    }

    @Override
    public DataResult<EntityRecord> add(EntityRecord er) throws FieldConversionException, DuplicateLkRecordException, EntityRecordValidationException {
        EntityRecord.ValidationResult validation = er.validate();
        if (!validation.isValid())
            throw new EntityRecordValidationException(er, validation);
        return this.persistenceEntityDataManager.add(er);
    }

    @Override
    public DataResult<EntityRecord> fullUpdate(String lk, Map<String, Object> data) throws EntityRecordNotFoundException, FieldConversionException, EntityFieldNotFoundException {
        DataResult<EntityRecord> record = getRecord(lk, null);
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

        return update(er);
    }

    @Override
    public DataResult<EntityRecord> partialUpdate(String lk, Map<String, Object> data) throws FieldConversionException, EntityRecordNotFoundException, EntityFieldNotFoundException {
        DataResult<EntityRecord> record = getRecord(lk, null);
        EntityRecord er = record.getData();
        er.set(data);
        return update(er);
    }

    @Override
    public DataResult<EntityRecord> update(EntityRecord record) throws FieldConversionException {
        return this.persistenceEntityDataManager.update(record);
    }

    @Override
    public DataResult<EntityRecord> delete(String id) throws EntityRecordNotFoundException, FieldConversionException {
        DataResult<EntityRecord> record = getRecord(id, null);
        EntityRecord er = record.getData();
        return this.persistenceEntityDataManager.delete(er);
    }
}
