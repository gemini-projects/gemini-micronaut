package it.at7.gemini.micronaut.firebase;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import io.micronaut.context.annotation.Value;
import it.at7.gemini.micronaut.core.*;
import it.at7.gemini.micronaut.exception.DuplicateLkRecordException;
import it.at7.gemini.micronaut.exception.EntityFieldNotFoundException;
import it.at7.gemini.micronaut.exception.EntityRecordNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@DefaultEntityDataManager
public class PersistenceEntityDataManagerImpl implements PersistenceEntityDataManager {

    private Firestore db;

    @Value("${gemini.firebase.projectId}")
    String projectId;

    @Value("${gemini.firebase.collectionsPrefix:}")
    String collectionsPrefix;

    public PersistenceEntityDataManagerImpl() {
    }

    public PersistenceEntityDataManagerImpl(String projectId, InputStream serviceAccount, String collectionsPrefix, String firebaseName) {
        this.projectId = projectId;
        this.collectionsPrefix = collectionsPrefix;

        try {
            FirebaseOptions.Builder builder = FirebaseOptions.builder();
            if (serviceAccount != null)
                builder.setCredentials(GoogleCredentials.fromStream(serviceAccount));
            else
                builder.setCredentials(GoogleCredentials.getApplicationDefault());

            FirebaseOptions options = builder.setProjectId(projectId)
                    .build();
            FirebaseApp firebaseApp = FirebaseApp.initializeApp(options, firebaseName);
            db = FirestoreClient.getFirestore(firebaseApp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    void init() throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId(projectId)
                .build();
        if (FirebaseApp.getApps().isEmpty())
            FirebaseApp.initializeApp(options);

        db = FirestoreClient.getFirestore();
    }

    @Override
    public DataListResult<EntityRecord> getRecords(Entity entity, DataListRequest dataListRequest) throws FieldConversionException, EntityFieldNotFoundException {
        Query query = db.collection(getEntityCollectionName(entity));
        if (!dataListRequest.getFilters().isEmpty()) {
            for (DataListRequest.Filter filter : dataListRequest.getFilters()) {
                query = FieldFilter.fieldFilter(query, entity, filter);
            }
        }
        ApiFuture<QuerySnapshot> future = query.get();
        List<EntityRecord> res = new ArrayList<>();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                EntityRecord persistedEntityRecord = new PersistedEntityRecord(entity);
                persistedEntityRecord.set(document.getData());
                res.add(persistedEntityRecord);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Unable to retrieve documents", e);
        }
        return DataListResult.from(res);
    }

    @Override
    public DataResult<EntityRecord> getRecord(Entity entity, String lk, DataRequest dataRequest) throws EntityRecordNotFoundException, FieldConversionException {
        try {
            DocumentReference document = db
                    .collection(getEntityCollectionName(entity))
                    .document(lk);
            ApiFuture<DocumentSnapshot> future = document.get();
            DocumentSnapshot documentSnapshot = future.get();
            if (!documentSnapshot.exists()) {
                throw new EntityRecordNotFoundException(entity, lk);
            }
            EntityRecord persistedEntityRecord = new PersistedEntityRecord(entity);
            Map<String, Object> data = documentSnapshot.getData();
            if (data == null) {
                throw new RuntimeException("data not found inside firebase document snapshot");
            }
            persistedEntityRecord.set(documentSnapshot.getData());

            DataResult<EntityRecord> result = DataResult.from(persistedEntityRecord);

            if (documentSnapshot.getUpdateTime() != null) {
                result.setLastUpdate(documentSnapshot.getUpdateTime().toDate().getTime());
            }
            return result;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("getRecord critical exception", e);
        }
    }

    @Override
    public DataResult<EntityRecord> add(EntityRecord entityRecord) throws FieldConversionException, DuplicateLkRecordException {
        try {
            String lkString = entityRecord.getLkString();
            DocumentReference document = db
                    .collection(getEntityCollectionName(entityRecord.getEntity()))
                    .document(entityRecord.getLkString());
            ApiFuture<DocumentSnapshot> future = document.get();
            DocumentSnapshot documentSnapshot = future.get();
            if (documentSnapshot.exists()) {
                throw new DuplicateLkRecordException(entityRecord.getEntity(), lkString);
            }

            Map<String, Object> docData = entityRecord.getData();
            Map<String, Object> convertedData = DataConverter.toDataStoreMap(entityRecord.getEntity(), docData);
            ApiFuture<WriteResult> newRec = document
                    .set(convertedData);
            WriteResult writeResult = newRec.get();
            EntityRecord per = PersistedEntityRecord.from(entityRecord);
            return DataResult.from(per).setLastUpdate(writeResult.getUpdateTime().toDate().getTime());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("addRecord critical exception", e);
        }
    }

    @Override
    public DataListResult<EntityRecord> addAll(Entity entity, List<EntityRecord> entityRecordList) throws FieldConversionException {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public DataResult<EntityRecord> update(EntityRecord entityRecord) throws FieldConversionException {
        try {
            // check if the key was changed
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
            String actualLk = entityRecord.getLkString();
            if (needNewKey && !toFindLk.equals(actualLk)) {
                delete(entityRecord.getEntity(), toFindLk);
            }

            Map<String, Object> docData = entityRecord.getData();
            Map<String, Object> convertedData = DataConverter.toDataStoreMap(entityRecord.getEntity(), docData);

            // add new record to the store
            ApiFuture<WriteResult> newRec = db
                    .collection(getEntityCollectionName(entityRecord.getEntity()))
                    .document(entityRecord.getLkString())
                    .set(convertedData);
            WriteResult writeResult = newRec.get();
            EntityRecord per = PersistedEntityRecord.from(entityRecord);
            return DataResult.from(per).setLastUpdate(writeResult.getUpdateTime().toDate().getTime());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("updateRecord critical exception", e);
        }
    }

    @Override
    public DataResult<EntityRecord> delete(EntityRecord entityRecord) throws FieldConversionException {
        try {
            String lkString = entityRecord.getLkString();
            WriteResult writeResult = delete(entityRecord.getEntity(), lkString);
            DataResult<EntityRecord> retVal = DataResult.from(entityRecord);
            retVal.setLastUpdate(writeResult.getUpdateTime().toDate().getTime());
            return retVal;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("delete critical exception", e);
        }
    }

    private WriteResult delete(Entity entity, String lkString) throws ExecutionException, InterruptedException {
        DocumentReference document = db
                .collection(getEntityCollectionName(entity))
                .document(lkString);

        ApiFuture<WriteResult> delete = document.delete();
        return delete.get();
    }

    @Override
    public Map<String, EntityTimes> times() {
        try {
            DocumentReference document = db
                    .collection(collectionsPrefix + "SUMMARY")
                    .document("default");

            ApiFuture<DocumentSnapshot> future = document.get();
            DocumentSnapshot documentSnapshot = future.get();
            if (!documentSnapshot.exists()) {
                return Map.of();
            }
            Map<String, Object> data = documentSnapshot.getData();
            if (data == null) {
                throw new RuntimeException("data not found inside firebase document snapshot");
            }
            Map<String, EntityTimes> ret = new HashMap<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Map<String, Object> value = (Map<String, Object>) entry.getValue();
                String updateISO = (String) value.get("UPDATE_time");
                String createISO = (String) value.get("NEW_time");
                String deleteISO = (String) value.get("DELETE_time");
                long updateUNIX = 0L;
                long createUNIX = 0L;
                long deleteUNIX = 0L;
                if (updateISO != null) {
                    updateUNIX = Instant.parse(updateISO).toEpochMilli();
                }
                if (createISO != null) {
                    createUNIX = Instant.parse(createISO).toEpochMilli();
                }
                if (deleteISO != null) {
                    deleteUNIX = Instant.parse(deleteISO).toEpochMilli();
                }
                ret.put(entry.getKey(), new EntityTimes(createUNIX, updateUNIX, deleteUNIX));
            }

            return ret;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("times critical exception", e);
        }
    }

    private String getEntityCollectionName(Entity entity) {
        return collectionsPrefix + entity.getName();
    }
}
