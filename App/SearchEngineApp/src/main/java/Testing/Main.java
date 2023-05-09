package Testing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        int starttime = (int) System.currentTimeMillis();
        for(int i=0;i<400;i++) {
            String url = "https://www.bbc.com/";
//            try {
//                URLConnection connection = new URL(url).openConnection();
//                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String inputLine;
//                StringBuilder content = new StringBuilder();
//                while ((inputLine = in.readLine()) != null) {
//                    content.append(inputLine);
//                }
//                in.close();
//                String document = content.toString();
                System.out.println(i);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            Document doc = Jsoup.connect(url).get();
        }
        int endtime = (int) System.currentTimeMillis();
        System.out.println(endtime-starttime);
    }
}