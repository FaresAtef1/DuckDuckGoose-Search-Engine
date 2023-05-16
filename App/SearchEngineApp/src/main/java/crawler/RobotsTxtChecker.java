package crawler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class RobotsTxtChecker {
    List<String>DisallowedURLs=new ArrayList<>();
    public void GenerateDisallowedURLs(String urlStr) {
        try {
            URL url = new URL(urlStr);
            String robotsUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";
            try (Scanner scanner = new Scanner(new URL(robotsUrl).openStream())) {
                String userAgent = "User-agent: *";
                boolean matched = false;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("User-agent"))
                        matched = line.equals(userAgent);
                    if (matched && line.startsWith("Disallow:")) {
                        String path = line.substring("Disallow:".length()).trim();
                        if (!path.isEmpty())
                            DisallowedURLs.add(url.getProtocol() + "://" + url.getHost() + path);
                    }
                }
            }
        } catch (IOException ignored) {}
    }

    public static void main (String[] args) throws InterruptedException, MalformedURLException {
        String url1="https://www.wikipedia.org/";
//        Set<String> disallowedURLs = GenerateDisAllowedURLs(url1);
//        if(disallowedURLs.contains("https://www.wikipedia.org/wiki/ויקיפדיה%3Aדפים_ליימים_ומוגנים"))
//            System.out.println("true");
//        else
//            System.out.println("false");
    }
}