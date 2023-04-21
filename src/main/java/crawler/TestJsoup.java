package crawler;

import org.jsoup.select.Elements;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
public class TestJsoup {

    public static void main(String[] args) throws IOException
    {
        // simple URL https://www.york.ac.uk/teaching/cws/wws/webpage1.html
        String url = "https://www.york.ac.uk/teaching/cws/wws/webpage1.html";
        try
        {
            Document doc = Jsoup.connect(url).get();
            String title = doc.title();
            System.out.println("Title: " + title);

            Elements links = doc.select("a[href]");

            for(Element link:links)
            {
                System.out.println("\nlink :"+link.attr("href"));
                System.out.println("text: "+link.text());
            }

        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
