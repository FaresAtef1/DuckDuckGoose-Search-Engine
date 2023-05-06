package ranker;

import DataBase.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

public class PageRanker {

    private HashMap<String, Double> PageRankScores;
    private HashMap<String, Set<String>> inLinks;

    private HashMap<String, Set<String>> outLinks;

    private static final double dampingFactor = 0.85;

    private static final double threshold = 0.0001;




    public PageRanker(HashMap<String, Set<String>> inwardLinks, HashMap<String, Set<String>> outwardLinks) {
        inLinks= new HashMap<String,Set<String>>();
        outLinks = outwardLinks;
        // Making sure that only crawled pages are considered
        System.out.println("inLinks size: " + inwardLinks.size());
        System.out.println("outLinks size: " + outwardLinks.size());
        for (String key : outLinks.keySet()) {
            inLinks.put(key, inwardLinks.get(key));
       }
        System.out.println("inLinks size: " + inLinks.size());
        System.out.println("outLinks size: " + outLinks.size());
        PageRankScores = new HashMap<>();
        for (String key : inLinks.keySet())
            PageRankScores.put(key, 1.0 / inLinks.size());
    }

    public void CalculatePageRanks() {
        HashMap<String, Double> newPageRankScores = new HashMap<>();
        while (true) {
            double totalSum = 0;
            for (String page : inLinks.keySet()) {
                double sum = 0;
                for (String inlink : inLinks.get(page)) {
                    int outDegree = outLinks.get(inlink).size();
                    sum += PageRankScores.get(inlink) / outDegree;
                }
                double newPageRankScore = (1 - dampingFactor) / inLinks.size() + dampingFactor * sum;
                newPageRankScores.put(page, newPageRankScore);
                totalSum += newPageRankScore;
            }
                if(Converges(PageRankScores, newPageRankScores))
                {
                    PageRankScores=newPageRankScores;
                    this.Normalize(PageRankScores,totalSum);
                    break;
                }
                PageRankScores = newPageRankScores;
                this.Normalize(PageRankScores,totalSum);
            }
        }

    private boolean Converges(HashMap<String, Double> oldPageRankScores, HashMap<String, Double> newPageRankScores) {
        for (String page : oldPageRankScores.keySet()) {
            if (Math.abs(oldPageRankScores.get(page) - newPageRankScores.get(page)) > threshold)
                return false;
        }
        return true;
    }

    private void Normalize(HashMap<String, Double> PageRankScores,double totalSum){
        for (String page : PageRankScores.keySet())
            PageRankScores.put(page, PageRankScores.get(page) / totalSum);
    }

    public HashMap<String, Double> getPageRankScores() {
        return PageRankScores;
    }

    public void IndexPageRankScores() {
        List<org.bson.Document> documents= new ArrayList<>();
        for(Map.Entry<String, Double> entry : PageRankScores.entrySet())
        {
            documents.add(new org.bson.Document("DocURL",entry.getKey()).append("PageRankScore",entry.getValue()));
        }
        String URL = "mongodb+srv://fares_atef:fares12fares@cluster0.u3zf1oz.mongodb.net/?retryWrites=true&w=majority";
        MongoClientURI mongoClientURI = new MongoClientURI(URL);
        try(MongoClient mongoClient = new MongoClient(mongoClientURI))
        {
            MongoDatabase database = mongoClient.getDatabase("myFirstDatabase");
            MongoCollection<Document> collection = database.getCollection("PageRankScores");
            collection.drop();
            collection.insertMany(documents);
        }
    }

}

