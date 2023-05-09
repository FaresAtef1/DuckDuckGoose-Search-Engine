package inverted_files;

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
    private final  Map<String,Integer> postingRanks = new HashMap<>();
    private final Set<String> URLs;

    public InvertedFileBuilder(Set<String> URLs)
    {
        this.URLs= URLs;
        postingRanks.put("title", 0);
        postingRanks.put("label", 0);
        postingRanks.put("h1", 1);
        postingRanks.put("h2", 2);
        postingRanks.put("p", 2);
        postingRanks.put("span", 2);
        postingRanks.put("div", 2);
        postingRanks.put("li", 2);
        postingRanks.put("a", 2);
        postingRanks.put("h3", 3);
        postingRanks.put("h4", 4);
        postingRanks.put("h5", 5);
        postingRanks.put("h6", 6);
        postingRanks.put("body", 7);
        //////////////////////////////// remaining tags
    }

    private void Invert()
    {
        for (String ss : URLs) {
            Document temp = null;
            try {
                temp = Jsoup.connect(ss).get();
            }
            catch (IOException e) {continue;}
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
            Set<String> words=stem.get(stemWord);
            if(words==null)
            {
                words=new HashSet<>();
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
            if (wordData!=null)
            {
                wordData.first = wordData.first + 1;
                if(postingRanks.containsKey(id.second))
                    if(postingRanks.get(wordData.second)> postingRanks.get(id.second))
                        wordData.second=id.second;
            }
            else /// remove after assigning all tags scores
            {
                wordData = new pair<>(1.0, id.second);
                if(!postingRanks.containsKey(id.second))
                    postingRanks.put(id.second, 3);
            }
            wordCounts.put(id.first, wordData);
        }
        for(Map.Entry<Integer, pair<Double, String>> word : wordCounts.entrySet())
            word.getValue().first = word.getValue().first / tokenIds.size();
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

//    public void InvertOne(Document doc,String URL)
//    {
////        List<pair<pair<String,Integer>, String>> tokens = Indexer.Normalize(doc); //word, index of the tag and tag name
////        List<pair<Integer, String>> tokensIds = convertTokensToIds(tokens);
//        Map<Integer, pair<Double, String>> WordsCounts = wordCounts(tokensIds);
//        addToPostings(URL, WordsCounts);
//    }

    public static void main (String[] args)
    {
        Set<String>URLs = new HashSet<>();
        URLs.add("https://www.bbc.com/");
        InvertedFileBuilder inv= new InvertedFileBuilder(URLs);
        inv.Build();
    }
}