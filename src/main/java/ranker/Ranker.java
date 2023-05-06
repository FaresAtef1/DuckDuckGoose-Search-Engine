package ranker;

import DataBase.Mongo;
import org.bson.Document;

import java.util.*;


public class Ranker {
    private static final double W1= 0.5; //TF_IDF weight
    private static final double W2= 0.5; // PageRank weight

    private static final double W3= 1.5;// Exact match weight

    public static List<String> Rank(List<Document> documents, List<String> originalQuery) {
        Map<String , Double> result=new HashMap<>();
        Mongo dbManager = new Mongo();
        List<Document> pageRankScores=new ArrayList<>();
        List<Document> queries=new LinkedList<>();
        for (Document doc : documents)
        {
            List<Document> actualWords=doc.getList("postings",Document.class);
            for(Document actualWordDoc : actualWords) {
                String actualWord = actualWordDoc.getString("actualWord");
                double IDF = actualWordDoc.getDouble("IDF");
                List<Document> postings = actualWordDoc.getList("postings", Document.class);
                for (Document posting : postings) {
                    String DocURL = posting.getString("DocURL");
                    double TF = posting.getDouble("tf");
                    double TF_IDF = (originalQuery.contains(actualWord) ? W3 : W1) * TF * IDF;
                    System.out.println("actualWord  " + actualWord + "  DocURL: " + DocURL + " TF_IDF: " + TF_IDF);
                    Double oldTF_IDF = result.get(DocURL);
                    queries.add(new Document("DocURL",DocURL));
                    if (oldTF_IDF != null) {
                        result.put(DocURL, oldTF_IDF + TF_IDF);
                    } else
                        result.put(DocURL, TF_IDF);
                }
            }
        }
        for(Map.Entry<String,Double> entry : result.entrySet())
        {
            System.out.println("DocURL: "+entry.getKey()+" TF_IDF: "+entry.getValue());
        }
        Document query=new Document("$or",queries);
        pageRankScores=dbManager.ExecuteQuery(query,"PageRankScores");
        for(Document pageRankScore : pageRankScores)
        {
            String DocURL=pageRankScore.getString("DocURL");
            double pageRank=pageRankScore.getDouble("PageRank");
            result.replace(DocURL,result.get(DocURL)+W2*pageRank);
        }
        List<String> FinalResult = result.entrySet()
                .stream()
                .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        //  result=Sort.sortByValue(result);
        for(Map.Entry<String,Double> entry : result.entrySet())
        {
            System.out.println("DocURL: "+entry.getKey()+" TF_IDF_PageRank: "+entry.getValue());
        }

        for(String DocURL : FinalResult)
        {
            System.out.println("DocURL: "+DocURL);
        }


        return null;
    }

}
