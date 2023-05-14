package phrase_searching;
import indexer.Indexer;
import org.bson.Document;
import database.Mongo;
import structures.pair;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;


public class PhraseSearching implements Runnable {
    private ConcurrentLinkedQueue<Document> results ;   /// List of documents returned from the database
    private ConcurrentHashMap<String,Map<Integer,Integer>> out ; ///< URL ,< tagIndex , count > >
    private ConcurrentHashMap<String,Set<Integer>> resultsList;// <URL,Set <TagIndex>>
    private List<String> words; /// List of words in the query

    private static ReentrantLock lock = new ReentrantLock();
    private static final int numberOfThreads=4;
    public PhraseSearching(ConcurrentLinkedQueue<Document> results, List<String> words,ConcurrentHashMap<String,Set<Integer>> ResultsList)
    {
        this.results=results;
        this.out=new ConcurrentHashMap<>();
        this.resultsList=ResultsList;
        this.words=words;
//        this.resultsList
    }
    public void  run()
    {
            while (results.size() != 0) {
                Document doc = results.poll();
                if (doc == null)
                    break;
                List<Document> postings = (List<Document>) doc.get("postings");
                for (Document posting : postings) {
                    String actualWord = (String) posting.get("actualWord");
                    for (int i = 0; i < words.size(); i++) {
                        if (actualWord.equalsIgnoreCase(words.get(i))) {
                            List<Document> postingsList = (List<Document>) posting.get("postings");
                            for (Document postingDoc : postingsList) {
                                String url = (String) postingDoc.get("DocURL");
                                List<Document> indices = (List<Document>) postingDoc.get("TagsList");
                                for (Document doc2 : indices) {
                                    lock.lock();
                                    int index = (int) doc2.get("TagIndex");
                                    Map<Integer, Integer> map = out.get(url); /// counting the ferquency of the tagIndex
                                    if (map == null) {
                                        map = new HashMap<>();
                                        map.put(index, 1);
                                        out.put(url, map);
                                    } else {
                                        Integer count = (Integer) map.get(index);
                                        if (count == null)
                                            map.put(index, 1);
                                        else
                                            map.put(index, count + 1);
                                    }
                                    if(map.get(index).equals(words.size())) // if the count of the tagIndex equals the number of words in the query
                                    {
                                        Set<Integer>  set=resultsList.get(url); // add the url to the resultsList and the tagIndex
                                        if(set==null)
                                        {
                                            set=new HashSet<>();
                                            set.add(index);
                                            resultsList.put(url,set);
                                        }
                                        else
                                        {
                                            set.add(index);
                                            resultsList.put(url,set);
                                        }
                                    }
                                    lock.unlock();
                                }
                            }

                        }
                    }
                }
            }
    }

    public static List<String> phraseSearch(String input,ConcurrentHashMap<String,Set<Integer>> ResultsList)
    {
        Indexer indexer=new Indexer();
        List<String> words = indexer.Query_Processing(input);
        if(words.isEmpty())
            return null;
        System.out.println(words);
        List<Document> queries = new ArrayList<>();
        for (String word : words)
            queries.add(new Document("stemmedWord", indexer.Stem(word)));
        Document query = new Document("$or", queries); // Combine queries with logical OR
        Mongo mongo = new Mongo();
        List<Document> documents = mongo.ExecuteQuery(query, "Indexer");

        ConcurrentLinkedQueue<Document> results = new ConcurrentLinkedQueue<>(documents);
        Thread [] threads = new Thread[numberOfThreads];
        PhraseSearching phraseSearching = new PhraseSearching(results,words,ResultsList);
        for(int i=0;i<numberOfThreads;i++)
        {
            threads[i]=new Thread(phraseSearching);
            threads[i].start();
        }
        for (int i = 0; i < numberOfThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ignored) {}
        }
        /////////////////// ranking ///////////////////////////
//        ResultsList=phraseSearching.resultsList;
        System.out.println(ResultsList);
        List<Document> pagerankQuery = new ArrayList<>();
        if(ResultsList==null)
            System.out.println("nullll2");
        for(String url : ResultsList.keySet())
        {
            pagerankQuery.add(new Document("DocURL",url));
        }
        if(pagerankQuery.isEmpty())
            return null;
        List<Document>pageRankScore=mongo.ExecuteQuery(new Document("$or",pagerankQuery),"PageRankScores");
        Map<String, Double> pageRankMap = new HashMap<>();
        for(Document doc : pageRankScore)
        {
            String url=(String)doc.get("DocURL");
            Double score=(Double) doc.get("PageRankScore");
            pageRankMap.put(url,score);
        }
        List<String> urls=pageRankMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
//        if(ResultsList==null)
//            System.out.println(results);
        return urls;
    }
    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        String input = scanner.nextLine();
//        String[] targetPhrase = input.substring(1, input.length() - 1).split("\\s+");
//        Indexer indexer=new Indexer();
//        List<String> words = indexer.Query_Processing(input);
//        if(words.isEmpty())
//            return;
//        System.out.println(words);
////        for(int i=0;i<words.size();i++)
////            words.set(i,indexer.Stem(words.get(i)));
//        List<Document> queries = new ArrayList<>();
//        for (String word : words)
//            queries.add(new Document("stemmedWord", indexer.Stem(word)));
//        Document query = new Document("$or", queries); // Combine queries with logical OR
//        Mongo mongo = new Mongo();
//        List<Document> documents = mongo.ExecuteQuery(query, "Indexer");
//
//        ConcurrentLinkedQueue<Document> results = new ConcurrentLinkedQueue<>(documents);
//        Thread [] threads = new Thread[numberOfThreads];
////        PhraseSearching phraseSearching = new PhraseSearching(results,words,);
//        for(int i=0;i<numberOfThreads;i++)
//        {
//            threads[i]=new Thread(phraseSearching);
//            threads[i].start();
//        }
//        for (int i = 0; i < numberOfThreads; i++) {
//            try {
//                threads[i].join();
//            } catch (InterruptedException ignored) {}
//        }




//        HashMap<String,Element> out = new HashMap<>();
//        List<String> distinctValues = new ArrayList<>();
//        collection.distinct("postings.postings.DocURL",query, String.class).into(distinctValues);\

//        for(Document doc : results)
//        {
//           List<Document> postings= (List<Document>) doc.get("postings");
//           for(Document posting : postings)
//           {
//               String actualWord = (String) posting.get("actualWord");
//               for(int i=0; i< words.size();i++)
//                {
//                     if(actualWord.equalsIgnoreCase(words.get(i)))
//                     {
//                          List<Document> postingsList = (List<Document>) posting.get("postings");
//                          for(Document postingDoc : postingsList)
//                          {
//                              String url = (String) postingDoc.get("DocURL");
//                              List<Document> indices = (List<Document>) postingDoc.get("TagsList");
//                              for(Document doc2 : indices)
//                              {
//                                 int index=(int) doc2.get("TagIndex");
//                                  Map map = out.get(url);
//                                  if(map == null)
//                                  {
//                                      map = new HashMap<>();
//                                      map.put(index,1);
//                                      out.put(url,map);
//                                  }
//                                  else
//                                  {
//                                      Integer count = (Integer) map.get(index);
//                                      if(count == null)
//                                          map.put(index,1);
//                                      else
//                                          map.put(index,count+1);
//                                  }
//                              }
//                          }
//
//                     }
//                }
//           }
//
//        }
//        Set<pair<String,String>> resultsList = new HashSet<>();
//        for(Map.Entry<String,Map<Integer,Integer>> entry : out.entrySet())
//        {
//            String url = entry.getKey();
//            Map<Integer,Integer> map = entry.getValue();
//            for(Map.Entry<Integer,Integer> index: map.entrySet())
//            {
//                Integer frequency = index.getValue();
//                if(frequency == words.size())
//                {
//                    List<Document> docs=mongo.ExecuteQuery(new Document("URL",url).append("TagIndex", index.getKey()),"Snippets");
//                    resultsList.add(new pair<>(url,docs.get(0).getString("Text")));
//
//
//                }
//            }
//
//        }

//        for(pair<String,String> result : resultsList)
//        {
//            System.out.println(result.first);
//            System.out.println(result.second);
//        }

//        scanner.close();
    }
}
