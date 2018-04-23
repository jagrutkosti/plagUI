package com.plagui.modules.docdetails;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.plagui.config.Constants;
import com.plagui.repository.TimestampsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class TimestampsService {
    private final Logger log = LoggerFactory.getLogger(TimestampsService.class);

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Inject
    private TimestampsRepository timestampsRepository;

    /**
     * Save a timestamps.
     * @return the persisted entity
     */
    public Timestamps save(Timestamps timestamps) {
        log.info("Request to save Timestamps : {}", timestamps);
        Timestamps result = timestampsRepository.save(timestamps);
        return result;
    }

    /**
     *  get all the timestamps.
     *  @return the list of entities
     */
    public List<Timestamps> findAll() {
        log.info("Request to get all timestamps");
        List<Timestamps> result = timestampsRepository.findAll();
        return result;
    }

    /**
     *  get one timestamp by id.
     *  @return the entity
     */
    public Timestamps findOne(String id) {
        log.info("Request to get Timestamps : {}", id);
        Timestamps timestamps = timestampsRepository.findOne(id);
        return timestamps;
    }

    /**
     *  delete the timestamp by id.
     */
    public void delete(String id) {
        log.info("Request to delete Timestamps : {}", id);
        timestampsRepository.delete(id);
    }

    /**
     * delete ALL publishedWork in Database.
     */
    public void deleteAll(){
        log.info("Request to delete all timestamps");
        timestampsRepository.deleteAll();
    }

    /**
     * Use only for fetching very large number of documents.
     * Get all documents from Timestamps collection.
     * Using MongoTemplate and DBCursor to iterate over millions of records.
     * @return {DBCursor} to iterate over all documents in the collection
     */
    public DBCursor find() {
        log.info("Request to get all PublishedWorks with DBCursor");
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(), databaseName));
        DBCollection dbCollection = mongoTemplate.getCollection(Constants.TIMESTAMP_COLLECTION_NAME);
        return dbCollection.find();
    }

    /**
     * Use only for fetching very large number of documents.
     * Get all documents from Timestamps collection which satisfy the given criteria.
     * Using MongoTemplate and DBCursor to iterate over millions of records.
     * @return {DBCursor} to iterate over all documents in the collection
     */
    public DBCursor find(BasicDBObject query) {
        log.info("Request to get DBCurosr for all PublishedWorks for given query");
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(), databaseName));
        DBCollection dbCollection = mongoTemplate.getCollection(Constants.TIMESTAMP_COLLECTION_NAME);
        return dbCollection.find(query);
    }
}
