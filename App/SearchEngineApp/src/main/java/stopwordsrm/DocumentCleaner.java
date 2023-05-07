package stopwordsrm;

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
        return pattern.matcher(html).replaceAll("");
    }

    private DocumentCleaner( ) {
    }

}
