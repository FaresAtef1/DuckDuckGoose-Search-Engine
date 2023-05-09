<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<String> results = (List<String>) session.getAttribute("results");
    List<String> titles = (List<String>) session.getAttribute("titles");
    List<String> paragraphs = (List<String>) session.getAttribute("paragraphs");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="css/search.css">
    <title>Google Search Page</title>
</head>
<% int itemsPerPage = 10; %>
<% int totalItems = results.size(); %>
<% int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage); %>

<% int currentPage = Integer.parseInt(request.getParameter("page")); %>
<% int startIndex = (currentPage - 1) * itemsPerPage; %>
<% int endIndex = Math.min(startIndex + itemsPerPage, totalItems); %>
<% List<String> currentPageResults = results.subList(startIndex, endIndex); %>
<body>
<div class="main">
    <header>
        <div class="top_header">
            <div id="logo"><img src="css/crop.png" alt="logo" style="height: 50px;width: 90px">
            </div>
        </div>
    </header>
    <div class="body" style="font-family: arial,sans-serif;">
        <p>About 77,200,000 results (0.43 seconds)</p>
        <div id="results">
            <% for (int i=0;i<results.size();i++) { %>
            <div class="first_result">
                <div class="text_with_arrow_down">
                    <i class="fas fa-angle-down"></i>
                </div>
                <a href="<%=results.get(i)%>"><h2 style="color: #2323a2;font-size: 20px;"> <%=titles.get(i)%></h2> </a>
                <a href="<%=results.get(i)%>" id="<%=results.get(i)%>" style="color: green;font-size: 16px;"><%=results.get(i)%></a>
                <p style="color: grey;font-size: 16px;"><%=paragraphs.get(i)%></p>
            </div>
            <% } %>
        </div>
    </div>
</div>
<div class="pagination">
    <% for (int i = 1; i <= totalPages; i++) { %>
    <a href="ResultsPage.jsp?page=<%=i%>"><%= i %></a>
    <% } %>
</div>
</body>
</html>