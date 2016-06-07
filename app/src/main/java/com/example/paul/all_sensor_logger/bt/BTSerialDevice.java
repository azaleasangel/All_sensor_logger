package com.example.paul.all_sensor_logger.bt;

import android.bluetooth.BluetoothDevice;

public class BTSerialDevice {
	private final String name;
	private final String address;
	private final BluetoothDevice device;
	
	/*private LogFileWriter gpsWriter;
	private LogFileWriter magWriter;
	private LogFileWriter accWriter;
	private LogFileWriter gyroWriter;	 */
	
	private long startTime;
	private int numOfReceived = 0;
	
	public BTSerialDevice(BluetoothDevice device, String name, String address) {
		this.device = device;
		this.name = name;
		this.address = address;
		/*if (name.matches("Hotlife")) {
			this.accWriter  = new LogFileWriter("AccFile"+address+".csv",2);
			this.magWriter  = new LogFileWriter("MagFile"+address+".csv",3);
			this.gyroWriter = new LogFileWriter("GyroFile"+address+".csv",4);
		} else if (name.matches("CS2525929")) {
			this.gpsWriter  = new LogFileWriter("GPSFile"+address+".csv",1);
		}*/
	}
	
	
	/*public LogFileWriter getGPSWriter() {
		return this.gpsWriter;
	}
	
	public LogFileWriter getMagWriter() {
		return this.magWriter;
	}
	
	public LogFileWriter getAccWriter() {
		return this.accWriter;
	}
	
	public LogFileWriter getGyroWriter() {
		return this.gyroWriter;
	}*/
	
	
	public String getName() {
		return this.name;
	}
	
	public String getAddress() {
		return this.address;
	}

	public BluetoothDevice getDevice() {
		return this.device;
	}
	
	public void SetStartTime(long time) {
		this.startTime = time;
	}
	
	public void accumulateReceivedData() {
		this.numOfReceived++;
	}
	
	public float getCurrentSamplingRate() {
		long spentTime = System.currentTimeMillis() - this.startTime;
		long totalSecond  = (int) (spentTime/1000);
		return (float) this.numOfReceived/totalSecond;
	}
}
