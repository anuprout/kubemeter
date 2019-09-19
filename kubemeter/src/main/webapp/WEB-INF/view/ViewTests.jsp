<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>View Test Executions History</title>
<link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/timepicker/1.3.5/jquery.timepicker.min.js"></script>
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/timepicker/1.3.5/jquery.timepicker.min.css">
<link rel="stylesheet" type="text/css" href="css/default.css">

<script type="text/javascript" src="js/searchTests.js"></script>

<script type="text/javascript">
$(document).ready(function () {
	$('#startDate').datepicker({dateFormat: "yy-mm-dd"});
	$('#startTime').timepicker();
	$('#endDate').datepicker({dateFormat: "yy-mm-dd"});
	$('#endTime').timepicker();
});
</script>

</head>
<body>

<form id="formSearchTests" >

		<br>
		 Start Time: <input type="text" name="startDate" id="startDate"> <input type="text" name="startTime" id="startTime" value="12:00 AM" size="10"> 
		 &nbsp;End Time: <input type="text"	name="endDate" id="endDate"> <input type="text"	name="endTime" id="endTime" value="12:00 AM" size="10"> 
		 &nbsp;Status: <select name="testStatus">
		 			<option value="ALL">ALL</option>
				    <option value="INITIALIZING">INITIALIZING</option>
				    <option value="RUNNING">RUNNING</option>
				    <option value="COMPLETE">COMPLETE</option>
				    <option value="ERROR">ERROR</option>
				  </select>
  		 &nbsp;Test Name: <input type="text" name="testName"> &nbsp;
		 
		<input type="submit" value="Search" id="btnSearchTests" >
	</form>
	
	<br>
	<div>
		<table class="zebraTable">
			<thead>
				<tr>
					<th>Test Name</th>
					<th>Status</th>
					<th>Start Time</th>
					<th>End Time</th>
				</tr>
			</thead>
			<tbody id="tableTestExecutions">
				
			</tbody>
		</table>
	</div>

</body>
</html>