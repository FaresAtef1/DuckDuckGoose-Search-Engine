package queryprocessor;
import database.Mongo;
import org.bson.Document;

import java.io.IOException;
import java.util.*;

public class WebpageParagraphScraper {
    public static List<String> Scraper(List<String> URLs,String Query,List<String> titles,Map<String,List<Integer>>URLTagsIndices,int pagenum) throws IOException {
        List<String> paragraphs = new ArrayList<>();
        Mongo mon=new Mongo();
        int start=(pagenum-1)*10;
        int end=start+10;
        if(end>URLs.size())
            end=URLs.size();
        for(int j=start;j<end;j++)
        {
            HashMap<Integer,Integer>MF=new HashMap<>();
            List<Integer>Indices=URLTagsIndices.get(URLs.get(j));
            for(Integer i:Indices)
            {
                if(MF.containsKey(i))
                    MF.put(i,MF.get(i)+1);
                else
                    MF.put(i,1);
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
}