package com.example.paul.all_sensor_logger.bt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.example.paul.all_sensor_logger.bt.devices.GX3Handler;
import com.example.paul.all_sensor_logger.bt.devices.LeicaHandler;
import com.example.paul.all_sensor_logger.bt.devices.Location;
import com.example.paul.all_sensor_logger.bt.devices.LocationListener;
import com.example.paul.all_sensor_logger.bt.devices.SensorEvent;
import com.example.paul.all_sensor_logger.bt.devices.SensorEventListener;

public class BTSerialPortCommunicationService extends Service {
	private final static String TAG = BTSerialPortCommunicationService.class.getName();
    
	private final IBinder mBinder = new LocalBinder();
	
	private BluetoothAdapter mBluetoothAdapter = null;
    private final List<BluetoothDevice> mBoundedDevices = new ArrayList<BluetoothDevice>();
	private static final UUID SERIAL_PORT_SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	public final static String ACTION_ACCEROMETER_DATA = "ACTION_ACCEROMETER_DATA";
	public final static String ACTION_MAGNETIC_FIELD_DATA = "ACTION_MAGNETIC_FIELD_DATA";
	public final static String ACTION_GYROSCOPE_DATA = "ACTION_GYROSCOPE_DATA";
	public final static String ACTION_GPS_DATA = "ACTION_GPS_DATA";
    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static String EXTRA_NAME = "EXTRA_NAME";
    
    //map mac address and bluetooth socket/device handler
    private final Map<String, BluetoothSocket> mBluetoothDeviceSocketMap = new HashMap<String, BluetoothSocket>();
	private final Map<String, GX3Handler> gx3Handlers = new HashMap<String, GX3Handler>();
	private final Map<String, LeicaHandler> leicaHandlers = new HashMap<String, LeicaHandler>();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class LocalBinder extends Binder {
        public BTSerialPortCommunicationService getService() {
            return BTSerialPortCommunicationService.this;
        }
    }	
	

	public boolean initialize() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
	}
	
	public void updatBoundedBTDevices() {
		mBoundedDevices.clear();
		mBoundedDevices.addAll(mBluetoothAdapter.getBondedDevices());
	}
	
	public boolean connectToDevice(String address) {
		for (int i=0, size=mBoundedDevices.size(); i<size; i++) {
			if (address.matches(mBoundedDevices.get(i).getAddress())) {
				BluetoothDevice d = mBoundedDevices.get(i);
				if (d.getName().matches("Hotlife") || d.getName().matches("CS2525858")) {
					try {
						BluetoothSocket socket = d.createRfcommSocketToServiceRecord(SERIAL_PORT_SERVICE_UUID);
						socket.connect();
						mBluetoothDeviceSocketMap.put(d.getAddress(), socket);
						if (d.getName().matches("Hotlife")) {
							//pass socket to handler
							Log.d(TAG, "Connecting to GX3.");
							GX3Handler gx3Handler = new GX3Handler();
							gx3Handler.setBTDevice(d);
							gx3Handler.setInputStream(socket.getInputStream());
							gx3Handler.setOutputStream(socket.getOutputStream());
							//gx3Handler.registerSensorEventListener(sensorListener, SensorEvent.TYPE_ACCEROMETER);
							//gx3Handler.registerSensorEventListener(sensorListener, SensorEvent.TYPE_MAGNETIC_FIELD);
							//gx3Handler.registerSensorEventListener(sensorListener, SensorEvent.TYPE_GYROSCOPE);
							gx3Handler.open();
							gx3Handlers.put(d.getAddress(), gx3Handler);
						}
						else if (d.getName().matches("CS2525929")) {
							//pass socket to handler
							Log.d(TAG, "Connecting to LeicaGPS.");
							LeicaHandler leicaHandler = new LeicaHandler();
							leicaHandler.setBTDevice(d);
							leicaHandler.setInputStream(socket.getInputStream());
							leicaHandler.registerLocationListener(locationListener);
							leicaHandler.open();
							leicaHandlers.put(d.getAddress(), leicaHandler);
						}
						return true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				} else {
					return false;
				}
			}
		}
		return false;
	}
	
	public void disconnectDevice(BluetoothDevice btDevice) throws IOException {
		if (btDevice.getName().matches("Hotlife")) {
			//remove handler
			GX3Handler gx3Handler = gx3Handlers.get(btDevice.getAddress());
			gx3Handler.close();
		}
		else if (btDevice.getName().matches("CS2525929")) {
			//remove handler
			LeicaHandler leicaHandler = leicaHandlers.get(btDevice.getAddress());
			leicaHandler.close();
		}
		
		//close socket
		BluetoothSocket socket = mBluetoothDeviceSocketMap.get(btDevice.getAddress());		
		socket.close();
		mBluetoothDeviceSocketMap.remove(btDevice.getAddress());
	}
	
	
	public void disconnect() throws IOException {
        if (mBluetoothAdapter == null || mBluetoothDeviceSocketMap.isEmpty()) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothSocket socket; 
        while(mBluetoothDeviceSocketMap.values().iterator().hasNext()) {
        	socket = mBluetoothDeviceSocketMap.values().iterator().next();
        	disconnectDevice(socket.getRemoteDevice());
        }
        gx3Handlers.clear();
        leicaHandlers.clear();
    }
	
	 public void close() {
	    	if (this.mBoundedDevices.isEmpty()) {
	    		return;
	    	}
	    	this.mBoundedDevices.clear();
	    }
	
	private SensorEventListener sensorListener = new SensorEventListener() {
		@Override
		public void onSensorChange(SensorEvent e) {
			final Intent intent;
			final int eventType = e.type;
			switch (eventType) {
			case SensorEvent.TYPE_ACCEROMETER:
				intent = new Intent(ACTION_ACCEROMETER_DATA);
		        intent.putExtra(EXTRA_NAME, String.valueOf(e.device.getAddress()));
		        intent.putExtra(EXTRA_DATA, e.values);
		        sendBroadcast(intent);
				break;
			case SensorEvent.TYPE_MAGNETIC_FIELD:
				intent = new Intent(ACTION_MAGNETIC_FIELD_DATA);
		        intent.putExtra(EXTRA_NAME, String.valueOf(e.device.getAddress()));
		        intent.putExtra(EXTRA_DATA, e.values);
		        sendBroadcast(intent);
				break;
			case SensorEvent.TYPE_GYROSCOPE:
				intent = new Intent(ACTION_GYROSCOPE_DATA);
		        intent.putExtra(EXTRA_NAME, String.valueOf(e.device.getAddress()));
		        intent.putExtra(EXTRA_DATA, e.values);
		        sendBroadcast(intent);
				break;
			}
			
		}
		
	};
	
	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			final Intent intent = new Intent(ACTION_GPS_DATA);
			final double dValues[] = new double[3];
			final float fValues[] = new float[2];
			final boolean bValues[] = new boolean[3];
			
			dValues[0] = location.getLatitude();
			dValues[1] = location.getLongitude();
			if ((dValues[2]=location.getAltitude()) == 0.0) {
				bValues[0] = false;
			}
			if ((fValues[0]=location.getSpeed()) == 0.0) {
				bValues[1] = false;
			}
			
			if ((fValues[1]=location.getBearing()) == 0.0) {
				bValues[2] = false;
			}
			
	        intent.putExtra(EXTRA_NAME, String.valueOf(location.getBTDevice().getAddress()));
	        intent.putExtra(EXTRA_DATA, location.getTime());
	        intent.putExtra(EXTRA_DATA, dValues);
	        intent.putExtra(EXTRA_DATA, fValues);
	        intent.putExtra(EXTRA_DATA, bValues);
	        sendBroadcast(intent);
			
		}
		
	};

}
