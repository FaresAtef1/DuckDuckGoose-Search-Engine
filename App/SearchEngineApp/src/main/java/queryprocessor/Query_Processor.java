package queryprocessor;

import database.Mongo;

import indexer.Indexer;
import org.bson.Document;
import ranker.Ranker;
import voice.VoiceRecognizer;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Query_Processor {

    public List<String> RetrieveResults(String userQuery, ConcurrentHashMap<String,Set<Integer>> URLTagIndices) {
        Indexer indexer=new Indexer();


        List<String> Original_Text = indexer.Query_Processing(userQuery);
        List<String> words = new ArrayList<>();
        for (String s : Original_Text) words.add(indexer.Stem(s));
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
//        URLTagIndices=new HashMap<>();
        List<String> Results = Ranker.Rank(distinctValues, Original_Text,URLTagIndices);
        dbMan.closeConnection();
        return Results;
    }

    public static void main(String[] args) throws IOException {
//        Scanner input = new Scanner(System.in);
//        Query_Processor qp = new Query_Processor();
//        String query = input.nextLine();
//        List<String> results = qp.RetrieveResults(query);
//        List<String> titles = new ArrayList();
//        List<String> paragraphs =WebpageParagraphScraper.Scraper(results,query,titles);

    }
}