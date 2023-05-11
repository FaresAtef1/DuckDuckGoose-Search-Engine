package engine.app;

import java.io.*;
import java.util.*;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import queryprocessor.Query_Processor;
import queryprocessor.WebpageParagraphScraper;
@WebServlet(name = "appServlet", value = "/app-servlet")

public class MainApp extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello World!";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        String query = request.getParameter("query");
        String pagenum= request.getParameter("page");
        if(pagenum==null)
            pagenum="1";
        int Pagenum=Integer.parseInt(pagenum);
        System.out.println(pagenum);
        System.out.println(query);
        int StartTime=(int)System.currentTimeMillis();
        Query_Processor queryProcessor = new Query_Processor();
        Map<String, List<Integer>> URLTagIndices=new HashMap<>();
        List<String> URLs = queryProcessor.RetrieveResults(query,URLTagIndices);
        List<String> titles = new ArrayList<>();
        List<String> paragraphs = new ArrayList<>();
        if(URLs==null)
            URLs= new ArrayList<String>() ;
        else
            paragraphs=WebpageParagraphScraper.Scraper(URLs,query,titles,URLTagIndices,Pagenum);
        if(paragraphs==null)
            paragraphs= new ArrayList<String>() ;
//        System.out.println("Num of results : "+URLs.size());
        int EndTime=(int)System.currentTimeMillis();
        float Time= (float) ((EndTime-StartTime)/1000.0);
        request.setAttribute("results", URLs);
        request.setAttribute("titles", titles);
        request.setAttribute("paragraphs", paragraphs);
        request.setAttribute("runtime", Time);
        HttpSession session = request.getSession();
        session.setAttribute("results",URLs);
        session.setAttribute("titles",titles);
        session.setAttribute("paragraphs",paragraphs);
        session.setAttribute("runtime",Time);
        session.setAttribute("query",query);
//        RequestDispatcher  dispatcher = request.getRequestDispatcher("ResultsPage.jsp?page=1");
//        try {
//            dispatcher.forward(request, response);
//        }
//        catch (Exception e)
//        {
//           e.printStackTrace();
//        }
        response.sendRedirect("ResultsPage.jsp?page="+pagenum);

    }
    public void destroy() {
    }
}