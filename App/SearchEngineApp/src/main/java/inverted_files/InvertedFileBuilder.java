package inverted_files;

import database.Mongo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import indexer.*;
import structures.pair;

public class InvertedFileBuilder implements Runnable{
    //    private Map<String, Integer> lexicon = new HashMap<>();
    private static ConcurrentHashMap<String, List<pair<String, pair<Double, String>>>> postings = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<pair<String,String>,Set<Integer>> TagIndices = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String , Set<String>>  stem= new ConcurrentHashMap<>();
    private static Map<String,Integer> postingRanks = new HashMap<>();
    private static ReentrantLock stemLock = new ReentrantLock();
    private static ReentrantLock postingsLock = new ReentrantLock();
    private static final int threadsNumber=8;
    //private Set<String> URLs;

    private  final int numberOfURLs;
    private static ConcurrentLinkedQueue<String> URLs = new ConcurrentLinkedQueue<>();

    public void run()
    {
        List<org.bson.Document> titlesDocs=new ArrayList<>();
        Mongo mongo=new Mongo();
        Indexer indexer=new Indexer();
        while(URLs.size()!=0) {
            String url = URLs.poll();
            if (url != null) {
                Document doc = null;
                String title;
                try {
                    doc = Jsoup.connect(url).get();
                    title = doc.title();
                } catch (IOException e) {
                    continue;
                }
                if (title == "")
                    title = url;
                titlesDocs.add(new org.bson.Document("URL", url).append("Title", title));
                List<pair<pair<String, Integer>, String>> tokens = indexer.Normalize(doc, url); //word, index of the tag and tag name
                Map<String, pair<Double, String>> WordsCounts = wordCounts(tokens, url, indexer);
                addToPostings(url, WordsCounts);
            }
        }
        mongo.AddToCollection("Titles",titlesDocs);
    }
    public InvertedFileBuilder(Set<String> URLs)
    {
        for(String s:URLs)
            this.URLs.add(s);
        this.numberOfURLs=URLs.size();
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

    public void Invert()
    {
        List<org.bson.Document> queries=new ArrayList<>();
        Indexer indexer=new Indexer();
        for (String ss : URLs) {
            Document temp = null;
            try {
                temp = Jsoup.connect(ss).get();
            }
            catch (IOException e) {continue;}
            if(temp==null)
                continue;
            String title=temp.title();
            if(title.equals(""))
                title=ss;
            queries.add(new org.bson.Document("URL",ss).append("Title",title));

            List<pair<pair<String,Integer>, String>> tokens = indexer.Normalize(temp,ss); //word, index of the tag and tag name
            //List<pair<Integer, String>> tokensIds = convertTokensToIds(tokens,ss);
            Map<String, pair<Double, String>> WordsCounts = wordCounts(tokens,ss,indexer);
            addToPostings(ss, WordsCounts);
        }
        Mongo mon=new Mongo();
        mon.updateCollection("Titles",queries);
    }

    public void Index()
    {
        int timestart = (int) System.currentTimeMillis();
        List<org.bson.Document> Documents = new ArrayList<>(); // DataBase
        Mongo mongo = new Mongo();
        for (Map.Entry<String, Set<String>> stemmedWord : stem.entrySet()) //for each stemmed word
        {
            org.bson.Document query = new org.bson.Document("stemmedWord", stemmedWord.getKey());
            List<org.bson.Document> postingsListOfEachWord = new ArrayList<>();
            for(String word : stemmedWord.getValue()) //for each actual word
            {

                List<pair<String, pair<Double, String>>> postingsList = postings.get(word); // the posting of each actual word
                List<org.bson.Document> postingsListOfEachActualWord = new ArrayList<>();
                for(pair<String, pair<Double, String>> p : postingsList)//for each posting
                {
                    org.bson.Document temp = new org.bson.Document("DocURL", p.first).append("tf", p.second.first).append("position", p.second.second); // the posting of each actual word
                    Set<Integer> tags = TagIndices.get(new pair<>(p.first,word));
                    List <org.bson.Document> tagsList = new ArrayList<>();
                    if(tags!=null)
                    {
                        for (Integer tag : tags)
                        {
                            org.bson.Document temp2 = new org.bson.Document("TagIndex", tag);
                            tagsList.add(temp2);
                        }
                        temp.append("TagsList", tagsList);
                    }
                    postingsListOfEachActualWord.add(temp);
                }
                double IDF = (double)numberOfURLs/postingsList.size();
                IDF=Math.log10(IDF);
                org.bson.Document temp = new org.bson.Document("actualWord", word).append("IDF",IDF).append("postings", postingsListOfEachActualWord);
                postingsListOfEachWord.add(temp);

            }
            query.append("postings", postingsListOfEachWord);
            Documents.add(query);
//            mongo.AddOneDoc("Indexer", query);
        }
        int finish = (int) System.currentTimeMillis();
        //System.out.println("Indexing Time: " + (finish - timestart));
        // DataBase
//        Mongo mongo = new Mongo();
        mongo.updateCollection("Indexer",Documents);
    }

    public void Build()
    {
        int start = (int) System.currentTimeMillis();
        Mongo mongo = new Mongo();
        Thread[] threads = new Thread[threadsNumber];
        for (int i = 0; i <threadsNumber; i++)
        {
            threads[i] = new Thread(this);
            threads[i].setName(String.valueOf(i));
            threads[i].start();
        }
        for(int i=0;i<threadsNumber;i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        int finish = (int) System.currentTimeMillis();
        System.out.println("Inverting Time: " + (finish - start));
        this.Index();
        System.out.println("Indexing Time: " + ((int)System.currentTimeMillis() - finish));
    }
//
//    private List<pair<Integer, String>> convertTokensToIds(List<pair<pair<String,Integer>, String>> tokens,String URL) {
//        List<pair<Integer, String>> tokensIDs = new ArrayList<>();
//        for (pair<pair<String,Integer>, String> token : tokens)
//        {
//            Integer id= lexicon.get(token.first.first);
//            if (id!=null)
//            {
//                pair<Integer, String> Pair = new pair<>(id, token.second);
//                tokensIDs.add(Pair);
//            }
//            else
//            {
//                id= lexicon.size();
//                lexicon.put(token.first.first, id);
//                pair<Integer, String> Pair = new pair<>(id, token.second);
//                tokensIDs.add(Pair);
//            }
//            String stemWord= Indexer.Stem(token.first.first);
//            Set<String> words=stem.get(stemWord);
//            if(words==null)
//            {
//                words=new HashSet<>();
//                words.add(token.first.first);
//                stem.put(stemWord, words);
//            }
//            else
//                words.add(token.first.first);
//            List<Integer> indices = TagIndices.get(new pair<>(URL,id));
////            System.out.println("URL: " + URL + " id: " + id);
//            if(indices!=null)
//                indices.add(token.first.second);
//            else
//            {
//                indices = new ArrayList<>();
//                indices.add(token.first.second);
//                TagIndices.put(new pair<>(URL,id), indices);
//            }
////            System.out.println("URL: " + URL + " id: " + id + " index: " + token.first.second);
////            System.out.println("TagIndices: " + TagIndices.get(new pair<>(URL,id)).size() + " id: " + id + " index: " + token.first.second);
//        }
//        return tokensIDs;
//    }

    private Map<String, pair<Double, String>> wordCounts(List<pair<pair<String,Integer>, String>> tokens,String URL , Indexer indexer) {
        Map<String, pair<Double, String>> wordCounts = new HashMap<>(); // word, <tf, position>
        for (pair<pair<String,Integer>,String> token : tokens)
        {
            pair<Double, String> wordData= wordCounts.get(token.first.first); //checks if the word was already found in the doc
            if (wordData!=null)
            {
                wordData.first = wordData.first + 1;
                if(postingRanks.containsKey(token.second)) // checks if the new tag is included in postingRanks if not ignore it
                    if(postingRanks.get(wordData.second)> postingRanks.get(token.second)) //checks if new tag has less value (higher priority) than the old one if that happens update the tag
                        wordData.second=token.second;
            }
            else /// remove after assigning all tags scores
            {
                wordData = new pair<>(1.0, token.second);// if not it will be added to the Map wordCounts
                if(!postingRanks.containsKey(token.second)) // if the tag is not included in the postingRanks it will be added with a value of 3
                    postingRanks.put(token.second, 3);
            }
            wordCounts.put(token.first.first, wordData);
            // critical area start
            stemLock.lock();
            String stemWord= indexer.Stem(token.first.first);
            Set<String> words=stem.get(stemWord);
            if(words==null)
            {
                words=new HashSet<>();
                words.add(token.first.first);
                stem.put(stemWord, words);
            }
            else
                words.add(token.first.first);
            stemLock.unlock();
            // critical area end

            Set<Integer> indices = TagIndices.get(new pair<>(URL,token.first.first));
            if(indices!=null)
                indices.add(token.first.second);
            else
            {
                indices = new HashSet<>();
                indices.add(token.first.second);
                TagIndices.put(new pair<>(URL,token.first.first), indices);
            }


        }
        for(Map.Entry<String, pair<Double, String>> word : wordCounts.entrySet())
            word.getValue().first = word.getValue().first / tokens.size();
        return wordCounts;
    }

    private void addToPostings(String URL, Map<String, pair<Double, String>> wordsCounts) {
        for (Map.Entry<String, pair<Double, String>> record : wordsCounts.entrySet())
        {
            pair<String, pair<Double, String>> New_Posting = new pair<>(URL, new pair<>(record.getValue().first, record.getValue().second));
            // Critical area start
            postingsLock.lock();
            List<pair<String, pair<Double, String>>> postingList = postings.get(record.getKey());
            if (postingList!=null)
                postingList.add(New_Posting);
            else
            {
                postingList = new ArrayList<>();
                postingList.add(New_Posting);
                postings.put(record.getKey(), postingList);
            }
            postingsLock.unlock();
            // critical area ends
        }
    }

//    public void InvertOne(Document doc,String URL)
//    {
//        List<pair<pair<String,Integer>, String>> tokens = Indexer.Normalize(doc,URL); //word, index of the tag and tag name
//        List<pair<Integer, String>> tokensIds = convertTokensToIds(tokens,URL);
//        Map<Integer, pair<Double, String>> WordsCounts = wordCounts(tokensIds);
//        addToPostings(URL, WordsCounts);
//    }

    public static void main (String[] args)
    {



    }
}