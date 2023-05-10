package indexer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import database.Mongo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import stemmer.*;
import stopwordsrm.*;
import structures.pair;


public class Indexer {

    private static final englishStemmer stemmer = new englishStemmer();
    private static final StopWordsRemover SWRemover = new StopWordsRemover("StopWords.txt");


    public static void main(String[] args) {

    }
    public static void Print(String text)
    {
        System.out.println(text);
    }

    private static String Clean(String Text)
    {
        Text=Text.toLowerCase();
        Text=DocumentCleaner.RemoveSpecialCharacters(Text);
        Text=SWRemover.RemoveStopWords(Text);
        return  Text;
    }

    public static List<pair<String,String>> GetLeaves(Document doc) {
        List<pair<String,String>> Leaves=new ArrayList<>();
        Elements elements=doc.select("*");
        for (Element element : elements) {
        String []words= element.text().split(" ");
            if (element.children().size() == 0 && words.length > 3 && !element.tagName().equals("script") && !element.tagName().equals("style") && !element.tagName().equals("noscript") && !element.tagName().equals("meta") && !element.tagName().equals("link") && !element.tagName().equals("em"))
                Leaves.add(new pair<>(element.tagName(), element.text()));
        }
        return Leaves;
    }

    public static List<pair<pair<String,Integer>,String>> Normalize(Document doc,String URL)
    {
        List<pair<String,String>> Leaves=GetLeaves(doc);
        List<pair<pair<String,Integer>,String>> tokens = new LinkedList<>(); //word, index of the tag and tag name
        Tokenize(Leaves,tokens,URL);
        return tokens;
    }

    public static String Stem(String word)
    {
        if(word.length()==0)
            return word;
        stemmer.setCurrent(word);
        stemmer.stem();
        word=stemmer.getCurrent();
        return word;
    }

    public static void Tokenize (List<pair<String,String>> elements,List<pair<pair<String,Integer>,String>> tokens,String URL){
        List<org.bson.Document> queries = new ArrayList<>();
        String Text;
        for (int i=0;i<elements.size();i++)
        {
            Text=elements.get(i).second;
            queries.add(new org.bson.Document("URL",URL).append("TagIndex", i).append("Text", Text));
            Text=Clean(Text);
            for(String word: Text.split("\\s+"))
            {
                if (!word.matches("[a-z0-9]+"))
                    continue;
                tokens.add(new pair<>(new pair<>(word,i), elements.get(i).first)); //word, index of the tag and tag name
            }
        }
        Mongo mongo = new Mongo();
        if(queries.size()>0)
            mongo.AddToCollection("Snippets",queries);
    }

    public static List <String> Query_Processing(String Query){
        String Text=Query;
        List<String> Out = new LinkedList<>();
        Text=Clean(Text);
        for(String word: Text.split("\\s+"))
        {
            if (!word.matches("[a-z0-9]+"))
                continue;
            Out.add(word);
        }
        return Out;
    }
}