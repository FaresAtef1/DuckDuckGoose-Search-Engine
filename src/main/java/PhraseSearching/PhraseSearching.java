package PhraseSearching;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import indexer.Indexer;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;


public class PhraseSearching {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String[] targetPhrase = input.substring(1, input.length() - 1).split("\\s+");
        List<String> words = Indexer.Query_Processing(input);
        if(words.isEmpty())
            return;
        List<Document> queries = new ArrayList<>();
        for (String word : words)
            queries.add(new Document("Word", word));
        Document query = new Document("$or", queries); // Combine queries with logical OR
        String url = "mongodb+srv://fares_atef:fares12fares@cluster0.u3zf1oz.mongodb.net/?retryWrites=true&w=majority";
        MongoClientURI mongoClientURI = new MongoClientURI(url);
        HashMap<String,Element> out = new HashMap<>();
        try(MongoClient mongoClient = new MongoClient(mongoClientURI))
        {
            MongoDatabase database = mongoClient.getDatabase("myFirstDatabase");
            MongoCollection<Document> collection = database.getCollection("Indexer");
            List<String> distinctValues = new ArrayList<>();
            collection.distinct("postings.DocURL",query, String.class).into(distinctValues);
            for (String value : distinctValues)
            {
                try {
                    org.jsoup.nodes.Document doc = Jsoup.connect(value).get();
                    Elements elements = doc.select("*");
                    for (Element element : elements)
                    {
                        String linkText = element.text();
                        String[] words2 = linkText.split("\\s+");
                        int current = 0;
                        for (String word : words2)
                        {
                            if(word.equalsIgnoreCase(targetPhrase[current])) //assuming ignoring the case
                            {
                                current++;
                                if(current == targetPhrase.length)
                                {
                                    Element smallestTag = out.get(value);
                                    if (smallestTag == null || element.parents().size() > smallestTag.parents().size())
                                    {
                                        smallestTag = element;
                                        out.put(value,smallestTag);
                                    }
                                    break;
                                }
                            }
                            else
                                current = 0;
                        }
                    }
                }
                catch(Exception e){
                    System.out.println("Exception");
                }
            }
        }
        for (Map.Entry <String,Element> site : out.entrySet())
        {
            System.out.println(site.getKey());
            System.out.println(site.getValue().text());
            if(site.getValue().nextElementSibling() != null)
                System.out.println(site.getValue().nextElementSibling().text());
        }
        scanner.close();
    }
}