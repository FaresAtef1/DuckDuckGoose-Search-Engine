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
                <div id="logo"><img src="css/logoonly.png" alt="logo" style="height: 50px;width: 50px"></div>
            </a>
            <form action="app-servlet" method="GET" class="searchform">
                <fieldset>
                    <div class="inner-form1" style="height: auto; width: auto">
                        <div class="input-field1" style="border-radius: 10px;">
                            <form onsubmit="event.preventDefault();"
                                  role="search"
                                  style="position: relative; width: 30rem; background: #57bd84; border-radius: 0.7rem;">
                                <label for="search" style="position: absolute; clip: rect(1px, 1px, 1px, 1px); padding: 0; border: 0; height: 1px; width: 1px; overflow: hidden;"></label>
                                <input id="search"
                                       type="search"
                                       placeholder="Search..."
                                       autofocus
                                       required
                                       style="outline: 0; background: #fff; padding: 0 1.6rem; border-radius: 0.7rem; appearance: none; transition: all 0.3s cubic-bezier(0, 0, 0.43, 1.49); transition-property: width, border-radius; z-index: 1; position: relative;  font-family: 'Lato', sans-serif; border: 0; color: #2f2f2f; " value="<%=StringEscapeUtils.escapeHtml4(query)%>" name="query">
                            </form>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </header>
    <div class="body" style="font-family: arial,sans-serif;">
        <div style="margin-bottom: 10px">
            <p>About <%=results.size()%> results (<%=runtime%> seconds)</p>
        </div>
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
<div class="pagination" style="display: flex; align-items: center; justify-content: center; margin-bottom: 10px">
    <% int pageStart = Math.max(currentPage - 5, 1);
        int pageEnd = Math.min(pageStart + 10, totalPages);
        for (int i = pageStart; i <= pageEnd; i++) { %>
    <a href="app-servlet?page=<%=i%>&query=<%=encodedQuery%>" style="margin: 0 5px; text-decoration: none; color: #000; background-color: #f1f1f1; padding: 5px 10px; border-radius: 5px;font-family: arial,sans-serif; "><%=i%></a>
    <% } %>
</div>
</body>
</html>