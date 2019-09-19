package com.arsoftwares.kubemeter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.arsoftwares.kubemeter.dao.TestExecutionRepository;
import com.arsoftwares.kubemeter.dao.model.TestExecution;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wba.jmeteronkube.utils.IOUtil;
import com.wba.jmeteronkube.utils.XMLUtil;

@Component
public class TestExecutionService {
	private static final Logger log = LoggerFactory.getLogger(TestExecutionService.class);
	
	@Value("${kubemeter.testexecuton.testpods.max-per-test:50}")
	private int MAX_TESTPODS_FOR_A_TEST;
	
	@Value("${kubemeter.testexecuton.testpods.max-starttime-in-sec:180}")
	private int MAX_TESTPOD_START_TIME_IN_SECONDS;
	
	private ObjectMapper mapper = new ObjectMapper();
	private XPathExpression xPathExpr1 = null;
	private XMLUtil xmlUtil = null;
	
	@Autowired
	private JmeterClusterService jmeterClusterService;

	@Autowired
	TestExecutionRepository testExecutionRepository;
	
	public TestExecutionService() throws Exception {
		xPathExpr1 =  XPathFactory.newInstance().newXPath().compile("//Arguments[@testname=\"TestDataFiles\"]/collectionProp/elementProp");
		xmlUtil = new XMLUtil();
	}
	
	public void startTestOnPODs (
			String testId,
			int podsCount,
			MultipartFile scriptFile,
    		MultipartFile[] testDataFiles,
    		MultipartFile[] supportingFiles,
    		//long testDuration,
    		String testDataFilesMapping
    		) {
		
		ExecutorService executor = Executors.newFixedThreadPool(
				MAX_TESTPODS_FOR_A_TEST,
				new ThreadFactoryBuilder()
					.setNameFormat("starttestonpod-"+testId+"-thread-%d")
					.setPriority(Thread.MAX_PRIORITY)
					.build()
				);
		List<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();
		
		try {
			//modify test script to add mandatory details
			String testScriptStr = refactorTestScript(scriptFile, mapper.readValue(testDataFilesMapping,Map.class));
			
			if(podsCount > MAX_TESTPODS_FOR_A_TEST) podsCount = MAX_TESTPODS_FOR_A_TEST; 			
			
			//create the PODs and start the test on the PODs 
			{
				
					for(int i=0;i<podsCount;i++) {
						
						futureList.add(executor.submit(new Callable<Boolean>() {

							@Override
							public Boolean call() throws Exception {
								jmeterClusterService.run_Test_On_A_New_Pod(
											testId, 
											testScriptStr, 
											testDataFiles,
											supportingFiles);
								return true;
								
							}
						}));
						
						
					}
					
					testExecutionRepository.updateTestExecutionStatus(testId, TestExecution.STATUS_INPROGRESS);
					
					for(Future<Boolean> future:futureList) {
						future.get();
					}
					
					
					
			}
			
		}catch(Exception e) {
			log.error("Error starting the test on the PODs with id {} .", testId);
			testExecutionRepository.updateTestExecutionStatus(testId, TestExecution.STATUS_ERROR);
		}finally {
			executor.shutdown();
			
		}
		
		
	}
	
	public  String refactorTestScript(
			MultipartFile scriptFile, 
			Map<String,String> testDataFilesMapping
			) throws Exception {
		
		Document testScriptDOM = xmlUtil.createDocumentFromString(new String(scriptFile.getBytes()));
		
		testScriptDOM =  replaceTestDataFileNames(testScriptDOM, testDataFilesMapping);
		
		//add default backend listener
		//TODO
		
		
		return xmlUtil.transformDocumentToString(testScriptDOM);
	}
	
	public  Document replaceTestDataFileNames(Document testScriptDOM, Map<String,String> testDataFilesMapping) throws Exception{
	
		NodeList nl = (NodeList) xPathExpr1.evaluate(testScriptDOM, XPathConstants.NODESET);
		
		for(int i=0;i<nl.getLength();i++) {
			Element testDataFileNode = (Element) nl.item(i);
			String testDataFileName = testDataFileNode.getAttribute("name");
			
			if(testDataFilesMapping.containsKey(testDataFileName)) {
				
				NodeList nodeList= testDataFileNode.getElementsByTagName("stringProp");
				for(int j=0;j<nodeList.getLength();j++) {
					if(((Element)nodeList.item(j)).getAttribute("name").equalsIgnoreCase("Argument.value")) {
						nodeList.item(j).setTextContent(testDataFilesMapping.get(testDataFileName));
					}
				}
				
			}
			
			
		}
		
		return testScriptDOM;
	}
	
	
	
	
}
