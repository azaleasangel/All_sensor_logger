package com.example.paul.all_sensor_logger.bt.devices;

import android.bluetooth.BluetoothDevice;

public class SensorEvent {
	public static final int TYPE_ACCEROMETER = 1 << 0;
	public static final int TYPE_MAGNETIC_FIELD = 1 << 1;
	public static final int TYPE_GYROSCOPE = 1 << 2;
	public static final int TYPE_GPS = 1 << 3;
	
	public final int type;
	public final BluetoothDevice device;
	public final float values[];
	
	protected SensorEvent(final int type, BluetoothDevice device, int valueSize) {
		this.type = type;
		this.device = device;
		this.values = new float[valueSize];
	}
	
	protected void setValues(float values[]) {
		for (int i=0, size=values.length; i<size; i++) {
			this.values[i] = values[i];
		}
	}
}
