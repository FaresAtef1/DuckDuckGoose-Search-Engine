package engine.app;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import database.Mongo;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import queryprocessor.Query_Processor;
import queryprocessor.WebpageParagraphScraper;
import voice.*;

@WebServlet(name = "appServlet", value = "/app-servlet", loadOnStartup = 1)

public class MainApp extends HttpServlet {

    private  VoiceRecognizer recognizer;

    public void init() {
        System.out.println("init");
        recognizer=new VoiceRecognizer();
        Mongo dbMan = new Mongo();

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        //get the query from the user
        String query = request.getParameter("query");
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
            }
        }
        if(pagenum==null) {
            pagenum = "1";
        }
        int StartTime=(int)System.currentTimeMillis();
        //Create a new query processor object
        Query_Processor queryProcessor = new Query_Processor();
        Map<String, List<Integer>> URLTagIndices=new HashMap<>();
        List<String> URLs = queryProcessor.RetrieveResults(query,URLTagIndices);
        List<String> titles = new ArrayList<>();
        List<String> paragraphs = new ArrayList<>();
        //check if no results are found
        if(URLs==null)
            URLs= new ArrayList<String>() ;
        if(button_type!=null)
        {
            //check if the user clicked on the i am feeling lucky button
            if(button_type.equals("lucky"))
            {
                if(URLs.size()>0)
                {
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
        paragraphs=WebpageParagraphScraper.Scraper(URLs,query,titles,URLTagIndices,Integer.parseInt(pagenum));
        //Calculate the time taken to process the query
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