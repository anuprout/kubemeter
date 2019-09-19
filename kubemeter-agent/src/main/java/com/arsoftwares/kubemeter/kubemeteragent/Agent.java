package com.arsoftwares.kubemeter.kubemeteragent;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.NewDriver;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agent {
	
	private static final Logger log = LoggerFactory.getLogger(Agent.class);
	
	private static int udp_port ;
    
	private StandardJMeterEngine jmeterEngine = new StandardJMeterEngine();
	private String testScriptFile = "";
	
	public static void main(String[] args) {
		
		//start Jmeter Test
		startTest(args);
		
		//run UDP server 
		udp_port = JMeter.UDP_PORT_DEFAULT-1; 
		
		
	}
	
	public void run() {
		
		DatagramSocket socket = getSocket();
		byte[] buf = new byte[80];
		DatagramPacket request = new DatagramPacket(buf	, buf.length);
		try {
			while(true) {
				socket.receive(request);
				InetAddress address = request.getAddress();
                // Only accept commands from the local host
                if (address.isLoopbackAddress()){
                	String command = new String(request.getData(), request.getOffset(), request.getLength(),"ASCII");
                    System.out.println("Command: "+command+" received from "+address);//NOSONAR
                    log.info("Command: {} received from {}", command, address);
                    switch(command) {
	                    case "STOPTEST" :
                            	stopTestNow();
                            break;
                        case "SHUTDOWN" :
                            shutDownTest();
                            break;
                        case "METRICS" :
                            getTestMetrics();
                            break;
                        default:
                            log.warn("Command: "+command+" not recognised ");//NOSONAR                            
                    }
                }
			}
		}catch(Exception e) {
			log.error("Error in processing UDP request.",e);
		}finally{
			socket.close();
		}
		
	}
	
	public static void startTest(final String[] args) {
		//start Jmeter test
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				NewDriver.main(args);
				
			}
		}).start();
	}

	public void stopTestNow() {
		/** Set to GUI mode even though it is run as non-GUI mode. 
		 *  This is to avoid the agent process to exit after the Jmeter engine is stopped.
		*/
		System.setProperty(JMeter.JMETER_NON_GUI,"false");
		
		//call UDP to stop the test
		
		//exit the agent process
		System.exit(0);
	}
	
	public void shutDownTest() {
		//TODO
	}
	
	public void getTestMetrics() {
		//TODO
	}
	
	
	private static DatagramSocket getSocket() {
        DatagramSocket socket = null;
        int i = port;
        while (i<= maxPort) {
            try {
                socket = new DatagramSocket(i);
                break;
            } catch (SocketException e) { // NOSONAR
                i++;
            }            
        }

        return socket;
    }
	
	
	
}
