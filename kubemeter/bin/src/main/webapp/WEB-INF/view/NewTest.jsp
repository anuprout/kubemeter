<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Kubemeter home</title>
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" src="js/launchTest.js"></script>
<script type="text/javascript" src="js/testDataFiles.js"></script>


</head>
<body>

	<form id="formLaunchTest" method="post" enctype="multipart/form-data">
		Select Jmeter script(*.jmx) : <input type="file"
			name="jmeterScript" id="jmeterScript" > 
		<br> <br>
		Select Test Data Folder :
		<input id="testDataFolder" type="file" webkitdirectory/>
		<span style="font-weight: bold; ">(Note: The script must have a "User Defined Variable" element with name "TestDataFiles")</span>
		<div>
			<table>
				<tbody id="testDataFiles">

				</tbody>
			</table>

		</div>
		<br>
		Add supporting files(*.jars,*.bsh,*.sh etc) :<br>
		<div id="supportFilesDiv">
			<input type="button" value="Add File"
				onclick="addSupportFile()">
		</div>
		
		<br> <br> Enter Number of Jmeter slaves:<br> <input
			type="text" name="jmeter-slaves-count" value="3" mandatory> <br>
<!-- 		<br> Enter test duration(in sec):<br> <input type="text" -->
<!-- 			name="jmeter-test-duration" value="3600" mandatory> <br> -->
		<br> <input type="submit" value="Launch Test" id="btnLaunchTest">
	</form>

	<div id="launchTestResponse"></div>
	
	<br>



</body>

</html>


