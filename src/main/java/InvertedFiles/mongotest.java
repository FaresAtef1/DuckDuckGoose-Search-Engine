package InvertedFiles;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class mongotest {
    public static void main(String[] args) {
        String uri = "mongodb+srv://fares_atef:fares12fares@cluster0.u3zf1oz.mongodb.net/?retryWrites=true&w=majority";
        MongoClientURI mongoClientURI = new MongoClientURI(uri);
        try(MongoClient mongoClient = new MongoClient(mongoClientURI))
        {
            MongoDatabase database = mongoClient.getDatabase("myFirstDatabase");
            MongoCollection<Document> collection = database.getCollection("table1");
            //Document query = new Document("_id", new ObjectId("5e234fe121fcf183e83ddce2"));
            Document query = new Document("title", "fares");
            Document result = collection.find(query).first();
            System.out.println(result);
        }
    }
}