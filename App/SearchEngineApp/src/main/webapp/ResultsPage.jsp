<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<String> results = (List<String>) session.getAttribute("results");
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
            <div id="logo"><img src="google.png" alt="logo">
            </div>
        </div>
    </header>
    <div class="body">
        <p>About 77,200,000 results (0.43 seconds)</p>
        <div id="results">
            <% for (String result : currentPageResults) { %>
            <div class="first_result">
                <div class="text_with_arrow_down">
                    <i class="fas fa-angle-down"></i>
                </div>

                <a href="<%=result%>" id="<%=result%>"> </a>
                <p>TO CREATE A WEBSITE WITH WORDPRESS (OR JOOMLA & DRUPAL), USE ONE CLICK INSTALLATION: Log in to your hosting account. Go to your control panel. Look for the “WordPress” or “Website” icon. Choose the domain where you want to install your website.</p>

                <script>
                    const url = "<%=result%>";
                    fetch(url)
                        .then(response => response.text())
                        .then(data => {
                            const parser = new DOMParser();
                            const doc = parser.parseFromString(data, 'text/html');
                            const title = doc.querySelector('title').innerText;
                            document.getElementById('<%=result%>').textContent = title;
                        })
                        .catch(error => console.error(error));
                </script>

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