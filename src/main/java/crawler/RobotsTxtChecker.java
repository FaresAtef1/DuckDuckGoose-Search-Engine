package crawler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RobotsTxtChecker {
    public static void main(String[] args) throws IOException {
        Set<String> VisitedURLs= Collections.synchronizedSet(new HashSet<String>());;
        String urlString = "https://www.yotube.com/robots.txt";
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("Disallow")&&line.length()>12)
                {
                    line=line.substring(10); //remove "Disallow: "
                    VisitedURLs.add("https://www.youtube.com"+line);
                }
            }
            System.out.println(VisitedURLs);
            reader.close();
        }
        catch (IOException e )
        {}
    }
}