package crawler;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ranker.PageRanker;

import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crawler implements Runnable{
    private String BaseURL;  // it is not used
    private AtomicInteger CrawledNum; // Thread-safe counter
    private ConcurrentLinkedQueue<String> URLsToCrawl;
    private static ConcurrentHashMap<String, Set<String>> inLinks;
    private static ConcurrentHashMap<String, Set<String>> outLinks;
    private ConcurrentHashMap<String,String> VisitedURLsContentHash;   // key-> hash , value-> URL
    private ConcurrentLinkedQueue<String> DisallowedURLs;
    final int MAX_VALUE = 200;

    public Crawler(String BaseUrl) throws MalformedURLException, InterruptedException {
        CrawledNum=new AtomicInteger(0);
        this.BaseURL = BaseUrl;
        URLsToCrawl = new ConcurrentLinkedQueue<>();
        DisallowedURLs=new ConcurrentLinkedQueue <>();
        inLinks =new ConcurrentHashMap <>();
        outLinks =new ConcurrentHashMap <>();
        VisitedURLsContentHash=new ConcurrentHashMap <>();
//        URLsToCrawl.add("https://www.bbc.com");
        URLsToCrawl.add(BaseURL);
        String Hash=getContentHashFromURL(BaseURL);
        VisitedURLsContentHash.put(Hash,BaseUrl);
//        Hash= getContentHashFromURL("https://www.bbc.com");
//        VisitedURLsContentHash.put(Hash,"https://www.bbc.com");
    }

    public void run() {
        while(CrawledNum.get()<MAX_VALUE)
        {
            String head=URLsToCrawl.poll();
            if(head!=null)
            {
                CrawledNum.incrementAndGet();
                System.out.println(CrawledNum+" "+Thread.currentThread().getName()+"  "+head+" ");
                outLinks.put(head,new HashSet<>());
                try
                {
                    Document doc = Jsoup.connect(head).get();
                    Elements links = doc.select("a[href]");
                    for(Element link:links)
                    {
                        if(CrawledNum.get()>=MAX_VALUE)
                            return;
                        String LinkURL = link.absUrl("href"); // if link contains the relative URL the abs will get the complete URL
                        String hash=getContentHashFromURL(LinkURL); // hash of the content of the URL
                        String HashedURL=VisitedURLsContentHash.get(hash); // the URL that has the same hash
                        outLinks.get(head).add(HashedURL);
                        if(HashedURL!=null) // there exists a URL that has this content (or we are trying to crawl a visited URL), I don't need to crawl this URL
                        {
                            if(inLinks.get(HashedURL)!=null)
                                inLinks.get(HashedURL).add(head);
                            else // if the URL was a seed
                            {
                                Set<String> tempSet = new HashSet<>();
                                tempSet.add(head);
                                inLinks.put(HashedURL, tempSet);
                            }
                            continue;
                        }
                        VisitedURLsContentHash.put(hash,LinkURL);
                        GenerateDisAllowedURLs(LinkURL);
                        if(CrawledNum.get()<MAX_VALUE&&LinkURL.startsWith("http")&&(!DisallowedURLs.contains(LinkURL))) // HTTP and HTTPs URLS only
                        {
                            if(inLinks.get(LinkURL)!=null) // this may happen if the content of the URL has changed
                            {
                                // here we need to update the database
                                inLinks.get(LinkURL).add(head);
                            }
                            else
                            {
                                Set<String> tempSet = new HashSet<>();
                                tempSet.add(head);
                                inLinks.put(LinkURL, tempSet);
                            }
                            System.out.println(head+" Found "+Thread.currentThread().getName()+"  "+LinkURL+" ");
                            if(CrawledNum.get()+URLsToCrawl.size()<MAX_VALUE)
                                URLsToCrawl.add(LinkURL);
                        }
                    }
                } catch (IOException | InterruptedException ignored) {}
            }
        }
    }

    public void GenerateDisAllowedURLs(String urlStr){
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
                        DisallowedURLs.add(url.getProtocol() + "://" + url.getHost() + path);
                }
            }
        }
        catch(IOException ignored){}
    }

    private static String getContentHashFromURL(String input) throws InterruptedException, MalformedURLException {
        try {
            // Get an instance of the MD5 hash algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Convert the input string to a byte array and hash it
            byte[] inputBytes = input.getBytes();
            byte[] hashBytes = md.digest(inputBytes);

            // Convert the hash bytes to a hexadecimal string representation
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hashBytes.length; i++) {
                String hex = Integer.toHexString(0xff & hashBytes[i]);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // This should never happen since "MD5" is a standard algorithm
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }


    public static void main(String[] args)throws Exception {
        Crawler crawler =new Crawler("https://www.bbc.com/");
        Thread[] threads = new Thread[8];

        for (int i = 0; i <8; i++)
        {
            threads[i] = new Thread(crawler);
            threads[i].setName(String.valueOf(i));
            threads[i].start();
        }
        for(int i=0;i<8;i++)
            threads[i].join();
//        HashMap<String, Set<String>> inhashMap = new HashMap<>(inLinks);
//        HashMap<String, Set<String>> outhashMap = new HashMap<>(outLinks);
//
//        PageRanker pageRanker = new PageRanker(inhashMap,outhashMap);
//        pageRanker.CalculatePageRanks();
//        pageRanker.IndexPageRankScores();
    }
}