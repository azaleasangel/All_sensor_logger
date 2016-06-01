package com.example.paul.all_sensor_logger.bt.devices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class LeicaHandler {
	private static final String TAG = LeicaHandler.class.getName();
	private final static int HOUR = 3600000; //in milliseconds
	private final static int MINUTE = 60000;
	private final static int SECOND = 1000;
	private final static int CENTISECOND = 10;
	private AtomicBoolean isReceived = new AtomicBoolean(false);
	private final List<LocationListener> locationListeners = new ArrayList<LocationListener>();
	private BufferedReader bufferedReader;
	private BluetoothDevice device;
	
	private Location currentLocation = null;
	private Location lastLocation = null;
	
	public LeicaHandler() {
		
	}
	
	public void setInputStream(InputStream is) {
		this.bufferedReader = new BufferedReader(new InputStreamReader(is));
	}
	
	public void setBTDevice(BluetoothDevice d) {
		this.device = d;
	}
	
	public void registerLocationListener(LocationListener listener) {
		this.locationListeners.add(listener);
	}
	
	public void unRegisterLocationListener(LocationListener listener) {
		for (int i=0, size=locationListeners.size(); i<size; i++) {
			LocationListener l = locationListeners.get(i);
			if (l.equals(listener)) {
				locationListeners.remove(i);
				return;
			}
		}
	}
	
	public void open() {
		isReceived.set(true);
		readingDataFromChannel();
	}
	
	public void close() {		
		try {
			isReceived.set(false);
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readingDataFromChannel() {
		new Thread() {
			@Override
			public void run() {
				//read NMEA sentence
				while (isReceived.get()) {
					String line = "";
					String gga_str;
					boolean isReady = false;
					try {
						line = bufferedReader.readLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (line.contains("GGA")) {
						gga_str = line;
						isReady = analyzeGGAString(gga_str);
						if (isReady)
							fireEvent();
					}					
				}
			}
		}.start();
	}
	
	
	private long parseNMEATime(String t) {
		long time = 0L;
		Calendar cal = Calendar.getInstance();
		DateFormat d = new SimpleDateFormat("yyyyMMDD");
		String year = String.valueOf(cal.get(Calendar.YEAR));
		int month = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		String monthStr;		
		String dayStr;
		
		if (month < 10) {
			monthStr = "0"+String.valueOf(month);
		} else {
			monthStr = String.valueOf(month);
		}
		
		if (day < 10) {
			dayStr = "0"+String.valueOf(day);
		} else {
			dayStr = String.valueOf(day);
		}
		
		String date = new StringBuilder("").append(year).append(monthStr).append(dayStr).toString();
		
		
		try {
			time = d.parse(date).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		int hour = Integer.valueOf(t.substring(0, 1));
		int min = Integer.valueOf(t.substring(2, 3));
		int sec = Integer.valueOf(t.substring(4, 5));
		int csec = Integer.valueOf(t.substring(7, 8));
		time += hour * HOUR + min * MINUTE + sec * SECOND + csec * CENTISECOND;
		
		return time;
	}
	
	private boolean analyzeGGAString(String gga_str) {
		//gga_str = "$GPGGA,094534.00,2447.2003797,N,12059.8618025,E,2,07,1.4,92.788,M,17.98,M,02,0014*6C"; //test GGA string
		 
		String[] ggaFields = gga_str.split(",");
		boolean isReady = true;
		long time=0L;
		double lat=0.0, lng=0.0, alt=0.0;		
		
		//ggaFields[0]: $GPGGA
		if (!ggaFields[1].isEmpty()) { 
			time = parseNMEATime(ggaFields[1]);//ggaFields[1]: time
			Log.i(TAG, "time:"+time);
		} else
			isReady = false;
		if (!ggaFields[2].isEmpty()) {
			float tmp = Float.valueOf(ggaFields[2]);//ggaFields[2]: lat
			lat = (int)tmp/100 + (tmp%100)/60;
			//ggaFields[3]: N, S
			if (ggaFields[3].equals("S"))
				lat = lat * -1;
			Log.i(TAG, "lat:"+lat);
		}
		else 
			isReady = false;		
				
		if (!ggaFields[4].isEmpty()) {
			float tmp = Float.valueOf(ggaFields[4]);//ggaFields[4]: lng 
			lng = (int)tmp/100 + (tmp%100)/60;
			//ggaFields[5]: E, W
			if (ggaFields[5].equals("W")) 
				lng = lng * -1;
			Log.i(TAG, "lng:"+lng);
		}
		else 
			isReady = false;
		
		if (isReady && currentLocation == null) {
			currentLocation = new Location(time, lat, lng, device);			
		} else if (isReady) {
			lastLocation = currentLocation;
			currentLocation = new Location(time, lat, lng, device);
			//set speed information
			float distanceAndBearMatrix[] = new float[2]; 
			Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), 
					currentLocation.getLatitude(), currentLocation.getLongitude(), distanceAndBearMatrix);
			currentLocation.setSpeed(distanceAndBearMatrix[0]/(currentLocation.getTime()-lastLocation.getTime())/1000); //in m/s
			currentLocation.setBearing(distanceAndBearMatrix[1]);
		}
		
		if (!ggaFields[12].isEmpty()) {
			float tmp = Float.valueOf(ggaFields[12]);//ggaFields[12]:height of geoid above WGS84 
			alt = tmp;
			Log.i(TAG, "alt:"+alt);
			if (isReady) {
				currentLocation.setAltitude(alt);
			}
		}

		//ggaFields[6]: quality
		/*
		if (!ggaFields[6].isEmpty()) {
			quality = Integer.valueOf(ggaFields[6]).byteValue();//ggaFields[6]: quality
			Log.i("debug", "quality:"+quality);
		}
		else 
			isReady = false;
		*/
		//ggaFields[7]: num_of_sat
		/*
		if (!ggaFields[7].isEmpty()) {
			num_of_sat = Integer.valueOf(ggaFields[7]).byteValue();//ggaFields[7]: num_of_sat
			Log.i("debug", "num_of_sat:"+num_of_sat);
		}
		else 
			isReady = false;
		*/
		//ggaFields[9]: horizontal dilution of position
		//ggaFields[10]:altitude
		//ggaFields[11]:M, (mean)
		//ggaFields[12]:height of geoid above WGS84 
		//ggaFields[13]:M		
		//ggaFields[14]:empty field
		//ggaFields[15]:check sum
		/*
		for (int i=0; i<ggaFields.length; i++) {
			Log.i("debug", "ggaFields["+i+"]="+ggaFields[i]);
		}*/
		return isReady;
	}
	
	private void fireEvent() {		
		for (int i=0, size=locationListeners.size(); i<size; i++) {
			if (locationListeners.get(i) != null) {
				LocationListener l = locationListeners.get(i);
				l.onLocationChanged(currentLocation);
			}
			
		}
	}
}
