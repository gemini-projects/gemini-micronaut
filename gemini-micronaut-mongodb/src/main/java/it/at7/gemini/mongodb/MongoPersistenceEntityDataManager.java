package it.at7.gemini.mongodb;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import io.micronaut.context.annotation.Value;
import it.at7.gemini.micronaut.core.*;
import it.at7.gemini.micronaut.exception.DuplicateLkRecordException;
import it.at7.gemini.micronaut.exception.EntityFieldNotFoundException;
import it.at7.gemini.micronaut.exception.EntityRecordNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static it.at7.gemini.mongodb.FieldFilter.fieldFilter;
import static it.at7.gemini.mongodb.FieldSorter.fieldSorter;

@DefaultEntityDataManager
public class MongoPersistenceEntityDataManager implements PersistenceEntityDataManager {

    private static final String SUMMARY_COLLECTION = "__SUMMARY_COLLECTION";

    @Value("${gemini.mongodb.url}")
    String url;

    @Value("${gemini.mongodb.db}")
    String dbName;

    MongoClient mongoClient;
    MongoDatabase db;

    public MongoPersistenceEntityDataManager() {
    }

    public MongoPersistenceEntityDataManager(String url, String dbName) {
        this.url = url;
        this.dbName = dbName;
        init();
    }

    @PostConstruct
    public void init() {
        mongoClient = MongoClients.create(url);
        db = mongoClient.getDatabase(dbName);
    }

    @Override
    public DataCountResult countRecords(Entity entity, DataListRequest dataListRequest) throws EntityFieldNotFoundException {
        Bson filter = createFilter(entity, dataListRequest);
        String entityCollectionName = getEntityCollectionName(entity);
        MongoCollection<Document> collection = this.db.getCollection(entityCollectionName);
        long counter = collection.countDocuments(filter);
        return DataCountResult.fromCount(counter);
    }

    @Override
    public DataListResult<EntityRecord> getRecords(Entity entity, DataListRequest dataListRequest) throws EntityFieldNotFoundException, FieldConversionException {
        Bson filter = createFilter(entity, dataListRequest);
        String entityCollectionName = getEntityCollectionName(entity);
        MongoCollection<Document> collection = this.db.getCollection(entityCollectionName);

        FindIterable<Document> documents = collection.find(filter);

        for (DataListRequest.Order order : dataListRequest.getOrders()) {
            fieldSorter(documents, entity, order);
        }

        if (dataListRequest.getStart() > 0) {
            documents.skip(dataListRequest.getStart());
        }

        if (dataListRequest.getLimit() > 0) {
            documents.limit(dataListRequest.getLimit());
        }
        List<EntityRecord> res = new ArrayList<>();
        for (Document doc : documents) {
            Map<String, Object> map = doc.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            EntityRecord persistedEntityRecord = new PersistedEntityRecord(entity);
            persistedEntityRecord.set(map);
            res.add(persistedEntityRecord);
        }

        return DataListResult.from(res);
    }

    private Bson createFilter(Entity entity, DataListRequest dataListRequest) throws EntityFieldNotFoundException {
        Bson filter = new Document(); // empty filter, as documentation say
        List<DataListRequest.Filter> reqFilters = dataListRequest.getFilters();
        if (!reqFilters.isEmpty()) {
            List<Bson> mongoFilters = new ArrayList<>();
            for (DataListRequest.Filter f : reqFilters) {
                mongoFilters.add(fieldFilter(entity, f));
            }
            filter = Filters.and(mongoFilters);
        }
        return filter;
    }


    @Override
    public DataResult<EntityRecord> getRecord(Entity entity, String lk, DataRequest dataRequest) throws EntityRecordNotFoundException, FieldConversionException {
        String entityCollectionName = getEntityCollectionName(entity);
        MongoCollection<Document> collection = this.db.getCollection(entityCollectionName);
        Document document = collection.find(eq("_lk", lk)).first();
        if (document == null) {
            throw new EntityRecordNotFoundException(entity, lk);
        }

        Map<String, Object> map = document.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        EntityRecord persistedEntityRecord = new PersistedEntityRecord(entity);
        persistedEntityRecord.set(map);
        DataResult<EntityRecord> result = DataResult.from(persistedEntityRecord);

        if (document.getDate("_lastUpdate") != null) {
            result.setLastUpdate(document.getDate("_lastUpdate").getTime());
        }
        return result;
    }

    @Override
    public DataResult<EntityRecord> add(EntityRecord entityRecord) throws FieldConversionException, DuplicateLkRecordException {
        String collectionName = getEntityCollectionName(entityRecord.getEntity());
        MongoCollection<Document> collection = this.db.getCollection(collectionName);
        long count = collection.countDocuments(eq("_lk", entityRecord.getLkString()));
        if (count == 0) {
            //Document does not exist

            Document mongoDoc = new Document("_lk", entityRecord.getLkString());
            Date createTime = new Date();

            TransactionBody<InsertOneResult> txnBody = () -> {
                mongoDoc.putAll(entityRecord.getData());
                mongoDoc.put("_lastUpdate", createTime);
                InsertOneResult irs = collection.insertOne(mongoDoc);

                MongoCollection<Document> summaryCollection = db.getCollection(SUMMARY_COLLECTION);
                UpdateResult updateResult = summaryCollection.updateOne(eq("_lk", "default"), Updates.set(collectionName + "." + "NEW_time", createTime));
                if (updateResult.getMatchedCount() == 0)
                    summaryCollection.insertOne(new Document("_lk", "default").append(collectionName, new Document("NEW_time", createTime)));
                return irs;
            };

            ClientSession clientSession = this.mongoClient.startSession();
            InsertOneResult insertOneResult = clientSession.withTransaction(txnBody);
            clientSession.close();

            EntityRecord per = PersistedEntityRecord.from(entityRecord);
            return DataResult.from(per).setLastUpdate(createTime.getTime());
        } else {
            //We found the document
            throw new DuplicateLkRecordException(entityRecord.getEntity(), entityRecord.getLkString());
        }
    }

    @Override
    public DataListResult<EntityRecord> addAll(Entity entity, List<EntityRecord> entityRecordList) throws FieldConversionException {
        String collectionName = getEntityCollectionName(entity);
        MongoCollection<Document> collection = this.db.getCollection(collectionName);

        ClientSession clientSession = this.mongoClient.startSession();
        clientSession.startTransaction();
        Date createTime = new Date();

        List<Document> docs = new ArrayList<>(entityRecordList.size());
        for (EntityRecord entityRecord : entityRecordList) {
            long count = collection.countDocuments(clientSession, eq("_lk", entityRecord.getLkString()));
            if (count == 0) {
                Document mongoDoc = new Document("_lk", entityRecord.getLkString());
                mongoDoc.putAll(entityRecord.getData());
                mongoDoc.put("_lastUpdate", createTime);
                docs.add(mongoDoc);

            } else {
                // We found the document // TODO exception
                // throw new DuplicateLkRecordException(entityRecord.getEntity(), entityRecord.getLkString());


            }
        }
        collection.insertMany(clientSession, docs);
        MongoCollection<Document> summaryCollection = db.getCollection(SUMMARY_COLLECTION);
        UpdateResult updateResult = summaryCollection.updateOne(eq("_lk", "default"), Updates.set(collectionName + "." + "NEW_time", createTime));
        if (updateResult.getMatchedCount() == 0)
            summaryCollection.insertOne(new Document("_lk", "default").append(collectionName, new Document("NEW_time", createTime)));

        clientSession.commitTransaction();

        List<EntityRecord> res = new ArrayList<>(entityRecordList.size());
        for (EntityRecord rec : entityRecordList) {
            res.add(PersistedEntityRecord.from(rec));
        }
        return DataListResult.from(res).setLastUpdate(createTime.getTime());
    }

    @Override
    public DataResult<EntityRecord> update(EntityRecord entityRecord) throws FieldConversionException {
        String lkString = entityRecord.getLkString();
        String collectionName = getEntityCollectionName(entityRecord.getEntity());

        MongoCollection<Document> collection = this.db.getCollection(collectionName);
        Date updateTime = new Date();

        // check if the key was changed
        String toFindLk = entityRecord.getLkString();
        if (entityRecord instanceof PersistedEntityRecord) {
            PersistedEntityRecord pr = (PersistedEntityRecord) entityRecord;
            List<String> changedLkFields = pr.getChangedLkFields();
            if (!changedLkFields.isEmpty()) {
                toFindLk = pr.getLastLkString();
            }
        }
        final String toFindLkFinal = toFindLk;
        TransactionBody<Document> txnBody = () -> {
            Document theDoc = new Document();
            theDoc.putAll(entityRecord.getData());
            theDoc.put("_lastUpdate", updateTime);
            theDoc.put("_lk", lkString);
            Document doc = collection.findOneAndReplace(eq("_lk", toFindLkFinal), theDoc);
            if (doc == null) {
                if (!entityRecord.getEntity().isSingleRecord())
                    throw new RuntimeException(String.format("record %s not found", lkString));
                else
                    collection.insertOne(theDoc);
            }

            MongoCollection<Document> summaryCollection = db.getCollection(SUMMARY_COLLECTION);
            UpdateResult updateResult = summaryCollection.updateOne(eq("_lk", "default"), Updates.set(collectionName + "." + "UPDATE_time", updateTime));
            if (updateResult.getMatchedCount() == 0) {
                summaryCollection.insertOne(new Document("_lk", "default").append(collectionName, new Document("UPDATE_time", updateTime)));
            }

            return doc;
        };

        ClientSession clientSession = this.mongoClient.startSession();
        clientSession.withTransaction(txnBody);
        clientSession.close();

        EntityRecord per = PersistedEntityRecord.from(entityRecord);
        return DataResult.from(per).setLastUpdate(updateTime.getTime());
    }

    @Override
    public DataResult<EntityRecord> delete(EntityRecord entityRecord) throws FieldConversionException {
        String lkString = entityRecord.getLkString();
        String collectionName = getEntityCollectionName(entityRecord.getEntity());

        MongoCollection<Document> collection = this.db.getCollection(collectionName);
        Date deleteTime = new Date();

        TransactionBody<DeleteResult> txnBody = () -> {
            DeleteResult res = collection.deleteOne(eq("_lk", lkString));
            if (res.getDeletedCount() == 0)
                throw new RuntimeException(String.format("Deletion error for %s - %s", entityRecord.getEntity().getName(), lkString));

            MongoCollection<Document> summaryCollection = db.getCollection(SUMMARY_COLLECTION);
            UpdateResult updateResult = summaryCollection.updateOne(eq("_lk", "default"), Updates.set(collectionName + "." + "DELETE_time", deleteTime));
            if (updateResult.getMatchedCount() == 0) {
                summaryCollection.insertOne(new Document("_lk", "default").append(collectionName, new Document("DELETE_time", deleteTime)));
            }
            return res;
        };

        ClientSession clientSession = this.mongoClient.startSession();
        clientSession.withTransaction(txnBody);
        clientSession.close();

        DataResult<EntityRecord> retVal = DataResult.from(entityRecord);
        retVal.setLastUpdate(deleteTime.getTime());
        return retVal;
    }

    @Override
    public Map<String, EntityTimes> times() {
        MongoCollection<Document> summaryCollection = db.getCollection(SUMMARY_COLLECTION);
        Document summary = summaryCollection.find(eq("_lk", "default")).first();
        if (summary == null)
            return Map.of();

        Map<String, EntityTimes> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : summary.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("_")) {
                Map<String, Object> value = (Map<String, Object>) entry.getValue();
                Date update = (Date) value.get("UPDATE_time");
                Date create = (Date) value.get("NEW_time");
                Date delete = (Date) value.get("DELETE_time");
                long updateUNIX = 0L;
                long createUNIX = 0L;
                long deleteUNIX = 0L;
                if (update != null) {
                    updateUNIX = update.toInstant().toEpochMilli();
                }
                if (create != null) {
                    createUNIX = create.toInstant().toEpochMilli();
                }
                if (delete != null) {
                    deleteUNIX = delete.toInstant().toEpochMilli();
                }
                ret.put(key, new EntityTimes(createUNIX, updateUNIX, deleteUNIX));
            }
        }

        return ret;

    }

    private String getEntityCollectionName(Entity entity) {
        return entity.getName().toUpperCase();
    }
}
