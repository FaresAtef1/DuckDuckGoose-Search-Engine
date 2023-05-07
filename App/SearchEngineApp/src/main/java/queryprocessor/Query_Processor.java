package queryprocessor;

import database.Mongo;

import indexer.Indexer;
import org.bson.Document;
import ranker.Ranker;
import java.net.URI;
import java.util.*;

public class Query_Processor {
    public List<String> RetrieveResults(String userQuery) {
        String input = userQuery;
        List<String> words = Indexer.Query_Processing(input);
        for (int i = 0; i < words.size(); i++)
            words.set(i, Indexer.Stem(words.get(i)));
        if (words.isEmpty())
            return null;
        List<Document> queries = new ArrayList<>();
        for (String word : words)
            queries.add(new Document("stemmedWord", word));
        Document query = new Document("$or", queries); // Combine queries with logical OR
        Mongo dbMan = new Mongo();
        List<Document> distinctValues = new ArrayList<>();
        distinctValues = dbMan.ExecuteQuery(query, "Indexer");
        if (distinctValues.isEmpty())
            System.out.println("No results found");
        List<String> Results = Ranker.Rank(distinctValues, words);
        dbMan.closeConnection();
        return Results;
    }
}