package com.arsoftwares.kubemeter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.arsoftwares.kubemeter.dao.TestExecutionRepository;
import com.wba.jmeteronkube.service.JmeterClusterService;

@RestController
public class JmeterClusterController {
	private static final Logger log = LoggerFactory.getLogger(JmeterClusterController.class);
	
	@Autowired
	JmeterClusterService jmeterClusterService;
	
	@Autowired
	TestExecutionRepository testExecutionRepository;
	
	
	
}
