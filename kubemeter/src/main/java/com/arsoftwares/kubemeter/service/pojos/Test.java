package com.wba.jmeteronkube.service.pojos;

import com.arsoftwares.kubemeter.dao.model.TestExecution;

public class Test {
	
	private String testId = "";
	public String getTestID() {
		return testId;
	}
	public void setTestID(String testId) {
		this.testId = testId;
	}
	private TestExecution testExecution ;
	private TestCluster testCluster;
	
	public TestExecution getTestExecution() {
		return testExecution;
	}
	public void setTestExecution(TestExecution testExecution) {
		this.testExecution = testExecution;
	}
	public TestCluster getTestCluster() {
		return testCluster;
	}
	public void setTestCluster(TestCluster testCluster) {
		this.testCluster = testCluster;
	}
}
