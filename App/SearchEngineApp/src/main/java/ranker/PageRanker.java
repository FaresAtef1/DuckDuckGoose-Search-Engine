package ranker;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import database.Mongo;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PageRanker {

    private HashMap<String, Double> PageRankScores;
    private ConcurrentHashMap<String, Set<String>> inLinks;
    private ConcurrentHashMap<String, Set<String>> outLinks;
    private static final double dampingFactor = 0.85;
    private static final double threshold = 0.0001;



    public PageRanker(ConcurrentHashMap<String, Set<String>> outwardLinks) {
        this.outLinks = outwardLinks;
        this.inLinks= this.getInLinks();
        PageRankScores = new HashMap<>();
        for (String key : inLinks.keySet())
            PageRankScores.put(key, 1.0 / inLinks.size());
    }
    private  ConcurrentHashMap<String,Set<String>> getInLinks() {
        ConcurrentHashMap<String, Set<String>> inwardLinks;
        inwardLinks = new ConcurrentHashMap<>();

        for (Map.Entry<String, Set<String>> entry : outLinks.entrySet()) {
            if(!inwardLinks.containsKey(entry.getKey()))
                inwardLinks.put(entry.getKey(), new HashSet<>());
              for (String link : entry.getValue()) {
                if (outLinks.containsKey(link)) {
                    if(!inwardLinks.containsKey(link))
                        inwardLinks.put(link, new HashSet<>());
                    inwardLinks.get(link).add(entry.getKey());
                }
            }
        }
        return inwardLinks;
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
            documents.add(new org.bson.Document("DocURL",entry.getKey()).append("PageRankScore",entry.getValue()));
        Mongo mongo = new Mongo();
        mongo.updateCollection("PageRankScores",documents);
    }
}