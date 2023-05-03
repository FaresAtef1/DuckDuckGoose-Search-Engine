package crawler;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crawler implements Runnable{
    private String BaseURL;  // it is not used
    private AtomicInteger CrawledNum; // Thread-safe counter
    private ConcurrentLinkedQueue<String> URLsToCrawl;
    private ConcurrentHashMap<String,Integer> VisitedURLsCount;
    private ConcurrentHashMap<String,String> VisitedURLsContentHash;   // key-> hash , value-> URL
    private ConcurrentLinkedQueue<String> DisallowedURLs;

    public Crawler(String BaseUrl) {
        CrawledNum=new AtomicInteger(0);
        this.BaseURL = BaseUrl;
        URLsToCrawl = new ConcurrentLinkedQueue<>();
        DisallowedURLs=new ConcurrentLinkedQueue <>();
        VisitedURLsCount =new ConcurrentHashMap <>();
        VisitedURLsContentHash=new ConcurrentHashMap <>();
        URLsToCrawl.add("https://www.example.com/");
        URLsToCrawl.add("https://www.example.com/index.html");
        //URLsToCrawl.add(BaseUrl);
    }

    public void run() {
        while(CrawledNum.get()<6000)
        {
            String head=URLsToCrawl.poll();
            if(head!=null)
            {
                //VisitedURLs.put(head,1);
                try
                {
                    Document doc = Jsoup.connect(head).get();
                    Elements links = doc.select("a[href]");
                    for(Element link:links)
                    {
                        String LinkURL = link.absUrl("href"); // if link contains the relative URL the abs will get the complete URL
                        String hash=getContentHashFromURL(LinkURL); // hash of the content of the URL
                        String HashedURL=VisitedURLsContentHash.get(hash); // the URL that has the same hash
                        if(HashedURL!=null) // there exists a URL that has this content (or we are trying to crawl a visited URL), I don't need to crawl this URL
                        {
                            System.out.println("here");
                            System.out.println(HashedURL);
                            if(VisitedURLsCount.get(HashedURL)!=null) // true if the URL was visited before
                                VisitedURLsCount.put(HashedURL, VisitedURLsCount.get(HashedURL)+1);
                            else // if the URL was not visited before (new URL but has the same content of another visited URL)
                                VisitedURLsCount.put(HashedURL,1);
                            continue;
                        }
                        RobotsTxtChecker checkrobot=new RobotsTxtChecker(LinkURL);
                        checkrobot.Generate();
                        if(CrawledNum.get()<6000&&LinkURL.startsWith("http")&&(!DisallowedURLs.contains(LinkURL))) // HTTP and HTTPs URLS only
                        {
                            VisitedURLsContentHash.put(hash,LinkURL);
                            if(VisitedURLsCount.get(LinkURL)!=null) // this may happen if the content of the URL has changed
                            {
                                // here we need to update the database
                                VisitedURLsCount.put(LinkURL, VisitedURLsCount.get(LinkURL)+1);
                                System.out.println(VisitedURLsCount.get(LinkURL));
                                System.out.println("was visited before "+Thread.currentThread().getName()+"  "+LinkURL);
                                //continue;
                            }
                            else
                                VisitedURLsCount.put(LinkURL,1);
                            CrawledNum.incrementAndGet();
                            System.out.println(CrawledNum+" "+Thread.currentThread().getName()+"  "+LinkURL+" "+ VisitedURLsCount.get(LinkURL));
                            URLsToCrawl.add(LinkURL);
                        }
                    }
                }
                catch (IOException e)
                {}
            }
        }
    }


    public class RobotsTxtChecker{
        private String BaseURL ;
        private URL URL ;
        private URLConnection connection ;

        public RobotsTxtChecker(String BaseUrl) throws IOException {
            this.BaseURL = BaseUrl;
            String suffix=(BaseURL.endsWith("/"))? "robots.txt":"/robots.txt";
            URL = new URL(BaseURL+suffix);
            connection = URL.openConnection();
        }
        public void Generate()
        {
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("Disallow")&&line.length()>12)
                    {
                        line=(BaseURL.endsWith("/"))? line.substring(11):line.substring(10); //remove "Disallow: "
                        //System.out.println(CrawledNum+" "+Thread.currentThread().getName()+"  "+BaseURL+line);
                        DisallowedURLs.add(BaseURL+line);
                    }
                }
                reader.close();
            }
            catch (IOException e )
            {}
        }
    }

    private static String getContentHashFromURL(String urlString) {
        try {
            URL url = new URL(urlString);
            if (!("http".equals(url.getProtocol())||"https".equals(url.getProtocol()))) {
                return "";
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int attempts = 0;
            while (true)
            {
                try
                {
                    connection.connect();
                    break;
                }
                catch (IOException e)
                {
                    if (++attempts > 5)
                    {
                        return "";
                    }
                    // Wait for 1 second before retrying
                    Thread.sleep(10);
                }
            }

            byte[] contentBytes = new byte[0];
            try (InputStream inputStream = connection.getInputStream())
            {
                contentBytes = inputStream.readAllBytes();
            }
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(contentBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        }
        catch (IOException | NoSuchAlgorithmException | InterruptedException e)
        {
            return "";
        }
    }


    public static void main(String[] args)throws Exception {
        Crawler crawler =new Crawler("https://www.york.ac.uk/teaching/cws/wws/webpage1.html");
        Thread[] threads = new Thread[8];

        for (int i = 0; i <8; i++)
        {
            threads[i] = new Thread(crawler);
            threads[i].setName(String.valueOf(i));
            threads[i].start();
        }
        for(int i=0;i<8;i++)
            threads[i].join();
    }
}