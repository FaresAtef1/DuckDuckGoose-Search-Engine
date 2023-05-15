package stopwordsrm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StopWordsRemover {

    private static  Set<String> stopWords;

    public StopWordsRemover(String filename) {
        if(stopWords==null)
        {
            stopWords = new HashSet<String>();
            try {
                BufferedReader reader= new BufferedReader(new FileReader(filename));
                String line=reader.readLine();
                while(line!=null)
                {
                    stopWords.add(line);
                    line=reader.readLine();
                }
                reader.close();
            }
            catch(Exception e)
            {
                System.out.println("cant find");
                System.err.print(e.getMessage());
            }
        }
    }
    public String RemoveStopWords(String text)
    {
        List<String> list = new ArrayList<>();
        for(String word:text.split(" "))
        {
            if(!stopWords.contains(word.toLowerCase()))
            {
                list.add(word);
            }
        }
        return String.join(" ", list);

    }

    public static void main (String[] args) {
        StopWordsRemover swr = new StopWordsRemover("StopWords.txt");
        System.out.println(swr.RemoveStopWords("I am a student"));
    }

}
