package inverted_files;

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
    public static Map<Integer, List<pair<String, pair<Double, String>>>> postings = new HashMap<>();
    public static Map<String , Set<String>>  stem= new HashMap<>();

  //  private List<String> URLs;

//    public buildInvertedFiles(List<String> URLs)
//    {
//        this.URLs= URLs;
//    }
    public static void main(String[] args) throws IOException {
        List<String> URLs= new ArrayList<>();
        String s = "https://www.york.ac.uk/teaching/cws/wws/webpage1.html";
        URLs.add(s);
        URLs.add("https://www.geeksforgeeks.org/set-in-java/");
        URLs.add("https://facebook.com/");
        URLs.add("https://www.learnthat.org/pages/view/suffix.html");
        URLs.add("https://en.wikipedia.org/wiki/Word_stem");
        URLs.add("https://medium.com/@tusharsri/nlp-a-quick-guide-to-stemming-60f1ca5db49e");
//        HashMap<String,Set<String>> outlinks= new HashMap<>();
//        HashMap<String,Set<String>> inlinks= new HashMap<>();
//
//        for(String url: URLs)
//        {
//            outlinks.put(url,new HashSet<>());
//            inlinks.put(url,new HashSet<>());
//        }
//
//        for(String url: URLs)
//        {
//            Document doc = Jsoup.connect(url).get();
//            Elements links = doc.select("a[href]");
//            for(Element link: links)
//            {
//                String linkText = link.attr("abs:href");
//                outlinks.get(url).add(linkText);
//                if(!inlinks.containsKey(linkText))
//                    inlinks.put(linkText,new HashSet<>());
//                inlinks.get(linkText).add(url);
//            }
//        }
////        System.out.println(outlinks.size());
////        System.out.println(inlinks.size());
//        PageRanker pageRanker = new PageRanker(inlinks,outlinks);
//        pageRanker.CalculatePageRanks();
//        pageRanker.IndexPageRankScores();



//
////			docSet.add(Jsoup.connect("https://openjfx.io/openjfx-docs/#introduction").get());
////			docSet.add(Jsoup.connect("https://www.geeksforgeeks.org/using-underscore-in-numeric-literals-in-java/?ref=lbp").get());
////			docSet.add(Jsoup.connect("https://library.umbc.edu/").get());
////			docSet.add(Jsoup.connect("https://ur.umbc.edu/").get());
////			docSet.add(Jsoup.connect("https://gradschool.umbc.edu/discover/research/").get());
////			docSet.add(Jsoup.connect("https://research.umbc.edu/for-faculty/").get());
////			docSet.add(Jsoup.connect("https://bwtech.umbc.edu").get());
////			docSet.add(Jsoup.connect("https://umbc.edu/learn-more/get-going/?utm_source=UMBCedu&utm_medium=PreNav&utm_campaign=RFI-Nav#").get());
////			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/").get());
////			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/arts-and-culture/").get());
////			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/athletics-recreation/").get());
////			docSet.add(Jsoup.connect("https://events.umbc.edu/").get());
////			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/clubs-activities/").get());
////			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/health-wellbeing/").get());
////			docSet.add(Jsoup.connect("https://reslife.umbc.edu").get());
////			docSet.add(Jsoup.connect("https://umbc.edu/learn-more/get-going/?utm_source=UMBCedu&utm_medium=PreNav&utm_campaign=RFI-Nav#").get());
////			docSet.add(Jsoup.connect("https://securelb.imodules.com/s/1325/lg20/form.aspx?sid=1325&gid=1&pgid=2240&cid=4286&bledit=1&appealcode=OIA003").get());
////			docSet.add(Jsoup.connect("https://umbc.edu/giving").get());
////			docSet.add(Jsoup.connect("https://gritstarter.umbc.edu").get());
////			docSet.add(Jsoup.connect("https://www.alumni.umbc.edu/").get());
//
//
        for (String ss : URLs) {
            Document temp = Jsoup.connect(ss).get();
            List<pair<String, String>> tokens = Indexer.Normalize(temp); // String, position
            List<pair<Integer, String>> tokensIds = convertTokensToIds(tokens);
            Map<Integer, pair<Double, String>> WordsCounts = wordCounts(tokensIds);
            addToPostings(ss, WordsCounts);
        }

        List<org.bson.Document> Documents = new ArrayList<>(); // DataBase
      //  int i = 0;
//        for (Map.Entry<String, Integer> word : lexicon.entrySet())
//        {
//            System.out.print(i + "." + word.getKey() + " : ");
//            List<pair<String, pair<Integer, String>>> postingsList = postings.get(word.getValue());
//            org.bson.Document query = new org.bson.Document("Word", word.getKey());
//            List<org.bson.Document> postingsListOfEachWord = new ArrayList<>();
//            for (pair<String, pair<Integer, String>> p : postingsList)
//            {
//                org.bson.Document temp = new org.bson.Document("DocURL", p.first).append("tf", p.second.first).append("position", p.second.second);
//                System.out.print("{DocURL : " + p.first + " , tf : " + p.second.first + " , position : " + p.second.second + "}");
//                postingsListOfEachWord.add(temp);
//            }
//            query.append("postings", postingsListOfEachWord);
//            Documents.add(query);
//            System.out.println();
//            i++;
//        }

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

    public static Map<Integer, pair<Double, String>> wordCounts(List<pair<Integer, String>> tokenIds) {
        Map<Integer, pair<Double, String>> wordCounts = new HashMap<>(); // wordID, <tf, position>
        for (pair<Integer, String> id : tokenIds)
        {
            pair<Double, String> wordData= wordCounts.get(id.first);
            if (wordData!=null)
                wordData.first = wordData.first + 1;
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

    public static void addToPostings(String URL, Map<Integer, pair<Double, String>> wordsCounts) {
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