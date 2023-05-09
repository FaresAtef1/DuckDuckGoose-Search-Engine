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
        Map<pair<String,String>,Integer> m=new HashMap<>();
        m.put(new pair<>("a","b"),1);
        System.out.println(m.get(new pair<>("a","b")));

    }
}