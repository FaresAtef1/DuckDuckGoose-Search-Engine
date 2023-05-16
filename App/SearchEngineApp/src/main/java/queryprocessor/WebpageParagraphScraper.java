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

    public static List<String> ScraperPhraseSearch(List<String> URLs, String Query, List<String> titles, ConcurrentHashMap<String,Integer> URLTagsIndices, int pagenum) throws IOException {

        List<String> paragraphs = new ArrayList<>();
        int start=(pagenum-1)*10;
        int end=start+10;
        if(end>URLs.size())
            end=URLs.size();
        for(int j=start;j<end;j++) // for every URL
        {
            List<Document> doc1=new Mongo().ExecuteQuery(new Document("URL",URLs.get(j)).append("TagIndex",URLTagsIndices.get(URLs.get(j))),"Snippets");
            List<Document> doc2=new Mongo().ExecuteQuery(new Document("URL",URLs.get(j)),"Titles");
            if(doc1.size()<1) {
                System.out.println("Error");
                System.out.println(doc1);
            }
            paragraphs.add(doc1.get(0).getString("Text"));
            titles.add(doc2.get(0).getString("Title"));
        }


        //highlighting
        List<String> QueryWords= new ArrayList<>();
        for(int k=1;k<Query.length()-1;k++)
        {

            StringBuilder temp= new StringBuilder();
            while(k<Query.length() && Query.charAt(k)!='"'&&Query.charAt(k)!=' ')
            {
                temp.append(Query.charAt(k));
                k++;
            }
            String temp2=temp.toString();
            if(!temp2.equalsIgnoreCase("and") && !temp2.equalsIgnoreCase("or"))
                QueryWords.add(temp.toString());
        }
        System.out.println(QueryWords);
        for(int j=0;j< paragraphs.size();j++)
        {
            String [] paragraphWords= paragraphs.get(j).split(" ");
            for(int i=0;i< paragraphWords.length;i++)
            {
                for (String q : QueryWords)
                    if (paragraphWords[i].toLowerCase().contains(q.toLowerCase()))
                        paragraphWords[i] = "<b>" + paragraphWords[i] + "</b>";
            }
            paragraphs.set(j,String.join(" ",paragraphWords));
        }
        return paragraphs;
    }
}