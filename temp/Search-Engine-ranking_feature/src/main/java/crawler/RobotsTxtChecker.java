package crawler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class RobotsTxtChecker {
    public static Set<String> GenerateDisAllowedURLs(String urlStr){
        Set<String> disallowedURLs = new HashSet<>();
        try{
            URL url = new URL(urlStr);
            String robotsUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";
            Scanner scanner = new Scanner(new URL(robotsUrl).openStream());
            String userAgent = "User-agent: *";
            boolean matched = false;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("User-agent"))
                {
                    matched = line.equals(userAgent);
                }
                if (matched && line.startsWith("Disallow:"))
                {
                    String path = line.substring("Disallow:".length()).trim();
                    if (!path.isEmpty())
                        disallowedURLs.add(url.getProtocol() + "://" + url.getHost() + path);
                }
            }
        }
        catch(IOException ignored){}
        return disallowedURLs;
    }

    public static void main (String[] args) throws InterruptedException, MalformedURLException {
        String url1="https://www.wikipedia.org/";
        Set<String> disallowedURLs = GenerateDisAllowedURLs(url1);
        if(disallowedURLs.contains("https://www.wikipedia.org/wiki/ויקיפדיה%3Aדפים_ליימים_ומוגנים"))
            System.out.println("true");
        else
            System.out.println("false");
    }
}