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

    public static void main (String[]args)
    {


    }

    public static List<pair<String,String>> Normalize(Document doc)
    {

        Elements htmlElements = doc.select("title,label,h1,h2,h3,h4,h5,h6,p");
        for(Element el : htmlElements)
        {
            System.out.println(el.tagName());
        }

        List<pair<String,String>> tokens = new LinkedList<>();
        String Text;
        for (Element e : htmlElements)
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
                tokens.add(new pair<String,String>(word,e.tagName()));
            }

        }
        return tokens;

    }

}
