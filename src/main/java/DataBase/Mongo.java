package DataBase;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Mongo {
    private MongoClientURI mongoClientURI;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public Mongo ()
    {
        String URL = "mongodb+srv://fares_atef:fares12fares@cluster0.u3zf1oz.mongodb.net/?retryWrites=true&w=majority";
        mongoClientURI = new MongoClientURI(URL);
        mongoClient = new MongoClient(mongoClientURI);
        database = mongoClient.getDatabase("myFirstDatabase");
    }

    public void CreateCollections()
    {
        database.createCollection("URLsToCrawl");
        database.createCollection("VisitedURLsCount");
        database.createCollection("VisitedURLsContentHash");
        database.createCollection("DisallowedURLs");
        database.createCollection("Indexer");
        database.createCollection("SearchHistory");
    }

    public void DropCollections()
    {
        MongoCollection<org.bson.Document> collection=database.getCollection("URLsToCrawl");
        collection.drop();
        collection=database.getCollection("VisitedURLsCount");
        collection.drop();
        collection=database.getCollection("VisitedURLsContentHash");
        collection.drop();
        collection=database.getCollection("DisallowedURLs");
        collection.drop();
        collection=database.getCollection("Indexer");
        collection.drop();
        collection=database.getCollection("SearchHistory");
        collection.drop();
    }

    public void SaveCrawlerState(ConcurrentLinkedQueue<String> URLsToCrawl, ConcurrentHashMap<String,Integer> VisitedURLsCount,ConcurrentHashMap<String,String> VisitedURLsContentHash ,ConcurrentLinkedQueue<String> DisallowedURLs)
    {
        MongoCollection<org.bson.Document> collection = database.getCollection("URLsToCrawl");
        collection.drop();
        List<org.bson.Document> Documents1 = new ArrayList<>();
        for (String s : URLsToCrawl)
            Documents1.add(new Document("URL", s));
        collection.insertMany(Documents1);

        collection = database.getCollection("VisitedURLsCount");
        collection.drop();
        List<org.bson.Document> Documents2 = new ArrayList<>();
        for (Map.Entry<String, Integer> row : VisitedURLsCount.entrySet())
        {
            Document temp_doc = new Document("URL", row.getKey()).append("Count", row.getValue());
            Documents2.add(temp_doc);
        }
        collection.insertMany(Documents2);

        collection = database.getCollection("VisitedURLsContentHash");
        collection.drop();
        List<org.bson.Document> Documents3 = new ArrayList<>();
        for (Map.Entry<String, String> row : VisitedURLsContentHash.entrySet())
        {
            Document temp_doc = new Document("Hash", row.getKey()).append("URL", row.getValue());
            Documents3.add(temp_doc);
        }
        collection.insertMany(Documents3);

        collection = database.getCollection("DisallowedURLs");
        collection.drop();
        List<org.bson.Document> Documents4 = new ArrayList<>();
        for (String s : DisallowedURLs)
        {
            Document temp_doc = new Document("URL", s);
            Documents4.add(temp_doc);
        }
        collection.insertMany(Documents4);
    }

    public void LoadPrevState(ConcurrentLinkedQueue<String> URLsToCrawl, ConcurrentHashMap<String,Integer> VisitedURLsCount,ConcurrentHashMap<String,String> VisitedURLsContentHash ,ConcurrentLinkedQueue<String> DisallowedURLs)
    {
        MongoCollection<org.bson.Document> collection = database.getCollection("URLsToCrawl");
        for (Document doc : collection.find())
            URLsToCrawl.add(doc.getString("URL"));
        collection = database.getCollection("VisitedURLsCount");
        for (Document doc : collection.find())
            VisitedURLsCount.put(doc.getString("URL") ,doc.getInteger("Count"));

        collection = database.getCollection("VisitedURLsContentHash");
        for (Document doc : collection.find())
            VisitedURLsContentHash.put(doc.getString("Hash") , doc.getString("URL"));

        collection = database.getCollection("DisallowedURLs");
        for (Document doc : collection.find())
            DisallowedURLs.add(doc.getString("URL"));
    }

    public static void main(String[] args)
    {
        Mongo mon=new Mongo();
        ConcurrentLinkedQueue<String> URLsToCrawl=new ConcurrentLinkedQueue<>();
        ConcurrentHashMap<String,Integer> VisitedURLsCount=new ConcurrentHashMap<>();
        ConcurrentHashMap<String,String> VisitedURLsContentHash =new ConcurrentHashMap<>();
        ConcurrentLinkedQueue<String> DisallowedURLs=new ConcurrentLinkedQueue<>();
        mon.LoadPrevState(URLsToCrawl,VisitedURLsCount,VisitedURLsContentHash,DisallowedURLs);

        System.out.println(URLsToCrawl);
        System.out.println(VisitedURLsCount);
        System.out.println(VisitedURLsContentHash);
        System.out.println(DisallowedURLs);
    }
}