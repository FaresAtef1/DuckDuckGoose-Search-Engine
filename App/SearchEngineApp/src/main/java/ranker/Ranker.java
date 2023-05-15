package ranker;

import database.Mongo;
import org.bson.Document;

import java.net.URI;
import java.net.URL;
import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ranker {
    private static final double W1= 0.9; //TF_IDF weight
    private static final double W2= 0.1; // PageRank weight
    private static final double W3= 65;// Exact match weight
    private static final double W4= 100;// Query word is in the url
    private static final double W5= 80;// Query word is in the title
    private static final double W6= 1;// Query word is in the body

    private static final double W7= 1000000; //The entire query is in the URL and Nothing else

    private static final double[] headingWeights = {2, 1.5, 1.25, 1.125, 1.0625, 1.03125};



    public static List<String> Rank(List<Document> documents, List<String> originalQuery, ConcurrentHashMap<String,Set<Integer>> URLTagsIndices) {
        Map<String , Double> result=new HashMap<>();
        Mongo dbManager = new Mongo();
        List<Document> pageRankScores=new ArrayList<>();
        List<Document> queries=new LinkedList<>();
//        URLTagsIndices=new HashMap<>();
        for (Document doc : documents)
        {
            List<Document> actualWords=doc.getList("postings",Document.class);
            for(Document actualWordDoc : actualWords) {
                String actualWord = actualWordDoc.getString("actualWord");
                double IDF = actualWordDoc.getDouble("IDF");
                List<Document> postings = actualWordDoc.getList("postings", Document.class);
                for (Document posting : postings)
                {
                    String DocURL = posting.getString("DocURL");

                    double TF = posting.getDouble("tf");
                    //Scam filter
                    if(TF>0.1)
                    {
                        TF=0.000000001;
                    }
                    if( DocURL.equals("https://www.bbc.co.uk/notifications") || DocURL.equals("https://account.bbc.com/account"))
                        System.out.println("DocURL: "+DocURL+" TF: "+TF+ " IDF: "+IDF);
                    double TF_IDF = (originalQuery.contains(actualWord) ? W3 : W1) * TF * IDF;
                    String Position = posting.getString("position");
                    Pattern pattern = Pattern.compile("^https?://([^/]+)");
                    Matcher matcher = pattern.matcher(DocURL);
                    try {
                        URL url = new URL(DocURL);
                        String path = url.getPath();
                        String host = url.getHost();
                        String[] parts = host.split("\\.");
                        String domain = parts[parts.length - 2];
                            if (host.toLowerCase().contains(actualWord)) {
                                if((path.equals("/")||path.isEmpty())&&domain.toLowerCase().equals(actualWord)&&originalQuery.size()==1)
                                {

                                        System.out.println("URL: "+DocURL);
                                    TF_IDF *= W7;
                                    System.out.println("TF_IDF: "+TF_IDF);
                                    if(DocURL.equals("https://www.bbc.co.uk/news/world/europe/guernsey") || DocURL.equals("https://www.bbc.com/news/world/europe/guernsey") || DocURL.equals("https://www.bbc.co.uk/notifications") || DocURL.equals("https://account.bbc.com/account"))
                                        System.out.println("DocURL: "+DocURL+" TF_IDF: "+TF_IDF);


                                }
                                else
                                {
                                    TF_IDF *= W4;
                                }
                            }
                        }
                    catch (Exception e)
                    {
                        continue;
                    }
                    if(DocURL.equals("https://www.bbc.co.uk/notifications") || DocURL.equals("https://account.bbc.com/account"))
                        System.out.println("DocURL: "+DocURL+" TF_IDF: "+TF_IDF);
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
                    Double oldTF_IDF = result.get(DocURL);
                    queries.add(new Document("DocURL",DocURL));
                    if (oldTF_IDF != null) {
                        result.put(DocURL, oldTF_IDF + TF_IDF);
                    } else
                        result.put(DocURL, TF_IDF);
                    List<Document> tagslist = posting.getList("TagsList", Document.class);
                    for (Document tag : tagslist) {
                        int tagindex = (int) tag.get("TagIndex");
                        Set<Integer> indices = URLTagsIndices.get(DocURL);
                        if(indices==null)
                        {
                            indices=new HashSet<>();
                            indices.add(tagindex);
                            URLTagsIndices.put(DocURL,indices);
                        }
                        else
                            indices.add(tagindex);
                    }
//                    if(DocURL.equals("https://www.bbc.co.uk/news/world/europe/guernsey") || DocURL.equals("https://www.bbc.com/news/world/europe/guernsey") || DocURL.equals("https://www.bbc.co.uk/notifications") || DocURL.equals("https://account.bbc.com/account"))
//                        System.out.println("DocURL: "+DocURL+" TF_IDF: "+TF_IDF);
                }
            }

        }
        Document query=new Document("$or",queries);
        pageRankScores=dbManager.ExecuteQuery(query,"PageRankScores");
        for(Document pageRankScore : pageRankScores)
        {
            String DocURL=pageRankScore.getString("DocURL");
            if(!result.containsKey(DocURL))
                continue;
            double pageRank=pageRankScore.getDouble("PageRankScore");
            result.replace(DocURL,result.get(DocURL)+W2*pageRank);
        }
        List<String> FinalResult = result.entrySet()
                .stream()
                .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
        return FinalResult;
    }
}