package crawler;

import java.net.*;
import java.util.*;
import java.io.IOException;

public class RobotsTxtChecker {
    public static void GenerateDisallowedURLs(String urlStr) {
        List<String>DisallowedURLs=new ArrayList<>();
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
        if(DisallowedURLs.contains(urlStr))
            System.out.println("true");
        else
            System.out.println("false");
    }

    public static void main (String[] args) throws InterruptedException, MalformedURLException {
        GenerateDisallowedURLs("https://www.google.com/jsky?");
    }
}