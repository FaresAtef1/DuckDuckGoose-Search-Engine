package queryprocessor;
import database.Mongo;
import org.bson.Document;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebpageParagraphScraper {

    private static  int NUM_OF_THREADS = 5;


    private  static void Scrape(int start, int end, List<String>URLs, ConcurrentHashMap<String, Set<Integer>> URLTagsIndices, Mongo mon, List<String> paragraphs, List<String>titles)
    {
        for(int j=start;j<end;j++)
        {
            String URL=URLs.get(j);
            HashMap<Integer,Integer>MF=new HashMap<>();
            Set<Integer>Indices=URLTagsIndices.get(URL);
            for(Integer i:Indices)
            {
                if(MF.containsKey(i))
                    MF.put(i,MF.get(i)+1); // increment frequency
                else
                    MF.put(i,1); // add new tag index
            }
            int maxval=-1;
            int maxindex=-1;
            for(Map.Entry<Integer,Integer> entry:MF.entrySet())
            {
                if(entry.getValue()>maxval)
                {
                    maxval=entry.getValue();
                    maxindex=entry.getKey();
                }
            }
            List<Document> doc1=mon.ExecuteQuery(new Document("URL",URL).append("TagIndex",maxindex),"Snippets");
            List<Document> doc2=mon.ExecuteQuery(new Document("URL",URL),"Titles");
            if(doc1.size()>1) {
                System.out.println("Error");
                System.out.println(doc1);
            }
//
//            String Title=doc2.get(0).getString("Title");
//            if(Title==)
            paragraphs.add(doc1.get(0).getString("Text"));
            titles.add(doc2.get(0).getString("Title"));
        }
        System.out.println("Thread "+Thread.currentThread().getName()+" Paragraps size "+paragraphs.size()+" Titles size "+titles.size());
    }
    public static List<String> Scraper(List<String> URLs, String Query, List<String> titles, ConcurrentHashMap<String, Set<Integer>> URLTagsIndices, int pagenum) throws IOException {
        List<String> paragraphs = new ArrayList<>();
        Mongo mon=new Mongo();
        int start=(pagenum-1)*10;
        int end=start+10;
        if(end>URLs.size())
            end=URLs.size();
        Thread [] threads=new Thread[5];
        if(end-start<NUM_OF_THREADS)
            NUM_OF_THREADS=end-start;
        for(int i=0;i<NUM_OF_THREADS;i++)
        {
            int s=start+(end-start)*i/NUM_OF_THREADS;
            int e;
            if(i==NUM_OF_THREADS-1)
                e=end;
            else {
                e = start + (end - start) * (i + 1) / NUM_OF_THREADS;
            }
            System.out.println("Thread "+i+" start "+s+" end "+e);
            threads[i]=new Thread(()->Scrape(s,e,URLs,URLTagsIndices,mon,paragraphs,titles));
            threads[i].start();
        }
        for(int i=0;i<NUM_OF_THREADS;i++)
        {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for(int j=0;j< paragraphs.size();j++)
        {
            String [] words= paragraphs.get(j).split(" ");
            for(int i=0;i< words.length;i++)
                for(String q:Query.split(" "))
                    if(words[i].toLowerCase().contains(q.toLowerCase()))
                        words[i] = "<b>" + words[i] + "</b>";
            paragraphs.set(j,String.join(" ",words));
        }
        return paragraphs;
    }

    public static List<String> ScraperPhraseSearch(List<String> URLs, String Query, List<String> titles, ConcurrentHashMap<String,Set<Integer>> URLTagsIndices, int pagenum,List<String>Final_URLs) throws IOException {
        Final_URLs.addAll(URLs);
        List<String> paragraphs = new ArrayList<>();
        String [] words=Query.substring(1, Query.length() - 1).split("\\s+");
        Mongo mon=new Mongo();
        int start=(pagenum-1)*10;
        int end=start+10;
        if(end>URLs.size())
            end=URLs.size();
        System.out.println("start: "+start+" end: "+end);
        for(int j=start;j<end&&end<=URLs.size();j++) // for every URL
        {
            System.out.println("begiiin");
            Set<Integer> Indices=URLTagsIndices.get(URLs.get(j));
            List<Document> snippetsQuery = new ArrayList<>();
            for(Integer i:Indices) // tag index
              snippetsQuery.add(  new Document("URL", URLs.get(j)).append("TagIndex", i));
            List<Document> snippetsDocs = mon.ExecuteQuery(new Document("$or", snippetsQuery), "Snippets");
            int minseparation=1000000;
            String bestsnippet="";
            for(Document doc: snippetsDocs)
            {
                System.out.println("kjsahdkjad");
                System.out.println("doc: "+doc.getString("Text"));
                System.out.println();
                int QueryIndex=0;
                int separation=0;
                int lastindex=-1;
                String[] text = doc.getString("Text").split(" ");
                for(int i=0;i<text.length;i++)
                    System.out.print(text[i]+"  ");
                for(int k =0;k <text.length;k++)
                {
                    if(text[k].toLowerCase().contains(words[QueryIndex].toLowerCase()))
                    {

                        System.out.println("hereeee"+words.length);
                        QueryIndex++;
                        if(lastindex!=-1)
                           separation+=k-lastindex;
                        lastindex=k;
                        if(QueryIndex==words.length)
                        {
                         System.out.println("hereeee4");
                            break;
                        }
                    }
                    if(text[k].contains(".")||text[k].contains(","))
                    {
                        QueryIndex = 0;
                        lastindex = -1;
                        separation = 0;
                    }
                    if(k-lastindex>10&&lastindex!=-1)
                    {
                        lastindex=-2;
                        System.out.println("hereeee3");
                        break;
                    }
                }
                if(QueryIndex<words.length||lastindex==-2)
                {
                    System.out.println(QueryIndex);
                    continue;
                }
                if(separation<minseparation)
                {
                    minseparation=separation;
                    bestsnippet=doc.getString("Text");
                }
            }
            if(!bestsnippet.equals("")) {
                System.out.println("added"+bestsnippet);
                paragraphs.add(bestsnippet);
            }
            else {
                System.out.println("removed"+URLs.get(j));
                end++;
                Final_URLs.remove(j);
                continue;
            }
            System.out.println("best snippet: "+bestsnippet);
            List<Document> doc2=mon.ExecuteQuery(new Document("URL",URLs.get(j)),"Titles");
            titles.add(doc2.get(0).getString("Title"));
        }
        System.out.println("size of final URLs"+Final_URLs.size());
        System.out.println("paragraphs size"+paragraphs.size());
//        URLs.removeAll(To_Be_Removen);
        //highlighting
        for(int j=0;j< paragraphs.size();j++)
        {
            String [] paragraphWords= paragraphs.get(j).split(" ");
            for(int i=0;i< paragraphWords.length;i++)
                for(String q:Query.substring(1,Query.length()-1).split(" "))
                    if(paragraphWords[i].toLowerCase().contains(q.toLowerCase()))
                        paragraphWords[i] = "<b>" + paragraphWords[i] + "</b>";
            paragraphs.set(j,String.join(" ",paragraphWords));
        }
        return paragraphs;
    }

}