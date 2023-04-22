package InvertedFiles;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


public class mongotest {
    public static void main(String[] args) {
        String url = "mongodb+srv://fares_atef:fares12fares@cluster0.u3zf1oz.mongodb.net/?retryWrites=true&w=majority";
        MongoClientURI mongoClientURI = new MongoClientURI(url);
        try(MongoClient mongoClient = new MongoClient(mongoClientURI))
        {
            MongoDatabase database = mongoClient.getDatabase("myFirstDatabase");
            MongoCollection<Document> collection = database.getCollection("testfn");
            List<String> hobbies = new ArrayList<>();
            hobbies.add("reading");
            hobbies.add("travelling");
            hobbies.add("sports");
            Document query = new Document("title", "fares").append("name", "atef").append("age", 22).append("hobbies", hobbies);
            collection.insertOne(query);
        }
    }
}