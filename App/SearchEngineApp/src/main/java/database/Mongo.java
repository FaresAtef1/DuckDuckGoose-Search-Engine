package database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import org.bson.Document;

import javax.print.Doc;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Mongo {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    private static boolean isConnectionEstablished = false;

    public Mongo ()
    {
        if(isConnectionEstablished)
            return;
        isConnectionEstablished = true;
        mongoClient = new MongoClient("localhost", 27017);
        database = mongoClient.getDatabase("SearchEngineDataBase");
    }

    public void  closeConnection()
    {
        mongoClient.close();
        isConnectionEstablished = false;
    }
    public void CreateCollections()
    {
        database.createCollection("URLsToCrawl");
        database.createCollection("outLinks");
        database.createCollection("VisitedURLsContentHash");
        database.createCollection("PageRankScores");
        database.createCollection("Indexer");
        database.createCollection("Snippets");
        database.createCollection("Titles");
    }

    public void DropCollections()
    {
        MongoCollection<org.bson.Document> collection=database.getCollection("URLsToCrawl");
        collection.drop();
        collection=database.getCollection("outLinks");
        collection.drop();
        collection=database.getCollection("VisitedURLsContentHash");
        collection.drop();
        collection=database.getCollection("PageRankScores");
        collection.drop();
        collection=database.getCollection("Indexer");
        collection.drop();
        collection=database.getCollection("Snippets");
        collection.drop();
        collection=database.getCollection("Titles");
        collection.drop();
    }

    public void SaveCrawlerState(ConcurrentLinkedQueue<String> URLsToCrawl,ConcurrentHashMap<String,Set<String>> outLinks, ConcurrentHashMap<String,String> VisitedURLsContentHash , ConcurrentLinkedQueue<String> DisallowedURLs)
    {
        MongoCollection<org.bson.Document> collection = database.getCollection("URLsToCrawl");
        collection.drop();
        List<org.bson.Document> Documents1 = new ArrayList<>();
        for (String s : URLsToCrawl)
            Documents1.add(new Document("URL", s));
        if(Documents1.size() > 0)
            collection.insertMany(Documents1);

        collection = database.getCollection("outLinks");
        collection.drop();
        List<org.bson.Document> Documents3 = new ArrayList<>();
        for(Map.Entry<String,Set<String>> entry:outLinks.entrySet())
        {
            Document temp_doc = new Document("URL", entry.getKey());
            List<org.bson.Document> temp_list = new ArrayList<>();
            for(String s : entry.getValue()) // for every URL that was an outlink of row.getKey()
            {
                temp_list.add(new Document("URL", s));
            }
            temp_doc.append("outLinksOfThisURL", temp_list);
            Documents3.add(temp_doc);
        }
        if(Documents3.size() > 0)
            collection.insertMany(Documents3);
        collection = database.getCollection("VisitedURLsContentHash");
        collection.drop();
        List<org.bson.Document> Documents4 = new ArrayList<>();
        for (Map.Entry<String, String> row : VisitedURLsContentHash.entrySet())
        {
            Document temp_doc = new Document("Hash", row.getKey()).append("URL", row.getValue());
            Documents4.add(temp_doc);
        }
        if(Documents4.size() > 0)
            collection.insertMany(Documents4);
    }


    public void LoadPrevState(ConcurrentLinkedQueue<String> URLsToCrawl,ConcurrentHashMap<String,Set<String>> outLinks ,ConcurrentHashMap<String,String> VisitedURLsContentHash)
    {
        MongoCollection<org.bson.Document> collection = database.getCollection("URLsToCrawl");
        for (Document doc : collection.find())
            URLsToCrawl.add(doc.getString("URL"));
        collection = database.getCollection("outLinks");
        for (Document doc : collection.find()) {
            String URL = doc.getString("URL");
            Set<String> temp_set = new HashSet<>();
            for(Document outLink : (List<Document>) doc.get("outLinksOfThisURL"))
                temp_set.add(outLink.getString("URL"));
            outLinks.put(URL, temp_set);
        }
        collection = database.getCollection("VisitedURLsContentHash");
        for (Document doc : collection.find())
            VisitedURLsContentHash.put(doc.getString("Hash") , doc.getString("URL"));
    }

    public List<Document> ExecuteQuery(Document query, String collectionName)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        List<Document> results = new ArrayList<>();
        collection.find(query).into(results);
        return results;
    }
    public List<String> ExecuteQueryAndGetDistinct(Document query, String collectionName, String distinctField)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        List<String> results = new ArrayList<>();
        collection.distinct(distinctField,query,String.class).into(results);
        return results;
    }

    public void updateCollection(String collectionName , List<Document> documents)
    {
        if(!isConnectionEstablished)
            ConnectToMongo();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        List<InsertOneModel<Document>> insertOps = new ArrayList<>();
        for (Document document : documents) {
            insertOps.add(new InsertOneModel<>(document));
        }
        BulkWriteOptions options = new BulkWriteOptions().ordered(false);
        BulkWriteResult result = collection.bulkWrite(insertOps, options);
    }
    public void AddToCollection(String collectionName , List<Document> documents)
    {
        if(!isConnectionEstablished)
            ConnectToMongo();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertMany(documents);
    }
    public void AddOneDoc(String collectionName , Document doc)
    {
        if(!isConnectionEstablished)
            ConnectToMongo();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(doc);
    }
    public void RemoveOneDoc(String collectionName , Document doc)
    {
        if(!isConnectionEstablished)
            ConnectToMongo();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.deleteOne(doc);
    }
    public void ConnectToMongo()
    {
        try {
            mongoClient = new MongoClient("localhost", 27017);
            database = mongoClient.getDatabase("SearchEngineDataBase");
            isConnectionEstablished = true;
        }
        catch (Exception e)
        {
            System.out.println("Error in connecting to mongo");
        }
    }

    public static void main(String[] args)
    {
        Mongo mon=new Mongo();
        mon.DropCollections();
        mon.CreateCollections();
    }
}