<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="author" content="colorlib.com">
    <link href="https://fonts.googleapis.com/css?family=Poppins:400,800" rel="stylesheet" />
    <link href="css/main.css" rel="stylesheet" />
</head>
<body style="background: azure;">
<div class="s006">
    <form action="app-servlet" method="GET" class="searchform" id="f1">
        <fieldset>
            <img src="css/Search_engine_logo-removebg.png" alt="DOODLE" class="logo">
<%--            <legend>What are you looking for?</legend>--%>
            <div class="inner-form">
                <button class="btn-lucky" id="l1" type="button" name="button"  value="lucky">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                        <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"></path>
                    </svg>
                </button>
                <button class="btn-voice" type="button" name="button" id="v1" value="voice" >
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                        <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"></path>
                    </svg>
                </button>
                <div class="input-field">
                    <button class="btn-search" type="submit" name="button"  value="search">
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                            <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"></path>
                        </svg>
                    </button>
                    <input id="search"  type="text" placeholder="What are you looking for?" name="query" style="background: #333333;color:white"/>
                </div>
            </div>
        </fieldset>
    </form>

</div>
</body>
</html>

<script>
    const submitButton = document.getElementById("l1");
    const voiceButton = document.getElementById("v1");
    submitButton.addEventListener("click", () => {
        submitButton.type= "submit";
    });
    submitButton.type= "button";
    voiceButton.addEventListener("click", () => {
        voiceButton.type= "submit";
        const Input = document.getElementById("search");
        Input.placeholder = "Let's hear you out!...Say Your Query";
    });
    voiceButton.type= "button";
</script>
