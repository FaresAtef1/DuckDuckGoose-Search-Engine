package crawler;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import database.Mongo;
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
    private AtomicInteger CrawledNum; // Thread-safe counter
    private ConcurrentLinkedQueue<String> URLsToCrawl;
    private  ConcurrentHashMap<String, Set<String>> outLinks;
    private ConcurrentHashMap<String,String> VisitedURLsContentHash;   // key-> hash , value-> URL
    private ConcurrentLinkedQueue<String> DisallowedURLs;
    private final List<String> extensions =  Arrays.asList(".gif",".gifv",".mp4",".webm",".mkv",".flv",".vob",".ogv",".ogg",".avi",".mts",".m2ts",".ts",".mov",".qt",".wmv",".yuv",".rm",".rmvb",".asf",".amv",".m4p",".m4v",".mpg",".mp2",".mpeg",".mpe",".mpv",".m2v",".m4v",".svi",".3gp",".3g2",".mxf",".roq",".nsv",".f4v",".png",".jpg",".webp",".tiff",".psd",".raw",".bmp",".heif",".indd",".jp2",".svg",".ai",".eps",".pdf",".ppt");

    private static Mongo dbMan ;
    private AtomicInteger MAX_VALUE ;

    public Crawler() throws MalformedURLException, InterruptedException {
        URLsToCrawl = new ConcurrentLinkedQueue<>();
        DisallowedURLs=new ConcurrentLinkedQueue <>();
        outLinks =new ConcurrentHashMap <>();
        VisitedURLsContentHash=new ConcurrentHashMap <>();
        dbMan=new Mongo();
      //  dbMan.LoadPrevState(URLsToCrawl,outLinks,VisitedURLsContentHash);
        MAX_VALUE=new AtomicInteger(6000);
        if(outLinks.isEmpty())
        {
            AddSeeds("https://www.facebook.com");
            AddSeeds("https://www.wikipedia.org");
            AddSeeds("https://www.reddit.com");
            AddSeeds("https://www.nytimes.com");
            AddSeeds("https://www.amazon.com");
            AddSeeds("https://www.imdb.com");
            AddSeeds("https://www.github.com");
            AddSeeds("https://www.stackoverflow.com");
            AddSeeds("https://www.twitter.com");
            AddSeeds("https://www.medium.com");
        }
        CrawledNum=new AtomicInteger(outLinks.size());
    }

    public void AddSeeds(String URL) throws MalformedURLException, InterruptedException {
        this.URLsToCrawl.add(URL);
        String Hash=getContentHashFromURL(URL);
        this.VisitedURLsContentHash.put(Hash,URL);
//        dbMan.AddOneDoc("VisitedURLsContentHash",new org.bson.Document("Hash",Hash).append("URL",URL));
//        dbMan.AddOneDoc("URLsToCrawl",new org.bson.Document("URL",URL));
    }

    public void run() {
        while(CrawledNum.get()<MAX_VALUE.get())
        {
            if(CrawledNum.get()>=MAX_VALUE.get())
                return;
            String head=URLsToCrawl.poll();
            if(head!=null)
            {
             //   dbMan.RemoveOneDoc("URLsToCrawl",new org.bson.Document("URL",head));
                CrawledNum.incrementAndGet();
                System.out.println(CrawledNum+" "+Thread.currentThread().getName()+"  "+head+" ");
                try
                {
                    Document doc = Jsoup.connect(head).get();
                    String lang=doc.select("html").attr("lang");
                    if(!lang.equals("")&&!lang.contains("en")) {
                        MAX_VALUE.incrementAndGet();
                        continue;
                    }
                    outLinks.put(head,new HashSet<>());
                    Elements links = doc.select("a[href]");
                    System.out.println(links.size());
                    label1:
                    for(Element link:links)
                    {
                        String LinkURL = link.absUrl("href"); // if link contains the relative URL the abs will get the complete URL
                        for(String ext:extensions) //checking if the URL is a media file
                        {
                            if(LinkURL.endsWith(ext))
                                continue label1;
                        }
                        if (LinkURL.contains("#"))
                            LinkURL = LinkURL.split("#")[0];
                        if(LinkURL.endsWith("/")||LinkURL.endsWith("#"))
                            LinkURL=LinkURL.substring(0,LinkURL.length()-1);
                        if(LinkURL.endsWith("/#"))
                            LinkURL=LinkURL.substring(0,LinkURL.length()-2);
                        String hash=getContentHashFromURL(LinkURL); // hash of the content of the URL
                        String HashedURL=VisitedURLsContentHash.get(hash);
                        // the URL that has the same hash
                        if(HashedURL!=null) // there exists a URL that has this content (or we are trying to crawl a visited URL), I don't need to crawl this URL
                        {
                            outLinks.get(head).add(HashedURL);
                            continue;
                        }
                        outLinks.get(head).add(LinkURL);
                        VisitedURLsContentHash.put(hash,LinkURL);
                      //  dbMan.AddOneDoc("VisitedURLsContentHash",new org.bson.Document("Hash",hash).append("URL",LinkURL));
//                        GenerateDisallowedURLs(LinkURL);
                        if(CrawledNum.get()<MAX_VALUE.get()&&LinkURL.startsWith("http")) // HTTP and HTTPs URLS only
                        {
                            System.out.println(CrawledNum.get()+" "+head+" Found "+Thread.currentThread().getName()+"  "+LinkURL+" ");
                            if(CrawledNum.get()+URLsToCrawl.size()<MAX_VALUE.get())
                            {
                                URLsToCrawl.add(LinkURL);
                               // dbMan.AddOneDoc("URLsToCrawl",new org.bson.Document("URL",LinkURL));
                            }
                        }
                    }
                   // dbMan.AddOneDoc("outLinks",new org.bson.Document("URL",head).append("outLinks",outLinks.get(head)));
                } catch (IOException | InterruptedException e) {MAX_VALUE.incrementAndGet();}
            }
        }
    }

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

    private static String getContentHashFromURL(String input) throws InterruptedException, MalformedURLException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] inputBytes = input.getBytes();
            byte[] hashBytes = md.digest(inputBytes);
            StringBuilder sb = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
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
        Crawler crawler =new Crawler();
        Thread[] threads = new Thread[12];
        for (int i = 0; i <12; i++)
        {
            threads[i] = new Thread(crawler);
            threads[i].setName(String.valueOf(i));
            threads[i].start();
        }
        for(int i=0;i<12;i++)
            threads[i].join();
        Mongo mon=new Mongo();
        mon.DropCollections();
        InvertedFileBuilder builder=new InvertedFileBuilder(crawler.outLinks.keySet());
        builder.Build();
        PageRanker pageRanker = new PageRanker(crawler.outLinks);
        pageRanker.CalculatePageRanks();
        pageRanker.IndexPageRankScores();
        Mongo dbMan=new Mongo();
        dbMan.closeConnection();
    }
}