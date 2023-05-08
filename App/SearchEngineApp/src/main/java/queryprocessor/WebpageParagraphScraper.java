package queryprocessor;
import java.io.IOException;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WebpageParagraphScraper {
    public static List<String> Scraper(List<String> URLs,String Query,List<String> titles) throws IOException {
        Map<String, Integer> TagScore = new HashMap<>();
        TagScore.put("title", 0);
        TagScore.put("h1", 1);
        TagScore.put("h2", 2);
        TagScore.put("h3", 3);
        TagScore.put("h4", 4);
        TagScore.put("h5", 5);
        TagScore.put("h6", 6);
        TagScore.put("meta", 1);
        TagScore.put("p", 3);
        TagScore.put("th", 1);
        TagScore.put("td", 3);
        TagScore.put("li", 3);
        TagScore.put("a", 2);
        String[] query = Query.split(" ");
//        titles=new ArrayList<>();
        List<String> paragraphs=new ArrayList<>();
        for (String url : URLs) {
            Document doc = Jsoup.connect(url).get();
            String title = doc.title();
            Elements elements = doc.select("p,li,td,th,h1,h2,h3,h4,h5,h6,a,meta");
            String bestParagraph = null;
            int bestScore = -1;
            String bestTag = null;
            for (org.jsoup.nodes.Element element : elements) {
                if (element.tagName().equals("body") || (element.tagName().equals("meta") && !element.attr("name").equals("description")))
                    continue;
                String paragraph = element.text();
                int score = 0;
                for (String s : query)
                    if (paragraph.toLowerCase().contains(s.toLowerCase()))
                        score++;
                if (score > bestScore) {
                    bestScore = score;
                    bestParagraph = paragraph;
                    bestTag = element.tagName();
                } else if (score == bestScore)
                    if (TagScore.get(element.tagName()) < TagScore.get(bestTag)) {
                        bestParagraph = paragraph;
                        bestTag = element.tagName();
                    }
            }
            System.out.println(title);
            if (bestParagraph == null) {
                System.out.println("No paragraph found");
                break;
            }
            String[] bestParagraphArray = bestParagraph.split(" ");
            for (int i = 0; i < bestParagraphArray.length; i++) {
                for (String s : query)
                    if (bestParagraphArray[i].toLowerCase().contains(s.toLowerCase())) {
                        bestParagraphArray[i] = "<b>" + bestParagraphArray[i] + "</b>";
                        break;
                    }
            }
            StringBuilder sb = new StringBuilder();
            for (String str : bestParagraphArray) {
                sb.append(str);
                sb.append(" ");
            }
            titles.add(title);
            paragraphs.add(sb.toString());
        }
        return paragraphs;
    }
}