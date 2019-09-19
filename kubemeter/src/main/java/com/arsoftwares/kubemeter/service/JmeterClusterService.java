package com.arsoftwares.kubemeter.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.arsoftwares.kubemeter.service.pojos.TestCluster;
import com.arsoftwares.kubemeter.service.pojos.TestPod;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wba.jmeteronkube.utils.KubeUtil;

import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.models.V1StatusBuilder;

@Service
public class JmeterClusterService {
	private static final Logger log = LoggerFactory.getLogger(JmeterClusterService.class);
	
	@Value("${kubernetese.namespace:jmeter}")
	private String namespace ;
	
	@Value("${kubemeter.testexecuton.max-testpod-starttime-in-sec:300}")
	private int MAX_TESTPOD_START_TIME_IN_SECONDS;
	
	@Value("${kubemeter.jmeter.root}")
	private String JMETER_ROOT_PATH;
	
	private KubeUtil kubeUtil =null;
	private String jmeter_pod_yaml =null;
	private String kubeConfigFile = "./config/kube_config";
	private String jmeterPODYAMLFile = "./config/jmeter_master_pod.yaml";
		
	public JmeterClusterService() throws Exception {
		kubeUtil = new KubeUtil(new File(kubeConfigFile));
		jmeter_pod_yaml = new String(Files.readAllBytes(Paths.get(jmeterPODYAMLFile)));
		
	}
	
	/**
	 * 
	 */
	/**
	 * @param slavesCount - number of Jmeter slave pods to be created
	 * @throws Exception 
	 */
	public TestCluster createCluster(int podsCount, String testID) throws Exception {
		TestCluster testCluster = new TestCluster();
		testCluster.setTestId(testID);
		
		//create the namespace if not exist
		{
			if(!kubeUtil.getNameSpaces().contains(namespace)) {
				kubeUtil.createNamespace(namespace).toString();
				log.info("created namespace '"+namespace+"'.");
			}
			else {
				log.info("namespace '"+namespace+"' found.");
			}
		}
		
		
		//create jmeter pods
		
		List<TestPod> podsList = new ArrayList<TestPod>();
		for(int i=0;i<podsCount;i++)
		{	
			TestPod testPod = createNewPODForTest(testID);
			if(testPod != null) testCluster.getPodsList().add(testPod);
			
		}
				
		return testCluster;
	}
	
	public TestPod createNewPODForTest(String testId) {
		
		try {
			String podName = "jmeter-"+testId+"-"+System.currentTimeMillis();
			V1Pod pod = kubeUtil.createPod(namespace,jmeter_pod_yaml,podName,testId);
			
			log.info("POD {} created for test with id {}",podName,testId);
			
			return new TestPod(
					pod.getMetadata().getName(),
					pod.getStatus().getPhase(),
					0,
					0.0
					) ;
			
		}catch(Exception e) {
			log.error("Failed to create a new test POD for test with id {}",testId,e);
			return null;
		}
	}
	
	public V1Status stopTestOnPod(String testId, String podName) {
		try {
			return kubeUtil.deletePOD(namespace, podName);
		}catch(Exception e) {
			log.error("Failed to stop the pod {}",podName,e);
			return new V1StatusBuilder()
					.build()
					.apiVersion("v1")
					.kind("Status")
					.status("Error")
					.message(e.getMessage())
					.code(400)
					;
		}
		
	}
	
	public TestCluster getCluster(String testId) {
		TestCluster cluster = new TestCluster();
		
		try {
			
			cluster.setTestId(testId);
			
			HashMap<String, String> labels = new HashMap<String,String>();
			labels.put("testid", testId);
			V1PodList podList = kubeUtil.searchPodsByLabel(namespace, labels);
			
			for(V1Pod pod: podList.getItems()) {
				cluster.getPodsList().add( 
						new TestPod(
									pod.getMetadata().getName(), 
									pod.getStatus().getPhase(), 
									0, 
									0
									)
						);
			}
		}catch(Exception e) {
			log.error("Error in getting the PODs details from Kubenetes cluster for test id {}",testId,e);
		}
		
		return cluster;
	}
	
	
	public void run_Test_On_A_New_Pod(String testId, String testScript, MultipartFile[] testDataFiles, MultipartFile[] supportFiles) {
		//create a new test POD
		TestPod testPod = createNewPODForTest(testId);
		if(testPod != null) {
			
			String podName = testPod.getPodName();
			ExecutorService executor = Executors.newFixedThreadPool(
					testDataFiles.length+supportFiles.length+1,
					new ThreadFactoryBuilder()
						.setNameFormat("fileUpload-"+podName+"-thread-%d")
						.setPriority(Thread.MAX_PRIORITY)
						.build()
					);
			List<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();
			
			try {
				
				//check if the POD is initialized
				long t1 = System.currentTimeMillis();
				while( !(kubeUtil.getPodByName(namespace, podName).getStatus().getPhase().equalsIgnoreCase("RUNNING"))) {
					
					if((System.currentTimeMillis() - t1)/1000 > MAX_TESTPOD_START_TIME_IN_SECONDS) {
						log.warn("POD {} did not initialize within the max time limit {} seconds. So deleting it.",podName,MAX_TESTPOD_START_TIME_IN_SECONDS);
						stopTestOnPod(testId, podName);
						return ;
					}
						
					Thread.sleep(1000);
				}
				
				
				//copy script to the POD
				futureList.add(executor.submit(new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						try {
							kubeUtil.copyFileToPod(namespace, podName, testScript.getBytes(), "testscript.jmx");
							log.info("Successfully copied the test script to pod {}.",podName);
							return true;
						}catch(Exception e) {
							log.error("Failed to copy the test script to pod {}.",podName);
							throw e;
							
						}
					}
				}));
				
				//copy test data files to the POD
				for(MultipartFile testDataFile:testDataFiles) {
					
					futureList.add(executor.submit(new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							try {
								kubeUtil.copyMultipartFileToPod(namespace, podName, "", testDataFile);
								log.info("Successfully copied file {} to pod {}.",testDataFile.getOriginalFilename(),podName);
								return true;
							}catch(Exception e) {
								log.error("Failed to copy file {} to pod {}.",testDataFile.getOriginalFilename(),podName);
								throw e;
								
							}
						 
						}
					}));
				}
				
				//copy support files to the POD
				for(MultipartFile supportFile:supportFiles) {
					
					futureList.add(executor.submit(new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							try {
								kubeUtil.copyMultipartFileToPod(namespace, podName, "", supportFile);
								log.info("Successfully copied file {} to pod {}.",supportFile.getOriginalFilename(),podName);
								return true;
							}catch(Exception e) {
								log.error("Failed to copy file {} to pod {}.",supportFile.getOriginalFilename(),podName);
								throw e;
								
							}
						 
						}
					}));
				}
				
				for(Future<Boolean> future:futureList) {
					future.get(MAX_TESTPOD_START_TIME_IN_SECONDS, TimeUnit.SECONDS);
				}
				
				kubeUtil.executeCommandInPOD(namespace, podName, new String[] {
						"sh",
						JMETER_ROOT_PATH+"/bin/jmeter.sh",
						"-n",
						"-t","testscript.jmx",
						"-l","testresult.jtl",
						"-j","jmeter.log"
						});
				
				log.info("Succesfully started test on POD {}",podName);
				
			}catch(Exception e) {
				log.error("Failed to start test on POD {}. Deleting  it ..",podName,e);
				stopTestOnPod(testId, podName);
				
			}finally {
				executor.shutdown();
			}
		}
		
	}
	
	public List<InputStream> getTestResultsFromPods(String testId) throws Exception{
		List<InputStream> results = new ArrayList<InputStream>();
		
		HashMap<String, String> labels = new HashMap<String,String>();
		labels.put("testid", testId);
		V1PodList testPods = kubeUtil.searchPodsByLabel(namespace, labels);
		for( V1Pod testPod:testPods.getItems()) {
			results.add(kubeUtil.copyFileFromPod(namespace, testPod.getMetadata().getName(), "testresult.jtl"));
		}
		
		return results;
		
	}
	
	public ByteArrayOutputStream zipTestResultsFromPods(String testId) throws Exception{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		
		HashMap<String, String> labels = new HashMap<String,String>();
		labels.put("testid", testId);
		V1PodList testPods = kubeUtil.searchPodsByLabel(namespace, labels);
		for( V1Pod testPod:testPods.getItems()) {
			InputStream inputStream = kubeUtil.copyFileFromPod(namespace, testPod.getMetadata().getName(), "testresult.jtl");
			zos.putNextEntry(new ZipEntry("pod-"+testPod.getMetadata().getName()+".jtl"));
			IOUtils.copy(inputStream, zos);
			zos.closeEntry();
			inputStream.close();
		}
		
		zos.close();
		baos.close();
		
		return baos;
		
	}
}
