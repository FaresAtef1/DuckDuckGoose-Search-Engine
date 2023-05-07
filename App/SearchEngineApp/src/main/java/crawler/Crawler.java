package crawler;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

import database.Mongo;
import indexer.Indexer;
import inverted_files.InvertedFileBuilder;
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
    private AtomicBoolean isPaused; // Thread-safe boolean
    private ConcurrentLinkedQueue<String> URLsToCrawl;
    private  ConcurrentHashMap<String, Set<String>> outLinks;
    private ConcurrentHashMap<String,String> VisitedURLsContentHash;   // key-> hash , value-> URL
    private ConcurrentLinkedQueue<String> DisallowedURLs;

    private  List<String> extensions =  Arrays.asList(".gif",".gifv",".mp4",".webm",".mkv",".flv",".vob",".ogv",".ogg",".avi",".mts",".m2ts",".ts",".mov",".qt",".wmv",".yuv",".rm",".rmvb",".asf",".amv",".m4p",".m4v",".mpg",".mp2",".mpeg",".mpe",".mpv",".m2v",".m4v",".svi",".3gp",".3g2",".mxf",".roq",".nsv",".f4v",".png",".jpg",".webp",".tiff",".psd",".raw",".bmp",".heif",".indd",".jp2",".svg",".ai",".eps",".pdf",".ppt");

    private static Mongo dbMan ;
    final int MAX_VALUE = 400;

    private static final int StateSize = 25;

    public Crawler(String BaseUrl) throws MalformedURLException, InterruptedException {
        isPaused=new AtomicBoolean(false); // initially crawler is not paused
        CrawledNum=new AtomicInteger(0);
        this.BaseURL = BaseUrl;
        URLsToCrawl = new ConcurrentLinkedQueue<>();
        DisallowedURLs=new ConcurrentLinkedQueue <>();
        outLinks =new ConcurrentHashMap <>();
        VisitedURLsContentHash=new ConcurrentHashMap <>();
//        URLsToCrawl.add("https://www.bbc.com");
//        URLsToCrawl.add(BaseURL);
//        String Hash=getContentHashFromURL(BaseURL);
//        VisitedURLsContentHash.put(Hash,BaseUrl);
//        Hash= getContentHashFromURL("https://www.bbc.com");
//        VisitedURLsContentHash.put(Hash,"https://www.bbc.com");
        dbMan=new Mongo();
        dbMan.LoadPrevState(URLsToCrawl,outLinks,VisitedURLsContentHash,DisallowedURLs);
        CrawledNum.set(outLinks.size());
    }


    public void run() {
        while(CrawledNum.get()<MAX_VALUE)
        {

            String head=URLsToCrawl.poll();
            if(head!=null)
            {
                checkIfInterrupted();
                CrawledNum.incrementAndGet();
                System.out.println(CrawledNum+" "+Thread.currentThread().getName()+"  "+head+" ");
                outLinks.put(head,new HashSet<>());
                try
                {
                    Document doc = Jsoup.connect(head).get();
                    Elements links = doc.select("a[href]");
                    label1:
                    for(Element link:links)
                    {

                        checkIfInterrupted();
                        if(CrawledNum.get()>=MAX_VALUE)
                            return;
                        String LinkURL = link.absUrl("href"); // if link contains the relative URL the abs will get the complete URL
                        for(String ext:extensions) //chcecking if the URL is a media file
                        {
                            if(LinkURL.endsWith(ext))
                                continue label1;
                        }
                        String hash=getContentHashFromURL(LinkURL); // hash of the content of the URL
                        String HashedURL=VisitedURLsContentHash.get(hash);
                        // the URL that has the same hash
                        if(HashedURL!=null) // there exists a URL that has this content (or we are trying to crawl a visited URL), I don't need to crawl this URL
                        {
//                            if(outLinks.get(head)==null)
//                                 outLinks.put(head,new HashSet<>());
                            outLinks.get(head).add(HashedURL);

                            continue;
                        }
                        VisitedURLsContentHash.put(hash,LinkURL);
                        GenerateDisAllowedURLs(LinkURL);
                        if(CrawledNum.get()<MAX_VALUE&&LinkURL.startsWith("http")&&(!DisallowedURLs.contains(LinkURL))) // HTTP and HTTPs URLS only
                        {
                            System.out.println(head+" Found "+Thread.currentThread().getName()+"  "+LinkURL+" ");
                            if(CrawledNum.get()+URLsToCrawl.size()<MAX_VALUE)
                                URLsToCrawl.add(LinkURL);
                        }
                    }

                    if(CrawledNum.get()!=0&&CrawledNum.get()%StateSize==0&&Thread.currentThread().getName().equals("0"))
                    {
                        isPaused.set(true);
                        dbMan.SaveCrawlerState(URLsToCrawl,outLinks,VisitedURLsContentHash,DisallowedURLs);
                        isPaused.set(false);

                    }
                } catch (IOException | InterruptedException ignored) {}
            }
        }
    }

    private void checkIfInterrupted()   {
        while (isPaused.get())
        {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {}
        }
    }

    public void GenerateDisAllowedURLs(String urlStr){
        try{
            URL url = new URL(urlStr);
            String robotsUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";
            Scanner scanner = new Scanner(new URL(robotsUrl).openStream());
            String userAgent = "User-agent: *";
            boolean matched = false;
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                if (line.startsWith("User-agent"))
                    matched = line.equals(userAgent);
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

    public  void PrintEverything()
    {
        System.out.println("URLsToCrawl "+URLsToCrawl.size());
        System.out.println("outLinks "+outLinks.size());
        System.out.println("VisitedURLsContentHash "+VisitedURLsContentHash.size());
        System.out.println("DisallowedURLs "+DisallowedURLs.size());
    }


    public static void main(String[] args)throws Exception {
        Crawler crawler =new Crawler("https://www.bbc.com/");
        Thread[] threads = new Thread[8];
        for (int i = 0; i <4; i++)
        {
            threads[i] = new Thread(crawler);
            threads[i].setName(String.valueOf(i));
            threads[i].start();
        }
        for(int i=0;i<4;i++)
            threads[i].join();

        InvertedFileBuilder builder=new InvertedFileBuilder(crawler.outLinks.keySet());
        builder.Build();
        PageRanker pageRanker = new PageRanker(crawler.outLinks);
        pageRanker.CalculatePageRanks();
        pageRanker.IndexPageRankScores();
        Mongo dbMan=new Mongo();
        dbMan.closeConnection();
    }
}