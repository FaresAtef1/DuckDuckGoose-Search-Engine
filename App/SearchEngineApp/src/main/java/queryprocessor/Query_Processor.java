package queryprocessor;

import database.Mongo;

import indexer.Indexer;
import org.bson.Document;
import ranker.Ranker;
import java.net.URI;
import java.util.*;

public class Query_Processor {
    public List<String> RetrieveResults(String userQuery) {
        List<String> Original_Text = Indexer.Query_Processing(userQuery);
        List<String> words = new ArrayList<>();
        for (String s : Original_Text) words.add(Indexer.Stem(s));
        if (words.isEmpty())
            return null;
        List<Document> queries = new ArrayList<>();
        for (String word : words)
            queries.add(new Document("stemmedWord", word));
        Document query = new Document("$or", queries); // Combine queries with logical OR
        Mongo dbMan = new Mongo();//////////////////////////////////
        List<Document> distinctValues = new ArrayList<>();
        distinctValues = dbMan.ExecuteQuery(query, "Indexer");
        if (distinctValues.isEmpty())
            return null;
        List<String> Results = Ranker.Rank(distinctValues, Original_Text);
        dbMan.closeConnection();
        return Results;
    }
}