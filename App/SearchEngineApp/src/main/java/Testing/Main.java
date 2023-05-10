package Testing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import structures.pair;

import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
       Document doc=Jsoup.connect("https://www.bbc.co.uk/search?d=HOMEPAGE_PS").get();
       System.out.println(doc.title());
    }
}