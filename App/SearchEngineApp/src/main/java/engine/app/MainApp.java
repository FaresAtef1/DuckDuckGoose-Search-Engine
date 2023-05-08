package engine.app;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import queryprocessor.Query_Processor;
import structures.pair;
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
        Query_Processor queryProcessor = new Query_Processor();
        List<String> URLs = queryProcessor.RetrieveResults(query);
        List<String> titles = new ArrayList<>();
        List<String> paragraphs = new ArrayList<>();
        if(URLs==null)
            URLs= new ArrayList<String>() ;
        else
        {
            paragraphs=WebpageParagraphScraper.Scraper(URLs,query,titles);
        }
        System.out.println("Num of results : "+URLs.size());
        request.setAttribute("results", URLs);
        request.setAttribute("titles", titles);
        request.setAttribute("paragraphs", paragraphs);
        HttpSession session = request.getSession();
        session.setAttribute("results",URLs);
        session.setAttribute("titles",titles);
        session.setAttribute("paragraphs",paragraphs);
//        RequestDispatcher  dispatcher = request.getRequestDispatcher("ResultsPage.jsp?page=1");
//        try {
//            dispatcher.forward(request, response);
//        }
//        catch (Exception e)
//        {
//           e.printStackTrace();
//        }
        response.sendRedirect("ResultsPage.jsp?page=1");
    }
    public void destroy() {
    }
}