package ranker;

import database.Mongo;
import org.bson.Document;

import java.net.URI;
import java.security.PrivateKey;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Ranker {
    private static final double W1= 0.5; //TF_IDF weight
    private static final double W2= 0.5; // PageRank weight
    private static final double W3= 1.5;// Exact match weight
    private static final double W4= 10;// Query word is in the url
    private static final double W5= 4;// Query word is in the title
    private static final double W6= 1;// Query word is in the body

    private static final double[] headingWeights = {2, 1.5, 1.25, 1.125, 1.0625, 1.03125};

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
                    String Position = posting.getString("position");
                    Pattern pattern = Pattern.compile("^https?://([^/]+)");
                    Matcher matcher = pattern.matcher(DocURL);
                    if(matcher.find()) {
                        String host = matcher.group(1);
                        if (host.toLowerCase().contains(actualWord))
                            TF_IDF *= W4;
                    }
                    switch (Position) {
                        case "title" -> TF_IDF *= W5;
                        case "body" -> TF_IDF *= W6;
                        case "h1" -> TF_IDF *= headingWeights[0];
                        case "h2" -> TF_IDF *= headingWeights[1];
                        case "h3" -> TF_IDF *= headingWeights[2];
                        case "h4" -> TF_IDF *= headingWeights[3];
                        case "h5" -> TF_IDF *= headingWeights[4];
                        case "h6" -> TF_IDF *= headingWeights[5];
                    }
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
