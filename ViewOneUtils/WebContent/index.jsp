<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>EBLA Services FN Test Connection </title>
<%@ page import="com.ebla.viewone.services.FNConnectionTest" %>
<%@ page import="java.util.List" %>

<h1>EBLA</h1>
<%
List<String> results = FNConnectionTest.TestFnConnection();

%>
</head>
<body>
<%

for(String res : results){
	
%>
	
	<h3><%=res%></h3>
<% 	
}
%>



</body>
</html>