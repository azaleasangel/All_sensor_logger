package com.example.paul.all_sensor_logger.views;

public class ModelObject {
	private String deviceName;
	private String macAddress;
	private String pair;
	private String sampling;
	
	public ModelObject(String name, String addr, String pair, String sampling) {
		this.deviceName = name;
		this.macAddress = addr;
		this.pair = pair;
		this.sampling = sampling;
	}
	
	public void setName(String name) {
		this.deviceName = name;
	}
	
	public void setAddress(String addr) {
		this.macAddress = addr;
	}
	
	public void setSampling(float sampling) {
		this.sampling = String.valueOf(sampling);
	}
		
	
	public void setPair(boolean isPaired) {
		this.pair = String.valueOf(isPaired);
	}
	
	public String getName() {
		return this.deviceName;
	}
	
	public String getAddress() {
		return this.macAddress;
	}
	
	public String getSampling() {
		return this.sampling;
	}
		
	public String getPair() {
		return this.pair;
	}
}
