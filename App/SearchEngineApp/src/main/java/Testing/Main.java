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
        String query="\"hello world \"hello world\"";
        System.out.println(query);
        int count = 0;
        for(int i=0;i<query.length();i++)
        {
            if(query.charAt(i)=='"')
                count++;
        }
        System.out.println(count);


    }
}