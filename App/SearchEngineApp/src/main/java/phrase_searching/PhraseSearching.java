package phrase_searching;
import indexer.Indexer;
import org.bson.Document;
import database.Mongo;
import structures.pair;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class PhraseSearching implements Runnable {

    private ConcurrentLinkedQueue<String> URLsToSearch;

    private ConcurrentHashMap<String,Map<Integer,List<pair<String,Integer>>>> TagIndices; ///< URL ,< tagIndex , count > >

    private ConcurrentHashMap<String,Integer> URLs_Snippets;

    private List<String> words;
    private static final int numberOfThreads=12;
    public PhraseSearching(ConcurrentLinkedQueue<String> URLsToSearch,ConcurrentHashMap<String,Map<Integer,List<pair<String,Integer>>>> TagIndices,List<String> words)
    {
        this.URLsToSearch=URLsToSearch;
        this.TagIndices=TagIndices;
        this.URLs_Snippets=new ConcurrentHashMap<>();
        this.words=words;
    }


    public void run()
    {
        loop:
        while(URLsToSearch.size()!=0)
        {
            String URL=URLsToSearch.poll();
            if(URL==null)
                break;

            Map<Integer,List<pair<String,Integer>>> Indices=TagIndices.get(URL);
            for(Map.Entry<Integer,List<pair<String,Integer>>> entry : Indices.entrySet()) // tag index , list of words in that tag and their indices
            {
                int TatIndex=entry.getKey();
                List<pair<String,Integer>>  wordsIndices=entry.getValue() ; // word , index of that word in that tag

                if(wordsIndices.size()>= words.size())
                {
                    Collections.sort(wordsIndices, new Comparator<pair<String, Integer>>() { // sort the words in the tag according to their indices
                        @Override
                        public int compare(pair<String, Integer> p1, pair<String, Integer> p2) {
                            return p1.second.compareTo(p2.second);
                        }
                    });

                    int lastPosition=-1;
                    int lastWordIndex=-1;
                    int QueryIndex=0;
                    for(int j=0;j<wordsIndices.size();j++)
                    {
                        pair<String,Integer> wordIndex=wordsIndices.get(j);
                        String word=wordIndex.first;
                        int index=wordIndex.second;
//                        System.out.println("URL "+ URL + " tag index "+TatIndex+" and the word is "+word+"  and the word index in the tag "+index);
                        if(word.equalsIgnoreCase(words.get(QueryIndex)) &&(index-lastWordIndex<10||(lastWordIndex==-1&& QueryIndex==0)))
                        {
                            QueryIndex++;
                            lastWordIndex=index;
                            System.out.println("URL "+ URL + " tag index "+TatIndex+" and the word is "+word+"  and the word index in the tag "+index +" and the query index is "+QueryIndex);
                            if(QueryIndex==words.size())
                            {
                                URLs_Snippets.put(URL,TatIndex);
                                continue loop;
                            }

                        }
                        else
                        {
                            for(int k=QueryIndex;k>=0;k--)
                                if(word.equalsIgnoreCase(words.get(k)))
                                {
                                    QueryIndex=k;
                                    lastWordIndex=index;
                                }
                        }


                    }

                }
            }
        }
    }


    public static List<String> phraseSearch(String input,ConcurrentHashMap<String,Integer> ResultsList)
    {
        System.out.println("the query sent to the phrase search is "+input);
        Indexer indexer=new Indexer();
        List<String> words = indexer.Query_Processing(input);
        if(words.isEmpty())
            return null;
        System.out.println("the words after processing are "+words);
        List<Document> queries = new ArrayList<>();
        for (String word : words)
            queries.add(new Document("stemmedWord", indexer.Stem(word)));
        Document query = new Document("$or", queries); // Combine queries with logical OR
        Mongo mongo = new Mongo();
        List<Document> documents = mongo.ExecuteQuery(query, "Indexer");
        ///////////////////////////////////////////////////////////////////////////
        int timeStart = (int) System.currentTimeMillis();
        ConcurrentHashMap<String,Map<Integer,List<pair<String,Integer>>>> out =new ConcurrentHashMap<>(); ///< URL ,< tagIndex , < word , wordIndex > > >
        for(Document doc : documents)
        {
            List<Document> postings= (List<Document>) doc.get("postings");
            for(Document posting : postings)
            {
                String actualWord = (String) posting.get("actualWord");
                for(int i=0; i< words.size();i++)
                {
                    if(actualWord.equalsIgnoreCase(words.get(i)))
                    {
                        List<Document> postingsList = (List<Document>) posting.get("postings");
                        for(Document postingDoc : postingsList)
                        {
                            String url = (String) postingDoc.get("DocURL");
                            List<Document> indices = (List<Document>) postingDoc.get("TagsList");
                            for(Document doc2 : indices)
                            {
                                int tagIndex=(int) doc2.get("TagIndex");
                                int wordIndex=(int) doc2.get("WordIndex");
                                pair<String,Integer> p=new pair<>(actualWord,wordIndex);
                                Map<Integer,List<pair<String,Integer>>> map = out.get(url);  /// get the map of that URL that contains all the tag indices and the words in that tag
                                if(map == null)
                                {
                                    map = new HashMap<>(); /// if the map is null then create a new one
                                    List<pair<String,Integer>> list = new ArrayList<>(); /// create a new list
                                    list.add(p); /// add the word to the list
                                    map.put(tagIndex,list); /// put the word in the map
                                    out.put(url,map); /// put the map in the out map
                                }
                                else
                                {
                                    List<pair<String,Integer>> list = map.get(tagIndex); /// get the list of words in that tag
                                    if(list == null)
                                    {
                                        list = new ArrayList<>(); /// if the list is null then create a new one
                                        list.add(p); /// add the word to the list
                                        map.put(tagIndex,list); /// put the list in the map
                                    }
                                    else
                                    {
                                        list.add(p); /// add the word to the list
                                    }
                                }
                            }
                        }

                    }
                }
            }

        }
        System.out.println("the out map size is "+out.size());
        /////////////////////////////////////////////////////////////////////////////
        PhraseSearching phraseSearching=new PhraseSearching(new ConcurrentLinkedQueue<>(out.keySet()),out,words);
        Thread[] threads=new Thread[numberOfThreads];
        for(int i=0;i<numberOfThreads;i++)
        {
            threads[i]=new Thread(phraseSearching);
            threads[i].start();
        }
        for(int i=0;i<numberOfThreads;i++)
        {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for(String URL : phraseSearching.URLs_Snippets.keySet())
        {
            System.out.println(URL + " " + phraseSearching.URLs_Snippets.get(URL));
        }

        /////////////////// ranking ///////////////////////////
        if(phraseSearching.URLs_Snippets==null)
        {
            return null;
        }
        System.out.println("the size of the URLs_Snippets map is "+phraseSearching.URLs_Snippets.size());
        ResultsList.putAll(phraseSearching.URLs_Snippets);
        List<Document> pagerankQuery = new ArrayList<>();
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
        return urls;
    }

    public static List<String> phraseSearchAndOr(String FullQuery,ConcurrentHashMap<String,Integer> ResultsList,int count)
    {
        if(count==2)
        {
            String query=FullQuery.substring(1,FullQuery.length()-1);
            return phraseSearch(query,ResultsList);
        }
        int beginIndex=-1;
        int endIndex=-1;
        for(int i=1;i<FullQuery.length()&&endIndex==-1;i++)
        {
            if(FullQuery.charAt(i)=='"')
            {
                if(beginIndex==-1)
                    beginIndex=i+1;
                else
                    endIndex=i-1;
            }
        }
        String firstQuery=FullQuery.substring(1,beginIndex-1);
        String secondQuery=FullQuery.substring(endIndex+2,FullQuery.length()-1);
        String op=FullQuery.substring(beginIndex,endIndex+1);
        ConcurrentHashMap<String,Integer> ResultsList1=new ConcurrentHashMap<>();
        ConcurrentHashMap<String,Integer> ResultsList2=new ConcurrentHashMap<>();
        List<String> urls1=phraseSearch(firstQuery,ResultsList1);
        List<String> urls2=phraseSearch(secondQuery,ResultsList2);
        Set<String> urls=new HashSet<>();
        if(op.equalsIgnoreCase("AND"))
        {
            for(String url : ResultsList1.keySet())
            {
                if(ResultsList2.containsKey(url))
                {
                    ResultsList.put(url,ResultsList1.get(url));
                    urls.add(url);
                }
            }
        }
        else if(op.equalsIgnoreCase("OR"))
        {
            ResultsList.putAll(ResultsList1);
            ResultsList.putAll(ResultsList2);
            urls.addAll(ResultsList1.keySet());
            urls.addAll(ResultsList2.keySet());
        }
        return urls.stream().toList();
    }

    public static void main(String[] args) {
        ConcurrentHashMap<String,Integer> ResultsList=new ConcurrentHashMap<>();
        List<String> words=phraseSearchAndOr("\"donald\"and\"trump\"",ResultsList,4);
        for(String url : ResultsList.keySet()) {
            System.out.println(url + " " + ResultsList.get(url));
        }


    }
}
