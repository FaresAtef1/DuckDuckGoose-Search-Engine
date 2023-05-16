package engine.app;

import java.io.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import phrase_searching.PhraseSearching;
import queryprocessor.Query_Processor;
import queryprocessor.WebpageParagraphScraper;
import voice.*;

import javax.swing.*;

@WebServlet(name = "appServlet", value = "/app-servlet", loadOnStartup = 1)

public class MainApp extends HttpServlet {
    private  VoiceRecognizer recognizer;
    private List<String> URLs;
    private ConcurrentHashMap<String, Set<Integer>> URLTagIndices;

    private ConcurrentHashMap<String ,Integer> URLs_Snippets;
    private boolean flag; // to prevent calculating the URLs more than once
//    String prev_query;

    public void init() {
        recognizer = new VoiceRecognizer();
        URLs=new ArrayList<>();
        URLTagIndices=new ConcurrentHashMap<>();
        URLs_Snippets=new ConcurrentHashMap<>();
        flag=false;
//        prev_query=null;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        //get the query from the user
        String query = request.getParameter("query");
        System.out.println("query is: "+query);
//        if()
        System.out.println(query);
        int count = 0;
        for(int i=0;i<query.length();i++)
        {
            if(query.charAt(i)=='"')
                count++;
        }
        //check the current page number , 1 by default
        String pagenum= request.getParameter("page");
        //check the button type
        String button_type= request.getParameter("button");

        if(button_type !=null)
        {
            //check if the user clicked on the voice button
            if(button_type.equals("voice"))
            {
                //get the query from the user
                query = recognizer.Recognize();
                System.out.println("query is: "+query);
            }
        }
        if(pagenum==null) {
            pagenum = "1";
            flag=false;
            URLs=new ArrayList<>();
            URLTagIndices=new ConcurrentHashMap<>();
            URLs_Snippets=new ConcurrentHashMap<>();
        }
        int StartTime=(int)System.currentTimeMillis();
        //Create a new query processor object
        Query_Processor queryProcessor = new Query_Processor();
//        ConcurrentHashMap<String, Set<Integer>> URLTagIndices=new ConcurrentHashMap<>();
        System.out.println("flag is: "+flag);
        if(!flag)
        {
            if(count%2==0&&count!=0)
                URLs= PhraseSearching.phraseSearchAndOr(query,URLs_Snippets,count);
            else
                URLs= queryProcessor.RetrieveResults(query,URLTagIndices);
            flag=true;
        }
        List<String> titles = new ArrayList<>();
        List<String> paragraphs = new ArrayList<>();
        //check if no results are found
        if(URLs==null)
            URLs= new ArrayList<String>() ;
        System.out.println("Results are: "+URLs.size());

        if(button_type!=null)
        {
            flag=false;
            //check if the user clicked on the i am feeling lucky button
            if(button_type.equals("lucky"))
            {
                if(URLs.size()>0)
                {
                    System.out.println(button_type);
                    int index;
                    Random random= new Random();
                    if(URLs.size()>4)
                        index=random.nextInt(3);
                    else {
                        index=random.nextInt(URLs.size());
                    }
                    response.sendRedirect(URLs.get(index));
                    return;
                }
                else
                {
                    response.sendRedirect("https://www.facebook.com/");
                    return;
                }
            }
        }
        //Scrape the paragraphs from the webpages
        System.out.println("query is: "+query);
        if(count%2==0&&count!=0)
            paragraphs = WebpageParagraphScraper.ScraperPhraseSearch(URLs, query, titles, URLs_Snippets, Integer.parseInt(pagenum));
        else
            paragraphs = WebpageParagraphScraper.Scraper(URLs, query, titles, URLTagIndices, Integer.parseInt(pagenum));
        //Calculate the time taken to process the query
        System.out.println("paragraphs size is: "+paragraphs.size());
        System.out.println("titles size is: "+titles.size());
        int EndTime=(int)System.currentTimeMillis();
        float Time= (float) ((EndTime-StartTime)/1000.0);
        //Set the attributes of the session
        HttpSession session = request.getSession();
        session.setAttribute("results",URLs);
        session.setAttribute("titles",titles);
        session.setAttribute("paragraphs",paragraphs);
        session.setAttribute("runtime",Time);
        session.setAttribute("query",query);
        response.sendRedirect("ResultsPage.jsp?page="+pagenum);
    }

    public void destroy() {
    }
}