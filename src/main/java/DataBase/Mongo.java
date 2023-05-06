package DataBase;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.print.Doc;
import java.util.*;
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
        database.createCollection("inLinks");
        database.createCollection("outLinks");
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
        collection=database.getCollection("inLinks");
        collection.drop();
        collection=database.getCollection("outLinks");
        collection.drop();
        collection=database.getCollection("DisallowedURLs");
        collection.drop();
        collection=database.getCollection("Indexer");
        collection.drop();
        collection=database.getCollection("SearchHistory");
        collection.drop();
    }

    public void SaveCrawlerState(ConcurrentLinkedQueue<String> URLsToCrawl, ConcurrentHashMap<String, Set<String>> inLinks,ConcurrentHashMap<String,Set<String>> outLinks, ConcurrentHashMap<String,String> VisitedURLsContentHash , ConcurrentLinkedQueue<String> DisallowedURLs)
    {
        MongoCollection<org.bson.Document> collection = database.getCollection("URLsToCrawl");
        collection.drop();
        List<org.bson.Document> Documents1 = new ArrayList<>();
        for (String s : URLsToCrawl)
            Documents1.add(new Document("URL", s));
        collection.insertMany(Documents1);
        ////////////////////////////////////////////////////////////////
        collection = database.getCollection("inLinks");
        collection.drop();
        List<org.bson.Document> Documents2 = new ArrayList<>();
        for (Map.Entry<String, Set<String>> row : inLinks.entrySet()) // for each url in inlinks
        {
            Document temp_doc2 = new Document("URL", row.getKey());
            List<org.bson.Document> temp_list = new ArrayList<>();
            for(String s : row.getValue()) // for every URL that has row.getKey() as an outlink
            {
                temp_list.add(new Document("URL", s));
            }
            temp_doc2.append("inLinksOfThisURL", temp_list);
            Documents2.add(temp_doc2);
        }
        collection.insertMany(Documents2);
        ///////////////////////////////////////////////////////////////// edited by amr
        collection = database.getCollection("outLinksCount");
        collection.drop();
        List<org.bson.Document> outLinks_temp=new ArrayList<>();
        for(Map.Entry<String,Set<String>> entry:outLinks.entrySet())
        {
            Document temp_doc = new Document("URL", entry.getKey());
            List<org.bson.Document> temp_list = new ArrayList<>();
            for(String s : entry.getValue()) // for every URL that was an outlink of row.getKey()
            {
                temp_list.add(new Document("URL", s));
            }
            temp_doc.append("outLinksOfThisURL", temp_list);
            Documents2.add(temp_doc);
        }
        collection.insertMany(outLinks_temp);
        ////////////////////////////////////////////////////////////////
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

    public void LoadPrevState(ConcurrentLinkedQueue<String> URLsToCrawl, ConcurrentHashMap<String,Set<String>> inLinks,ConcurrentHashMap<String,Integer> outLinksCount ,ConcurrentHashMap<String,String> VisitedURLsContentHash ,ConcurrentLinkedQueue<String> DisallowedURLs)
    {
        MongoCollection<org.bson.Document> collection = database.getCollection("URLsToCrawl");
        for (Document doc : collection.find())
            URLsToCrawl.add(doc.getString("URL"));
        /////////////////////////////////////////////////////////edited by amr
        collection = database.getCollection("inLinks");
        for (Document doc : collection.find()) {
            String URL = doc.getString("URL");
            Set<String> temp_set = new HashSet<>();
            for(Document inlink : (List<Document>) doc.get("inLinksOfThisURL"))////////is this correct?
                temp_set.add(inlink.getString("URL"));
            inLinks.put(URL, temp_set);
        }
        /////////////////////////////////////////////////////////edited by amr
        collection = database.getCollection("outLinks");
        for (Document doc : collection.find()) {
            String URL = doc.getString("URL");
            Set<String> temp_set = new HashSet<>();
            for(Document inlink : (List<Document>) doc.get("outLinksOfThisURl"))////////is this correct?
                temp_set.add(inlink.getString("URL"));
            inLinks.put(URL, temp_set);
        }
        /////////////////////////////////////////////////////////

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
        ConcurrentHashMap<String,Set<String>> inLinks=new ConcurrentHashMap<>();
        ConcurrentHashMap<String,Integer> outLinksCount=new ConcurrentHashMap<>();
        ConcurrentHashMap<String,String> VisitedURLsContentHash =new ConcurrentHashMap<>();
        ConcurrentLinkedQueue<String> DisallowedURLs=new ConcurrentLinkedQueue<>();
        mon.LoadPrevState(URLsToCrawl,inLinks,outLinksCount,VisitedURLsContentHash,DisallowedURLs);

        System.out.println(URLsToCrawl);
        System.out.println(inLinks);
        System.out.println(outLinksCount);
        System.out.println(VisitedURLsContentHash);
        System.out.println(DisallowedURLs);
    }
}