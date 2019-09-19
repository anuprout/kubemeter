$(document).ready(function () {
	//keep refreshing current test details
	setInterval(function(){
		if(($("#tdTestStatus").html() == "RUNNING")
				|| ($("#tdTestStatus").html() == "INITIALIZING") ){
			
			var testId = $('#tdTestId').html(); 
			fire_ajax_getTestDetail();
		}
	},5000);
		    
	});

function fire_ajax_getTestDetail(testId) {

    

    $.ajax({
        type: "GET",
        url: "/gettest?testId="+testId,
        cache: false,
        timeout: 600000,
        success: function (data,status,request) {
        	viewTestDetail(request.responseText);
        },
        error: function (e) {
        	console.log(e.responseText);
        }
    });

}

function viewTestDetail(testDetail) {
	var jsonObj = JSON.parse(testDetail);
	            //console.log("testid : ", jsonObj.testID);
	            
	            //show test metadata
	            $("#tdTestId").html(jsonObj.testID);
	            if(jsonObj.testExecution!=null){
	            	$("#tdTestName").html(jsonObj.testExecution.testName);
		            $("#tdTestStatus").html(jsonObj.testExecution.testStatus);
		            $("#tdTestStartTime").html(jsonObj.testExecution.startTime);
		            $("#tdTestEndTime").html(jsonObj.testExecution.endTime);
		            
	            }
	            
	            
	            //show test pods
	            $("#tableTestCluster > tbody").html(""); //first empty the table 
          	  	
	            if(jsonObj.testCluster != null){
	            	var tableData = "";
		            for (i in jsonObj.testCluster.podsList) {
		            	
		            	tableData += "<tr>"+
							"<td id='tdPodName'>"+jsonObj.testCluster.podsList[i].podName+"</td>"+
							"<td id='tdPodStatus'>"+jsonObj.testCluster.podsList[i].podStatus+"</td>"+
							"<td id='tdPodUserCount'>"+jsonObj.testCluster.podsList[i].usersCount+"</td>"+
							"<td id='tdPodThrougput'>"+jsonObj.testCluster.podsList[i].throughput+"</td>"+
							"<td><button id='btnRefreshPod'>Refresh</button></td>"+
							"<td><button id='btnStopPod'>Stop</button></td>"+
						"</tr>";
		            	  
		            }
		            $("#tableTestCluster > tbody").html(tableData);
	            }
          	  	
}