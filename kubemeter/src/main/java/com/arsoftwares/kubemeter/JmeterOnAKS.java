package com.arsoftwares.kubemeter;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import com.wba.jmeteronkube.utils.KubeUtil;

@SpringBootApplication

public class JmeterOnAKS {

	
	public static void main(String[] args) {
		//System.out.println(System.getProperty("spring.config.location"));
		SpringApplication.run(JmeterOnAKS.class, args);
		
		
	}


	
}
