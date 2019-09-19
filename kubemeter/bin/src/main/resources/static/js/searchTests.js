	$(document).ready(function () {

	    $("#btnSearchTests").click(function (event) {
	    	//stop submit the form, we will post it manually.
	        event.preventDefault();

	        fire_ajax_searchTests();

	    });

	});

	function fire_ajax_searchTests() {

	    var data = $('#formSearchTests').serialize(); //build query string from the form inputs

	    $("#btnSearchTests").prop("disabled", true);

	    $.ajax({
	        type: "GET",
	        url: "/searchtests",
	        data: data,
	        cache: false,
	        timeout: 600000,
	        success: function (data,status,request) {
	        	
	        	$("#btnSearchTests").prop("disabled", false);
	        	showTestExecutions(request.responseText);

	        },
	        error: function (e) {
	        	
	        	$("#btnSearchTests").prop("disabled", false);

	        }
	    });

	}
	
	function showTestExecutions(testExecutions) {
		var jsonObj = JSON.parse(testExecutions);
		
		var tableData = "";
		
		for(i in jsonObj){
			tableData += "<tr>"+
						"<td>"+jsonObj[i].testName+"</td>"+
						"<td>"+jsonObj[i].testStatus+"</td>"+
						"<td>"+jsonObj[i].startTime+"</td>"+
						"<td>"+jsonObj[i].endTime+"</td>"+
						"<td hidden id='testId'>"+jsonObj[i].testId+"</td>"+
						"</tr>"
		}
		
		$("#tableTestExecutions").html(tableData);
	}
	
	$(document).on("click","#tableTestExecutions tr", function (){
			$(this).addClass('selected').siblings().removeClass('selected');    
		   var testId=$(this).find('#testId').html();
		   fire_ajax_getTestDetail(testId);
	});