package com.arsoftwares.kubemeter.controller;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.arsoftwares.kubemeter.dao.TestExecutionRepository;
import com.arsoftwares.kubemeter.dao.model.TestExecution;
import com.wba.jmeteronkube.service.JmeterClusterService;
import com.wba.jmeteronkube.service.TestExecutionService;
import com.wba.jmeteronkube.service.pojos.Test;

import io.kubernetes.client.models.V1Status;

@Controller
public class UIController {
	private static final Logger log = LoggerFactory.getLogger(UIController.class);
	
	
	@Autowired
	TestExecutionRepository testExecutionRepository;
	
	@Autowired
	JmeterClusterService jmeterClusterService;
	
	@Autowired
	TestExecutionService testExecutionService;
	
	@Autowired
	TestExecutor testExecutor;
	
	@RequestMapping(value="/*", method = RequestMethod.GET)
    public String showHomePage(ModelMap model){
		
		return "HomePage";
    }
	
	 @RequestMapping(value="/searchtests", method = RequestMethod.GET)
	 public ResponseEntity<List<TestExecution>> searchTests(
			 @RequestParam("testName") String testName,
			 @RequestParam("testStatus") String testStatus,
			 @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) DateTime startTime,
			 @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) DateTime endTime
			 
			 ) {
		 
		 try {
			 
			log.info("Searching test executions with parameters testName={}, testStatus={}, startTime={}, endTime={}.",testName,testStatus,startTime,endTime);
			 return new ResponseEntity<List<TestExecution>>(testExecutionRepository.searchTestExecutions(
					 testName, 
					 testStatus, 
					 startTime==null?new Timestamp(System.currentTimeMillis()):new Timestamp(startTime.getMillis()), 
					 endTime==null?new Timestamp(System.currentTimeMillis()):new Timestamp(endTime.getMillis())), 
					 HttpStatus.OK);
		 }catch(Exception e) {
			 log.error("Failed to search the test executions.", e);
			 return new ResponseEntity<List<TestExecution>>(new ArrayList<TestExecution>(),HttpStatus.EXPECTATION_FAILED);
		 }
		 
		 
	 }
	 
	 @RequestMapping(value="/launchtest", method = RequestMethod.POST)
	    public ResponseEntity<Object> launchTest(
	    		@RequestParam("jmeterScript") MultipartFile scriptFile,
	    		@RequestParam("testDataFile") MultipartFile[] testDataFiles,
	    		@RequestParam("supportingFile") MultipartFile[] supportingFiles,
	    		@RequestParam("jmeter-slaves-count") int podsCount,
	    	//	@RequestParam("jmeter-test-duration") long testDuration,
	    		@RequestParam("testDataFilesMapping") String testDataFilesMapping
	    		) {
			
			String testName = "";
			Test test = new Test();
			
			try {
				
				String scriptName = scriptFile.getOriginalFilename();
				if(scriptName.endsWith(".jmx")) {
					testName =  scriptName.substring(0, scriptName.length()-4);
				}else {
					testName = scriptName;
				}
				
				//create an entry in the test execution DB and get a new test ID
				test.setTestExecution(testExecutionRepository.createTestExecution(testName));
				test.setTestID(test.getTestExecution().getTestId());
				
				//create the  cluster of Jmeter PODs
				//test.setTestCluster(jmeterClusterService.createCluster(podsCount,test.getTestID()));
				
				//start the test on the PODs
				testExecutor.execute(new Runnable() {

					@Override
					public void run() {
						
						testExecutionService.startTestOnPODs(
										test.getTestID(), 
										podsCount,
										scriptFile, 
										testDataFiles, 
										supportingFiles, 
									//	testDuration, 
										testDataFilesMapping
								);
						
					}});
				
				log.info("A new test was launched successfully with id "+test.getTestID());
				
			} catch (Exception e) {
				log.error("Failed to launch the test.", e);
				testExecutionRepository.updateTestExecutionStatus(test.getTestID(), TestExecution.STATUS_ERROR);
				return new ResponseEntity<Object>("Failed to launch the test."+e.getMessage(), HttpStatus.EXPECTATION_FAILED);
			} 
			
			return new ResponseEntity<Object>(test,HttpStatus.OK);
	    }
	 
	 @RequestMapping(value="/stoppod", method = RequestMethod.POST)
	    public ResponseEntity<Object> stopTestOnPod(
	    		@RequestBody  Map<String,Object> request
	    		){
			
			String podName = null;
			String testId = null;
			testId = request.get("testId").toString();
			podName = request.get("podName").toString();
			
			V1Status podDeleteStatus = jmeterClusterService.stopTestOnPod(testId, podName);
			return new ResponseEntity<Object>(podDeleteStatus, HttpStatus.valueOf(podDeleteStatus.getCode()));
			
	    }
	 
	 
	 @RequestMapping(value="/gettest", method = RequestMethod.GET)
	 public ResponseEntity<Object> getTestDeatil(
			 @RequestParam("testId") String testId
			 ){
		 
		 try {
			 Test test = new Test();
			 test.setTestID(testId);
			 test.setTestExecution(testExecutionRepository.getTestExecution(testId));
			 test.setTestCluster(jmeterClusterService.getCluster(testId));
			 
			 
			 return new ResponseEntity<Object>(test,HttpStatus.OK);
			 
		 }catch(Exception e) {
			 log.error("Failed to retrive test details for test Id {}",testId,e);
			 return new ResponseEntity<Object>("Failed to retrived test details for Id "+testId,HttpStatus.EXPECTATION_FAILED);
		 }
		 
	 }
	 
	 @RequestMapping(value="/testresult", method = RequestMethod.GET)
	 public ResponseEntity<Object> getTestResult(
			 @RequestParam("testId") String testId
			 ){
		 
		 try {
			 InputStream inputStream = jmeterClusterService.getTestResultsFromPods(testId).get(0);
			 InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

			 HttpHeaders httpHeaders = new HttpHeaders();
			// httpHeaders.setContentDispositionFormData("attachment", "results.jtl");
			 httpHeaders.setContentType(MediaType.TEXT_PLAIN);
			 
			 return new ResponseEntity<Object>(inputStreamResource,httpHeaders,HttpStatus.OK);
			 
		 }catch(Exception e) {
			 log.error("Failed to retrive test result for test Id {}",testId,e);
			 return new ResponseEntity<Object>("Failed to retrive test result for Id "+testId,HttpStatus.EXPECTATION_FAILED);
		 }
		 
	 }
	 
	 @RequestMapping(value="/download/testresult", method = RequestMethod.GET)
	 public ResponseEntity<Object> downloadTestResult(
			 @RequestParam("testId") String testId
			 ){
		 
		 try {
			 
			 ByteArrayResource bar = new ByteArrayResource(jmeterClusterService.zipTestResultsFromPods(testId).toByteArray());
			 
			 HttpHeaders httpHeaders = new HttpHeaders();
			 httpHeaders.setContentDispositionFormData("attachment", "kubemeter_test_results_"+testId+".zip");
			 httpHeaders.setContentType(MediaType.parseMediaType("application/zip"));
			 
			 return new ResponseEntity<Object>(bar,httpHeaders,HttpStatus.OK);
			 
		 }catch(Exception e) {
			 log.error("Failed to retrive test result for test Id {}",testId,e);
			 return new ResponseEntity<Object>("Failed to retrived test result for Id "+testId,HttpStatus.EXPECTATION_FAILED);
		 }
		 
	 }
}
