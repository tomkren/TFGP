package net.fishtron.server.managers;

import net.fishtron.utils.F;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/** Created by sekol on 9.12.2016.*/

public class MongoManager implements Manager {



    @Override
    public String greetings() {
        return "MongoManager serving you humongous JSONs from "+dbName+"@"+host+":"+port+", bon appétit.";
    }

    private final MongoClient mongoClient;
    private final MongoDatabase db;

    private final String host;
    private final int port;
    private final String dbName;

    public MongoManager(JSONObject mongoConfig) {

        host = mongoConfig.getString("host");
        port = mongoConfig.getInt("port");
        dbName = mongoConfig.getString("db");

        mongoClient = new MongoClient(host, port);
        db = mongoClient.getDatabase(dbName);
    }

    // TODO mega-prasopes, udělat jak v mongu !!!
    private static String TIME_KEY = "__TIME__";

    public static JSONObject encodeTime(Instant instant) {
        return F.obj(TIME_KEY, instant.toString());
    }


    public static Document toDoc(JSONObject obj) {
        Document ret = new Document();
        for (String key : obj.keySet()) {
            Object val = obj.get(key);
            ret.append(key, toVal(val));
        }
        return ret;
    }

    private static Object toVal(Object val) {
        if (val instanceof JSONObject) {
            JSONObject obj = (JSONObject) val;

            if (obj.length() == 1 && obj.has(TIME_KEY)) {
                String time_str = obj.getString(TIME_KEY);
                return Date.from(Instant.parse(time_str));
            }

            return toDoc(obj);

        } else if (val instanceof JSONArray) {
            return F.map((JSONArray) val, MongoManager::toVal);
        } else if (val.equals(JSONObject.NULL)) {
            return null;
        } else {
            return val;
        }
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return db.getCollection(collectionName);
    }

    public void insert(String collectionName, JSONObject jsonDoc) {
        insert_internal(collectionName, toDoc(jsonDoc));
    }

    public void insert(String collectionName, List<JSONObject> jsonDocs) {
        if (jsonDocs.isEmpty()) {return;}
        List<Document> docs = F.map(jsonDocs, MongoManager::toDoc);
        insert_internal(collectionName, docs);
    }

    private void insert_internal(String collectionName, Document doc) {
        MongoCollection<Document> collection = db.getCollection(collectionName);
        collection.insertOne(doc);
    }

    private void insert_internal(String collectionName, List<Document> docs) {
        MongoCollection<Document> collection = db.getCollection(collectionName);
        collection.insertMany(docs);
    }


    public JSONArray find(String collectionName) {
        return find(collectionName, new Document(), new Document());
    }

    public JSONArray find(String collectionName, Bson filter) {
        return find(collectionName, filter, new Document());
    }

    public JSONArray find(String collectionName, Bson filter, Bson projection) {
        JSONArray ret = new JSONArray();
        MongoCollection<Document> collection = db.getCollection(collectionName);
        FindIterable<Document> documents = collection.find(filter).projection(projection);
        try (MongoCursor<Document> cursor = documents.iterator()) {
            while (cursor.hasNext()) {
                String bsonStr = cursor.next().toJson();
                ret.put(bsonStr2json(bsonStr));
            }
        }
        return ret;
    }

    private static JSONObject bsonStr2json(String bsonStr) {
        JSONObject rawJson = new JSONObject(bsonStr);
        Object jsonObj = rawObj2json(rawJson);
        if (!(jsonObj instanceof JSONObject)) {throw new Error("bsonStr2json result must be a JSONObject, but it is: "+jsonObj);}
        return (JSONObject) jsonObj;
    }

    private static Object raw2json(Object raw) {
        if (raw instanceof JSONArray) {
            return F.jsonMap((JSONArray) raw, MongoManager::raw2json);
        } else if (raw instanceof JSONObject) {
            return rawObj2json((JSONObject) raw);
        } else {
            return raw;
        }
    }

    private static Object rawObj2json(JSONObject raw) {
        if (raw.has("$numberLong")) {
            if (raw.length() != 1) {throw new Error("unexpected number of key-val pairs in: "+raw);}
            return Long.parseLong(raw.getString("$numberLong"));
        }

        return F.jsonMap(raw, MongoManager::raw2json);
    }


    public JSONObject findFirst(String collectionName, JSONObject filter) {
        return findFirst(collectionName, toDoc(filter), new Document());
    }

    public JSONObject findFirst(String collectionName, Bson filter) {
        return findFirst(collectionName, filter, new Document());
    }

    public JSONObject findFirst(String collectionName, Bson filter, Bson projection) {
        MongoCollection<Document> collection = db.getCollection(collectionName);
        Document doc = collection.find(filter).projection(projection).first();
        return doc != null ? bsonStr2json(doc.toJson()) : null;
    }

    public void deleteFirst(String collectionName, Bson filter) {
        //return findFirst(collectionName, filter, new Document());
        getCollection(collectionName).deleteOne(filter);
    }

    public long count(String collectionName, Bson filter) {
        MongoCollection<Document> collection = db.getCollection(collectionName);
        return collection.count(filter);
    }

    public JSONArray distinctStrings(String collectionName, String keyName) {
        MongoCollection<Document> collection = db.getCollection(collectionName);
        JSONArray ret = new JSONArray();
        Consumer<String> put = ret::put;
        collection.distinct(keyName, String.class).forEach(put);
        return ret;
    }

    public void close() {
        mongoClient.close();
    }

    // TODO remove
    /*public static void main(String[] args) {
        F.log("Ahoj!");

        Random r = new Random();

        MongoManager mongoMan = new MongoManager(loadConfig());

        Document doc = new Document()
                .append("_id", "test-job-"+r.nextLong())
                .append("process", "test")
                .append("delay", r.nextInt(10000))
                .append("period", r.nextInt(3000));

        mongoMan.insert_internal("jobs_test", doc);

        mongoMan.close();
    }*/



}
