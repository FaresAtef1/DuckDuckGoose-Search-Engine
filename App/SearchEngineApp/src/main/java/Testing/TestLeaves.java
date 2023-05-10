package Testing;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import structures.pair;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TestLeaves {
    public static void main(String[] args) throws IOException {
            URL url = new URL("https://www.bbc.com/news/world-europe-65532088");
            Document doc= org.jsoup.Jsoup.connect(url.toString()).get();
            List<pair<String,String>> Leaves=new ArrayList<>();
            Elements elements=doc.select("*");
            for (Element element : elements) {
                String []words= element.text().split(" ");
                if (element.children().size() == 0 && words.length > 3 && !element.tagName().equals("script") && !element.tagName().equals("style") && !element.tagName().equals("noscript") && !element.tagName().equals("meta") && !element.tagName().equals("link") && !element.tagName().equals("em"))
                    Leaves.add(new pair<>(element.tagName(), element.text()));
            }
        for (pair<String, String> leaf : Leaves) {
            System.out.println(leaf.first + " " + leaf.second);
        }
            System.out.println(Leaves);
    }
}