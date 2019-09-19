package com.arsoftwares.kubemeter.kubemeteragent;

import java.util.Date;

public class ContainerMetrics {
	
	private String containerName;
	private Date metricTimestamp;
	private long cpu;
	private long memory;
	private long max_cpu;
	private long max_memory;
	
	
	public String getContainerName() {
		return containerName;
	}
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}
	public Date getMetricTimestamp() {
		return metricTimestamp;
	}
	public void setMetricTimestamp(Date metricTimestamp) {
		this.metricTimestamp = metricTimestamp;
	}
	
	public long getCpu() {
		return cpu;
	}
	public void setCpu(long cpu) {
		this.cpu = cpu;
	}
	public long getMemory() {
		return memory;
	}
	public void setMemory(long memory) {
		this.memory = memory;
	}
	public long getMax_cpu() {
		return max_cpu;
	}
	public void setMax_cpu(long max_cpu) {
		this.max_cpu = max_cpu;
	}
	public long getMax_memory() {
		return max_memory;
	}
	public void setMax_memory(long max_memory) {
		this.max_memory = max_memory;
	}
	
	
	
}
