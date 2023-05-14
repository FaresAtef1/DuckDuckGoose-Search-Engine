<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<String> results = (List<String>) session.getAttribute("results");
    List<String> titles = (List<String>) session.getAttribute("titles");
    List<String> paragraphs = (List<String>) session.getAttribute("paragraphs");
    String query =(String)session.getAttribute("query");
    String encodedQuery = java.net.URLEncoder.encode(query, StandardCharsets.UTF_8);
    float runtime = (float) session.getAttribute("runtime");
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
<%--<% List<String> currentPageResults = results.subList(startIndex, endIndex); %>--%>
<body>
<div class="main">
    <header>
        <div class="top_header">
            <a href="index.jsp">
                <div id="logo"><img src="css/crop.png" alt="logo" style="height: 50px;width: 90px"></div>
            </a>
            <form action="app-servlet" method="GET" class="searchform">
                <fieldset>
                    <div class="inner-form">
                        <div class="input-field">
                            <button class="btn-search" type="submit">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                                    <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"></path>
                                </svg>
                            </button>
                            <input id="search" type="text" value="<%=StringEscapeUtils.escapeHtml4(query)%>" name="query"/>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </header>
    <div class="body" style="font-family: arial,sans-serif;">
        <p>About <%=results.size()%> results (<%=runtime%> seconds)</p>
        <div id="results">
            <% int k=0;
                for (int i=startIndex;i<endIndex;i++) { %>
            <div class="first_result">
                <div class="text_with_arrow_down">
                    <i class="fas fa-angle-down"></i>
                </div>
                <a href="<%=results.get(i)%>"><h2 style="color: #2323a2;font-size: 20px;"> <%=titles.get(k)%></h2> </a>
                <a href="<%=results.get(i)%>" id="<%=results.get(i)%>" style="color: green;font-size: 16px;"><%=results.get(i)%></a>
                <p style="color: grey;font-size: 16px;"><%=paragraphs.get(k++)%></p>
            </div>
            <% } %>
        </div>
    </div>
</div>
<div class="pagination">
    <% int pageStart = Math.max(currentPage - 5, 1);
        int pageEnd = Math.min(pageStart + 10, totalPages);
        for (int i = 1; i <= totalPages; i++) { %>
    <a href="app-servlet?page=<%=i%>&query=<%=encodedQuery%>"><%=i%></a>
    <% } %>
</div>
</body>
</html>