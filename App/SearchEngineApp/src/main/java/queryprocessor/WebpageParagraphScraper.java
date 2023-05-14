package queryprocessor;
import database.Mongo;
import org.bson.Document;

import javax.print.Doc;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebpageParagraphScraper {
    public static List<String> Scraper(List<String> URLs, String Query, List<String> titles, ConcurrentHashMap<String,Set<Integer>> URLTagsIndices, int pagenum) throws IOException {
        List<String> paragraphs = new ArrayList<>();
        Mongo mon=new Mongo();
        int start=(pagenum-1)*10;
        int end=start+10;
        if(end>URLs.size())
            end=URLs.size();
        for(int j=start;j<end;j++) // for every URL
        {
            HashMap<Integer,Integer>MF=new HashMap<>(); // most frequent tag index
            Set<Integer>Indices=URLTagsIndices.get(URLs.get(j));
            for(Integer i:Indices) // tag index
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
            List<Document> doc1=mon.ExecuteQuery(new Document("URL",URLs.get(j)).append("TagIndex",maxindex),"Snippets");
            List<Document> doc2=mon.ExecuteQuery(new Document("URL",URLs.get(j)),"Titles");
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

    public static List<String> ScraperPhraseSearch(List<String> URLs, String Query, List<String> titles, ConcurrentHashMap<String,Set<Integer>> URLTagsIndices, int pagenum) throws IOException {
        List<String> paragraphs = new ArrayList<>();
        String [] words=Query.substring(1, Query.length() - 1).split("\\s+");
        Mongo mon=new Mongo();
        int start=(pagenum-1)*10;
        int end=start+10;
        if(end>URLs.size())
            end=URLs.size();
        for(int j=start;j<end;j++) // for every URL
        {
            Set<Integer> Indices=URLTagsIndices.get(URLs.get(j));
            List<Document> snippetsQuery = new ArrayList<>();
            for(Integer i:Indices) // tag index
            {
              snippetsQuery.add(  new Document("URL", URLs.get(j)).append("TagIndex", i));
            }
            List<Document> snippetsDocs = mon.ExecuteQuery(new Document("$or", snippetsQuery), "Snippets");
            int minseparation=1000000000;
            String bestsnippet="";
            for(Document doc: snippetsDocs)
            {
                int QueryIndex=0;
                int separation=0;
                int lastindex=-1;
                String[] text = doc.getString("Text").split(" ");
                for(int k =0;k <text.length;k++)
                {
                    if(text[k].toLowerCase().contains(words[QueryIndex].toLowerCase()))
                    {
                        QueryIndex++;
                        if(lastindex!=-1)
                           separation+=k-lastindex;
                        lastindex=k;
                        if(QueryIndex==words.length)
                            break;
                    }
                    if(k-lastindex>10)
                    {
                        lastindex=-2;
                        break;
                    }
                }
                if(QueryIndex<words.length||lastindex==-2)
                    continue;
                if(separation<minseparation)
                {
                    minseparation=separation;
                    bestsnippet=doc.getString("Text");
                }

            }
            if(!bestsnippet.equals(""))
                paragraphs.add(bestsnippet);
            else
            {
                URLs.remove(j);
                continue;
            }
            List<Document> doc2=mon.ExecuteQuery(new Document("URL",URLs.get(j)),"Titles");
            titles.add(doc2.get(0).getString("Title"));
        }

        //highlighting
        for(int j=0;j< paragraphs.size();j++)
        {
            String [] paragraphWords= paragraphs.get(j).split(" ");
            for(int i=0;i< paragraphWords.length;i++)
                for(String q:Query.split(" "))
                    if(paragraphWords[i].toLowerCase().contains(q.toLowerCase()))
                        paragraphWords[i] = "<b>" + paragraphWords[i] + "</b>";
            paragraphs.set(j,String.join(" ",paragraphWords));
        }
        return paragraphs;
    }

}