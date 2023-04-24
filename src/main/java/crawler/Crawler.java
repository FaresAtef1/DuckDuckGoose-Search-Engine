package crawler;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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

public class Crawler implements Runnable{
    private String BaseURL;  // it is not used
    private AtomicInteger CrawledNum; // Thread-safe counter
    private ConcurrentLinkedQueue<String> URLsToCrawl;
    public Set<String> VisitedURLs; // global for indexer
    protected ConcurrentHashMap<String, Boolean> DisallowedURLs;

    public Crawler(String BaseUrl) {
        CrawledNum=new AtomicInteger(0);
        this.BaseURL = BaseUrl;
        URLsToCrawl = new ConcurrentLinkedQueue<>();
        VisitedURLs = Collections.synchronizedSet(new HashSet<>());  // to ensure that the visitedUrls HashSet is thread-safe
        DisallowedURLs=new ConcurrentHashMap <>();
        URLsToCrawl.add(BaseUrl);
    }

    public void run() {
        while(CrawledNum.get()<6000)
        {
            String head=URLsToCrawl.poll();
            if(VisitedURLs.contains(head))
                continue;
            VisitedURLs.add(head);
            if(head!=null)
            {
                try
                {
                    Document doc = Jsoup.connect(head).get();
                    Elements links = doc.select("a[href]");
                    for(Element link:links)
                    {
                        String LinkURL = link.absUrl("href"); // if link contains the relative URL the abs will get the complete URL
                        URL URLObj = new URL(LinkURL);
                        String CompactURL = URLObj.getProtocol() + "://" + URLObj.getHost() + URLObj.getPath();
                        RobotsTxtChecker checkrobot=new RobotsTxtChecker(CompactURL);
                        checkrobot.Generate();
                        if(CrawledNum.get()<6000&&CompactURL.startsWith("http")&&!VisitedURLs.contains(CompactURL)&&(DisallowedURLs.get(CompactURL)==null||!DisallowedURLs.get(CompactURL))) // we may need this &&CompactURL.startsWith("http")
                        {
                            CrawledNum.incrementAndGet();
                            System.out.println(CrawledNum+" "+Thread.currentThread().getName()+"  "+CompactURL);
                            URLsToCrawl.add(CompactURL);
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
                        DisallowedURLs.put(BaseURL+line,true);
                    }
                }
                reader.close();
            }
            catch (IOException e )
            {}
        }
    }


    public static void main(String[] args)throws Exception {
        Crawler crawler =new Crawler("https://www.york.ac.uk/teaching/cws/wws/webpage1.html");
        Thread[] threads = new Thread[8];

        for (int i = 0; i < 8; i++)
        {
            threads[i] = new Thread(crawler);
            threads[i].setName(String.valueOf(i));
            threads[i].start();
        }
        for(int i=0;i<8;i++)
            threads[i].join();
    }
}