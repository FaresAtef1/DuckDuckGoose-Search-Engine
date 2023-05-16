package stopwordsrm;

import indexer.Indexer;

import java.util.regex.Pattern;

public class DocumentCleaner {

    public static String RemoveHtmlTags(String html)
    {
        Pattern pattern = Pattern.compile("<.*?>");
        return pattern.matcher(html).replaceAll("");
    }
    public static String RemoveSpecialCharacters(String html)
    {
        Pattern pattern = Pattern.compile("(?<=\\s|^)[^a-zA-Z0-9]+(?=\\s|$)|(\\p{Punct})");
        return pattern.matcher(html).replaceAll(" ");
    }

    public static void main(String[] args) {
        System.out.println(RemoveSpecialCharacters("amazon com"));
        Indexer indexer = new Indexer();
        for(String word : indexer.Query_Processing("amazon.com"))
            System.out.println(word);
    }

    private DocumentCleaner( ) {
    }

}
