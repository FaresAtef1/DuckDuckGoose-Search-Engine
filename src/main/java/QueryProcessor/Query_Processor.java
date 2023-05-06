package QueryProcessor;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import indexer.indexer;
import org.bson.Document;
import structures.pair;

import java.util.*;

public class Query_Processor {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        List<String> words = indexer.Query_Processing(input);
        if(words.isEmpty())
            return;
        List<Document> queries = new ArrayList<>();
        for (String word : words)
            queries.add(new Document("stemmedWord", word));
        Document query = new Document("$or", queries); // Combine queries with logical OR
        String url = "mongodb+srv://fares_atef:fares12fares@cluster0.u3zf1oz.mongodb.net/?retryWrites=true&w=majority";
        MongoClientURI mongoClientURI = new MongoClientURI(url);
        //List<pair<String, pair<String, Double>>> result=new ArrayList<>();
        Map<String , Double> result=new HashMap<>();
        try(MongoClient mongoClient = new MongoClient(mongoClientURI))
        {
            MongoDatabase database = mongoClient.getDatabase("myFirstDatabase");
            MongoCollection<Document> collection = database.getCollection("Indexer");
            List<Document> distinctValues = new ArrayList<>();
            collection.find(query).into(distinctValues);
            for (Document doc : distinctValues)
            {
             List<Document> actualWords=doc.getList("postings",Document.class);
             for(Document actualWordDoc : actualWords)
             {
              String actualWord=actualWordDoc.getString("actualWord");
              double IDF=actualWordDoc.getDouble("IDF");
              List<Document> postings=actualWordDoc.getList("postings",Document.class);
              for(Document posting :postings)
              {
                  String DocURL=posting.getString("DocURL");
                  double TF=posting.getDouble("tf");
                  double TF_IDF=TF*IDF;
//                  System.out.println("actualWord  "+actualWord+"  DocURL: "+DocURL+" TF_IDF: "+TF_IDF);
                  Double oldTF_IDF=result.get(DocURL);
                  if(oldTF_IDF!=null)
                  {
                      result.put(DocURL,oldTF_IDF+TF_IDF);
                  }
                  else
                      result.put(DocURL,TF_IDF);
              }

             }
            }
            for(Map.Entry<String,Double> entry : result.entrySet())
            {
                System.out.println("DocURL: "+entry.getKey()+" TF_IDF: "+entry.getValue());
            }
        }
        scanner.close();
    }
}