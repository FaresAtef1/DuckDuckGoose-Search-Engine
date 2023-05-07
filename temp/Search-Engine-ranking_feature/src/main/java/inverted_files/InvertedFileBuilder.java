package inverted_files;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import database.Mongo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;

import indexer.*;
import structures.pair;

public class InvertedFileBuilder {
    private final  Map<String, Integer> lexicon = new HashMap<>();
    private final Map<Integer, List<pair<String, pair<Double, String>>>> postings = new HashMap<>();
    private final  Map<String , Set<String>>  stem= new HashMap<>();

    private final  Map<String,Integer> positingRanks = new HashMap<>();

    private final Set<String> URLs;



    public InvertedFileBuilder(Set<String> URLs)
    {
        this.URLs= URLs;
        positingRanks.put("title", 0);
        positingRanks.put("h1", 1);
        positingRanks.put("h2", 2);
        positingRanks.put("h3", 3);
        positingRanks.put("h4", 4);
        positingRanks.put("h5", 5);
        positingRanks.put("h6", 6);
        positingRanks.put("body", 7);
        positingRanks.put("label", 0);

    }

    private void Invert()
    {
        for (String ss : URLs) {
            Document temp = null;
            try {
                temp = Jsoup.connect(ss).get();
            }
            catch (IOException e) {
                //e.printStackTrace();
                continue;
            }
            if(temp==null)
                continue;
            List<pair<String, String>> tokens = Indexer.Normalize(temp); // String, position
            List<pair<Integer, String>> tokensIds = convertTokensToIds(tokens);
            Map<Integer, pair<Double, String>> WordsCounts = wordCounts(tokensIds);
            addToPostings(ss, WordsCounts);
        }
    }
    private void Index()
    {
        List<org.bson.Document> Documents = new ArrayList<>(); // DataBase

        for (Map.Entry<String, Set<String>> stemmedWord : stem.entrySet()) //for each stemmed word
        {
            org.bson.Document query = new org.bson.Document("stemmedWord", stemmedWord.getKey());
            List<org.bson.Document> postingsListOfEachWord = new ArrayList<>();
            for(String word : stemmedWord.getValue()) //for each actual word
            {
                int id = lexicon.get(word);
                List<pair<String, pair<Double, String>>> postingsList = postings.get(id); // the posting of each actual word
                List<org.bson.Document> postingsListOfEachActualWord = new ArrayList<>();
                for(pair<String, pair<Double, String>> p : postingsList)//for each posting
                {
                    org.bson.Document temp = new org.bson.Document("DocURL", p.first).append("tf", p.second.first).append("position", p.second.second); // the posting of each actual word
                    postingsListOfEachActualWord.add(temp);
                }
                double IDF = (double)URLs.size()/postingsList.size();
                IDF=Math.log10(IDF);
                org.bson.Document temp = new org.bson.Document("actualWord", word).append("IDF",IDF).append("postings", postingsListOfEachActualWord);
                postingsListOfEachWord.add(temp);

            }
            query.append("postings", postingsListOfEachWord);
            Documents.add(query);
        }
        // DataBase
        Mongo mongo = new Mongo();
        mongo.updateCollection("Indexer",Documents);
    }

    public void Build()
    {
       this.Invert();
       this.Index();
    }

    private   List<pair<Integer, String>> convertTokensToIds(List<pair<String, String>> tokens) {
        List<pair<Integer, String>> tokensIDs = new ArrayList<>();
        for (pair<String, String> token : tokens)
        {
            Integer id= lexicon.get(token.first);
            if (id!=null)
            {
                pair<Integer, String> Pair = new pair<>(id, token.second);
                tokensIDs.add(Pair);
            }
            else
            {
                id= lexicon.size();
                lexicon.put(token.first, id);
                pair<Integer, String> Pair = new pair<>(id, token.second);
                tokensIDs.add(Pair);
            }
            String stemWord= Indexer.Stem(token.first);
            Set words=stem.get(stemWord);
            if(words==null)
            {
                words=new HashSet();
                words.add(token.first);
                stem.put(stemWord, words);
            }
            else
                words.add(token.first);

        }
        return tokensIDs;
    }

    private   Map<Integer, pair<Double, String>> wordCounts(List<pair<Integer, String>> tokenIds) {
        Map<Integer, pair<Double, String>> wordCounts = new HashMap<>(); // wordID, <tf, position>


        for (pair<Integer, String> id : tokenIds)
        {
            pair<Double, String> wordData= wordCounts.get(id.first);
            if (wordData!=null) {
                wordData.first = wordData.first + 1;
                if(positingRanks.get(wordData.second)>positingRanks.get(id.second))
                {
                    wordData.second=id.second;
                }
            }
            else
                wordData = new pair<>(1.0, id.second);
            wordCounts.put(id.first, wordData);
        }
        for(Map.Entry<Integer, pair<Double, String>> word : wordCounts.entrySet())
        {
            word.getValue().first = word.getValue().first / tokenIds.size();
        }
        return wordCounts;
    }

    private void addToPostings(String URL, Map<Integer, pair<Double, String>> wordsCounts) {
        for (Map.Entry<Integer, pair<Double, String>> record : wordsCounts.entrySet())
        {
            List<pair<String, pair<Double, String>>> postingList = postings.get(record.getKey());
            pair<String, pair<Double, String>> New_Posting = new pair<>(URL, new pair<>(record.getValue().first, record.getValue().second));
            if (postingList!=null)
                postingList.add(New_Posting);
            else
            {
                postingList = new ArrayList<>();
                postingList.add(New_Posting);
                postings.put(record.getKey(), postingList);
            }
        }
    }
}