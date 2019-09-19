<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<link rel="icon"    type="image/png"   href="img/KubeMeter_icon.png">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript" src="js/jquery.min.js"></script>
<link rel="stylesheet" type="text/css" href="css/default.css">

<title>Jmeter on Kubernetese </title>

<script>
	
	$(document).ready(function () {
		document.getElementById("newTest").style.display = "block";
		document.getElementById("tabBtnNewTest").className += " active";
		
	});
	
	
	function showTab(evt, tabName) {
		  var i, tabcontent, tablinks;
		  tabcontent = document.getElementsByClassName("tabcontent");
		  for (i = 0; i < tabcontent.length; i++) {
		    tabcontent[i].style.display = "none";
		  }
		  tablinks = document.getElementsByClassName("tablinks");
		  for (i = 0; i < tablinks.length; i++) {
		    tablinks[i].className = tablinks[i].className.replace(" active", "");
		  }
		  document.getElementById(tabName).style.display = "block";
		  evt.currentTarget.className += " active";
		}

</script>

</head>
<body>
	<%@include file="PageHeader.html" %>
	
	
	<div class="tab">
	  <button id="tabBtnNewTest" class="tablinks" onclick="showTab(event,'newTest')">New Test</button>
	  <button id="tabBtnViewTests" class="tablinks" onclick="showTab(event,'viewTests')">View Tests</button>
	  
	</div>

	<div id="newTest" class="tabcontent">
		<%@include file="NewTest.jsp" %>
	</div>
	
	<div id="viewTests" class="tabcontent">
		 <%@include file="ViewTests.jsp" %> 
	</div>
	
	<div id="viewTestDetails">
		<%@include file="ViewTestDetail.html" %>
	</div>
</body>
</html>