package InvertedFiles;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import indexer.*;
import structures.pair;

class buildInvertedFiles {
    public static Map<String, Integer> lexicon = new HashMap<>();
    public static Map<Integer, List<pair<String, pair<Integer, String>>>> postings = new HashMap<>();

    public static void main(String[] args) throws IOException {
        List<String> URLs = new ArrayList<>();
        String s = "https://www.york.ac.uk/teaching/cws/wws/webpage1.html";
        URLs.add(s);
        URLs.add("https://www.geeksforgeeks.org/set-in-java/");
//			docSet.add(Jsoup.connect("https://openjfx.io/openjfx-docs/#introduction").get());
//			docSet.add(Jsoup.connect("https://www.geeksforgeeks.org/using-underscore-in-numeric-literals-in-java/?ref=lbp").get());
//			docSet.add(Jsoup.connect("https://library.umbc.edu/").get());
//			docSet.add(Jsoup.connect("https://ur.umbc.edu/").get());
//			docSet.add(Jsoup.connect("https://gradschool.umbc.edu/discover/research/").get());
//			docSet.add(Jsoup.connect("https://research.umbc.edu/for-faculty/").get());
//			docSet.add(Jsoup.connect("https://bwtech.umbc.edu").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/learn-more/get-going/?utm_source=UMBCedu&utm_medium=PreNav&utm_campaign=RFI-Nav#").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/arts-and-culture/").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/athletics-recreation/").get());
//			docSet.add(Jsoup.connect("https://events.umbc.edu/").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/clubs-activities/").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/health-wellbeing/").get());
//			docSet.add(Jsoup.connect("https://reslife.umbc.edu").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/learn-more/get-going/?utm_source=UMBCedu&utm_medium=PreNav&utm_campaign=RFI-Nav#").get());
//			docSet.add(Jsoup.connect("https://securelb.imodules.com/s/1325/lg20/form.aspx?sid=1325&gid=1&pgid=2240&cid=4286&bledit=1&appealcode=OIA003").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/giving").get());
//			docSet.add(Jsoup.connect("https://gritstarter.umbc.edu").get());
//			docSet.add(Jsoup.connect("https://www.alumni.umbc.edu/").get());

        for (String ss : URLs)
        {
            Document temp = Jsoup.connect(ss).get();
            List<pair<String, String>> tokens = indexer.Normalize(temp); // String, position
            List<pair<Integer, String>> tokensIds = convertTokensToIds(tokens);
            Map<Integer, pair<Integer, String>> WordsCounts = wordCounts(tokensIds);
            addToPostings(ss, WordsCounts);
        }

        List<org.bson.Document> Documents = new ArrayList<>(); // DataBase
        int i = 0;
        for (Map.Entry<String, Integer> word : lexicon.entrySet())
        {
            System.out.print(i + "." + word.getKey() + " : ");
            List<pair<String, pair<Integer, String>>> postingsList = postings.get(word.getValue());
            //List<String>
            for (pair<String, pair<Integer, String>> p : postingsList)
            {
                System.out.print("{DocURL : " + p.first + " , tf : " + p.second.first + " , position : " + p.second.second + "}");
                org.bson.Document query = new org.bson.Document("Word", word.getKey()).append("DocURL", p.first).append("tf", p.second.first).append("position", p.second.second);
                Documents.add(query);
            }
            System.out.println();
            i++;
        }
        // DataBase
        String URL = "mongodb+srv://fares_atef:fares12fares@cluster0.u3zf1oz.mongodb.net/?retryWrites=true&w=majority";
        MongoClientURI mongoClientURI = new MongoClientURI(URL);
        try(MongoClient mongoClient = new MongoClient(mongoClientURI))
        {
            MongoDatabase database = mongoClient.getDatabase("myFirstDatabase");
            MongoCollection<org.bson.Document> collection = database.getCollection("Indexer");
            collection.drop();
            collection.insertMany(Documents);
        }
    }

    public static List<pair<Integer, String>> convertTokensToIds(List<pair<String, String>> tokens) {
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
        }
        return tokensIDs;
    }

    public static Map<Integer, pair<Integer, String>> wordCounts(List<pair<Integer, String>> tokenIds) {
        Map<Integer, pair<Integer, String>> wordCounts = new HashMap<>(); // wordID, <tf, position>
        for (pair<Integer, String> id : tokenIds)
        {
            pair<Integer, String> wordData= wordCounts.get(id.first);
            if (wordData!=null)
                wordData.first = wordData.first + 1;
            else
                wordData = new pair<>(1, id.second);
            wordCounts.put(id.first, wordData);
        }
        return wordCounts;
    }

    public static void addToPostings(String URL, Map<Integer, pair<Integer, String>> wordsCounts) {
        for (Map.Entry<Integer, pair<Integer, String>> record : wordsCounts.entrySet())
        {
            List<pair<String, pair<Integer, String>>> postingList = postings.get(record.getKey());
            pair<String, pair<Integer, String>> New_Posting = new pair<>(URL, new pair<>(record.getValue().first, record.getValue().second));
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