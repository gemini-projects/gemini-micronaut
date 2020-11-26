package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.DuplicateLkRecordException;
import it.at7.gemini.micronaut.exception.EntityRecordNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class PersistenceEntityDataManagerImpl implements PersistenceEntityDataManager {

    private Map<String, Map<String, EntityRecord>> store = new HashMap<>();

    @Override
    public DataListResult<EntityRecord> getRecords(Entity entity, DataListRequest dataListRequest) throws FieldConversionException {
        List<EntityRecord> list = new ArrayList<>();
        for (EntityRecord entityRecord : store.get(entity.getName()).values()) {
            EntityRecord from = PersistedEntityRecord.from(entityRecord);
            list.add(from);
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
}
