<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head> 
    <title>e-VRE: Meta Data services description page</title> 
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" href="css/style.css"/>
    
</head>
<body>
<div class="colmask fullpage">
	<h2>Welcome to e-VRE Meta Data Services (release <i th:text="${session.release}"></i>)</h2>
	<p >If you can see this page the Meta Data Services have been correctly deployed and it should be possible to use the 
	Meta Data Web Services.
	</p>
	<p>
            The <a href="https://app.swaggerhub.com/apis/rousakis/ld-services/1.0.0" target="_blank"> Web Services documentation</a></p>
        The <a href="doc/index.html"  target="_blank">Javadocs</a>
	
	
</div>
	<div class="footer">
	
	 <hr/>
	<p>  <i>e-VRE</i></p>
	
	 </div>
	 
</body>
</html>
