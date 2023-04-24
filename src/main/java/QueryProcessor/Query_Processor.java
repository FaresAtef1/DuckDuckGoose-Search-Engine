package QueryProcessor;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import indexer.indexer;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Query_Processor {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        List<String> words = indexer.Query_Processing(input);
        List<Document> queries = new ArrayList<>();
        for (String word : words) queries.add(new Document("Word", word));
        Document query = new Document("$or", queries); // Combine queries with logical OR
        String url = "mongodb+srv://fares_atef:fares12fares@cluster0.u3zf1oz.mongodb.net/?retryWrites=true&w=majority";
        MongoClientURI mongoClientURI = new MongoClientURI(url);
        try(MongoClient mongoClient = new MongoClient(mongoClientURI))
        {
            MongoDatabase database = mongoClient.getDatabase("myFirstDatabase");
            MongoCollection<Document> collection = database.getCollection("Indexer");
            Document projection = new Document("postings.DocURL", 1).append("_id", 0);
            for (Document document : collection.find(query).projection(projection))
                System.out.println(document.toJson());
        }
    }
}
