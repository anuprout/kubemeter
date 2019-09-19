package com.wba.jmeteronkube.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.arsoftwares.kubemeter.controller.JmeterClusterController;
import com.wba.jmeteronkube.dao.model.TestExecution;
import com.wba.jmeteronkube.dao.model.TestExecutionRowMapper;

@Component
public class TestExecutionRepository {
	private static final Logger log = LoggerFactory.getLogger(TestExecutionRepository.class);
	
	private String TEST_EXECUTION_TABLE = "test_executions";
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	
	public TestExecution createTestExecution(String testName) {
		Date startTime = new Date(System.currentTimeMillis());
		String testId = UUID.randomUUID().toString();
		
		try {
			jdbcTemplate.update("INSERT INTO "+TEST_EXECUTION_TABLE+" (testid, testname , starttime, teststatus) VALUES (?,?,?,?)" ,
						testId,
						testName,
						startTime,
						TestExecution.STATUS_INITIALIZING
						);
				
			
			return new TestExecution(testId, testName, TestExecution.STATUS_INITIALIZING, startTime, null);
			
		}catch(Exception e) {
			log.error("Error in creating a new test execution in database with test name {}",testName,e);
			return null;
		}
	}
	
	public void updateTestExecutionStatus(String testID, String status) {
		try {
			int res = 0;
			if(status.equalsIgnoreCase(TestExecution.STATUS_INPROGRESS)) {
				res = jdbcTemplate.update("UPDATE "+TEST_EXECUTION_TABLE+" SET teststatus=? where testid=? " ,
						status,
						testID
						);
			}else {
				res = jdbcTemplate.update("UPDATE "+TEST_EXECUTION_TABLE+" SET teststatus=? AND endtime=? WHERE testid=? " ,
						status,
						new Date(System.currentTimeMillis()),
						testID
						);
			}
			 
			if(res == 0) {
				log.warn("Could not update the test status for test id {}. No test exists with this ID.",testID);
			}
		}catch(Exception e) {
			log.error("Error in updating test status with id {}",testID,e);
		}
	}
	
		
	public TestExecution getTestExecution(String testId) {
		try {
			List<TestExecution> testExecutionsList = jdbcTemplate.query("SELECT * FROM "+TEST_EXECUTION_TABLE+" WHERE testid=? " ,
					new Object[] {	
									testId
								},
					new int[] {
							Types.VARCHAR
							
					},
					new TestExecutionRowMapper()
					);
			
			return testExecutionsList.size()==0?null:testExecutionsList.get(0);
		}catch(Exception e) {
			log.error("Erroe is fetching test execution deatil with test id {}",testId,e);
			return null;
		}
	}
	
	public List<TestExecution> searchTestExecutions(String testName,String testStatus, Timestamp startTime, Timestamp endTime) {
		try {
			List<TestExecution> testExecutionsList;
			if(testStatus.equalsIgnoreCase("ALL")) {
				testExecutionsList = jdbcTemplate.query("SELECT * FROM "+TEST_EXECUTION_TABLE+" where testname like ? AND  starttime >=? AND starttime<=?" ,
						new Object[] {	"%"+testName+"%",
										startTime,
										endTime
									},
						new int[] {
								Types.VARCHAR,
								Types.TIMESTAMP,
								Types.TIMESTAMP
						},
						new TestExecutionRowMapper()
						);
			}else {
				testExecutionsList = jdbcTemplate.query("SELECT * FROM "+TEST_EXECUTION_TABLE+" where testname like ? AND  starttime >=? AND starttime<=? AND teststatus =?" ,
						new Object[] {	"%"+testName+"%",
										startTime,
										endTime,
										testStatus
									},
						new int[] {
								Types.VARCHAR,
								Types.TIMESTAMP,
								Types.TIMESTAMP,
								Types.VARCHAR
						},
						new TestExecutionRowMapper()
						);
			}
			
			
			return testExecutionsList;
		}catch(Exception e) {
			log.error("Error in fetching test executions with testname like {} AND  starttime >={} AND starttime<={} AND teststatus={}",
					testName,
					startTime,
					startTime,
					testStatus,
					e
					);
			return null;
		}
		
	}
	
	
}
