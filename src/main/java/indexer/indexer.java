package indexer;

import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import stemmer.*;
import stopwordsrm.*;
import structures.pair;


public class indexer {

    private static final englishStemmer stemmer = new englishStemmer();
    private static final StopWordsRemover SWRemover= new StopWordsRemover("StopWords.txt");

    // for Query Processing
    private static final englishStemmer stemmer2 = new englishStemmer();
    private static final StopWordsRemover SWRemover2= new StopWordsRemover("StopWords.txt");

    public static void main (String[]args)
    {}

    public static List<pair<String,String>> Normalize(Document doc)
    {
        //Elements htmlElements = doc.select("title,label,h1,h2,h3,h4,h5,h6,body");
        //Elements htmlElements = doc.select("body:not(:has(h1, h2, h3, h4, h5, h6))");
        Elements htmlElements = doc.select("body");
        Elements elementsToRemove = htmlElements.select("title,label,h1,h2,h3,h4,h5,h6");
        List<pair<String,String>> tokens = new LinkedList<>();
        Tokenize(elementsToRemove,tokens);
        for (Element element : elementsToRemove)
            element.remove();
        Tokenize(htmlElements,tokens);
        return tokens;
    }

    public static void Tokenize (Elements elementsToRemove,List<pair<String,String>> tokens){
        String Text;
        for (Element e : elementsToRemove)
        {
            Text=e.text();
            Text=Text.toLowerCase();
            Text=DocumentCleaner.RemoveSpecialCharacters(Text);
            Text=SWRemover.RemoveStopWords(Text);
            for(String word: Text.split("\\s+"))
            {
                stemmer.setCurrent(word);
                stemmer.stem();
                word=stemmer.getCurrent();
                tokens.add(new pair<>(word, e.tagName()));
            }
        }
    }

    public static List <String> Query_Processing(String Query){
        String Text=Query;
        List<String> Out = new LinkedList<>();
        Text=Text.toLowerCase();
        Text=DocumentCleaner.RemoveSpecialCharacters(Text);
        Text=SWRemover2.RemoveStopWords(Text);
        for(String word: Text.split("\\s+"))
        {
            stemmer2.setCurrent(word);
            stemmer2.stem();
            word=stemmer2.getCurrent();
            Out.add(word);
        }
        return Out;
    }
}