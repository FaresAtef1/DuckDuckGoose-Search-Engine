package engine.app;

import java.io.*;
import java.util.List;

import indexer.Indexer;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import queryprocessor.Query_Processor;

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
        System.out.println("Num of results : "+URLs.size());
        request.setAttribute("results", URLs);
        HttpSession session = request.getSession();
        session.setAttribute("results",URLs);
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