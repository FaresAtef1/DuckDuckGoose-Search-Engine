package inverted_files;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


public class mongotest {
    public static void main(String[] args) {
        try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {
            // Access the desired database
            MongoDatabase database = mongoClient.getDatabase("test");

            // Perform operations on the database
            database.createCollection("users");
            System.out.println("Collection 'users' created successfully.");
        }
    }
}