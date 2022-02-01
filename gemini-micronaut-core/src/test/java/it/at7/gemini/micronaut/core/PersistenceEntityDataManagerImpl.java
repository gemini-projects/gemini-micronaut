package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.DuplicateLkRecordException;
import it.at7.gemini.micronaut.exception.EntityRecordNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistenceEntityDataManagerImpl implements PersistenceEntityDataManager {

    private Map<String, Map<String, EntityRecord>> store = new HashMap<>();

    @Override
    public DataCountResult countRecords(Entity entity, DataListRequest dataListRequest) {
        Map<String, EntityRecord> entityStore = store.getOrDefault(entity.getName(), new HashMap<>());
        return DataCountResult.fromCount(entityStore.size());
    }

    @Override
    public DataListResult<EntityRecord> getRecords(Entity entity, DataListRequest dataListRequest) throws FieldConversionException {
        List<EntityRecord> list = new ArrayList<>();
        int count = 0;
        for (EntityRecord entityRecord : store.getOrDefault(entity.getName(), Map.of()).values()) {
            EntityRecord from = PersistedEntityRecord.from(entityRecord);
            list.add(from);
            count++;
            if(dataListRequest.getLimit() > 0 && count >= dataListRequest.getLimit())
                break;
        }
        return DataListResult.from(List.copyOf(list));
    }

    @Override
    public DataResult<EntityRecord> getRecord(Entity entity, String lk, DataRequest dataRequest) throws EntityRecordNotFoundException, FieldConversionException {
        Map<String, EntityRecord> entityStore = store.getOrDefault(entity.getName(), new HashMap<>());
        EntityRecord entityRecord = entityStore.get(lk);
        if (entityRecord == null) {
            throw new EntityRecordNotFoundException(entity, lk);
        }
        return DataResult.from(PersistedEntityRecord.from(entityRecord));
    }

    @Override
    public DataResult<EntityRecord> add(EntityRecord entityRecord) throws FieldConversionException, DuplicateLkRecordException {
        String entityStoreKey = entityRecord.getEntity().getName();
        Map<String, EntityRecord> entityStore = store.computeIfAbsent(entityStoreKey, k -> new HashMap<>());
        String lkString = entityRecord.getLkString();
        if (entityStore.containsKey(lkString))
            throw new DuplicateLkRecordException(entityRecord.getEntity(), lkString);
        entityStore.put(lkString, entityRecord);
        return DataResult.from(PersistedEntityRecord.from(entityRecord));
    }

    @Override
    public DataListResult<EntityRecord> addAll(Entity entity, List<EntityRecord> entityRecordList) throws FieldConversionException {
        // TODO
        return null;
    }

    @Override
    public DataResult<EntityRecord> update(EntityRecord entityRecord) throws FieldConversionException {
        String entityStoreKey = entityRecord.getEntity().getName();
        Map<String, EntityRecord> entityStore = store.computeIfAbsent(entityStoreKey, k -> new HashMap<>());

        boolean needNewKey = false;
        String toFindLk = entityRecord.getLkString();
        if (entityRecord instanceof PersistedEntityRecord) {
            PersistedEntityRecord pr = (PersistedEntityRecord) entityRecord;
            List<String> changedLkFields = pr.getChangedLkFields();
            if (!changedLkFields.isEmpty()) {
                needNewKey = true;
                toFindLk = pr.getLastLkString();
            }
        }

        /* if (!entityStore.containsKey(toFindLk))
            throw new EntityRecordNotFoundException(entityRecord.getEntity(), toFindLk); */

        String actualLk = entityRecord.getLkString();
        entityStore.put(actualLk, entityRecord);
        if (needNewKey && !toFindLk.equals(actualLk)) {
            entityStore.remove(toFindLk);
        }

        return DataResult.from(PersistedEntityRecord.from(entityRecord));
    }

    @Override
    public DataResult<EntityRecord> delete(EntityRecord entityRecord) throws FieldConversionException {
        String entityStoreKey = entityRecord.getEntity().getName();
        Map<String, EntityRecord> entityStore = store.computeIfAbsent(entityStoreKey, k -> new HashMap<>());

        String lkString = entityRecord.getLkString();
        entityStore.remove(lkString);
        return DataResult.from(entityRecord);
    }

    @Override
    public Map<String, EntityTimes> times() {
        return Map.of();
    }
}