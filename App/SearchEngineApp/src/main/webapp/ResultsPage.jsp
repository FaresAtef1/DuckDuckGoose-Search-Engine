<%@ page import="java.util.List" %><%--
  Created by IntelliJ IDEA.
  User: Amr Elsheshtawy
  Date: 07/05/2023
  Time: 7:01 am
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<String> results = (List<String>) session.getAttribute("results");
%>
<!DOCTYPE html>
<html>
<head>
    <link href="css/ResultsStyle.css" rel="stylesheet" type="text/css"/>
</head>

<% int itemsPerPage = 10; %>
<% int totalItems = results.size(); %>
<% int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage); %>

<% int currentPage = Integer.parseInt(request.getParameter("page")); %>
<% int startIndex = (currentPage - 1) * itemsPerPage; %>
<% int endIndex = Math.min(startIndex + itemsPerPage, totalItems); %>
<% List<String> currentPageResults = results.subList(startIndex, endIndex); %>
<body>
<div class="container">
    <ul class="list">
<% for (String result : currentPageResults) { %>
<li class="item">
    <h2>
        <a href="<%=result%>"><%=result%> </a> </h2>
    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit.
        Donec vel enim ut mauris bibendum bibendum.
        Sed eget ipsum quis ipsum lacinia tincidunt. Nulla facilisi</p>
</li>
<% } %>
    </ul>
</div>
<div class="pagination">
<% for (int i = 1; i <= totalPages; i++) { %>
<a href="ResultsPage.jsp?page=<%=i%>"><%= i %></a>
<% } %>
</div>
</body>
</html>