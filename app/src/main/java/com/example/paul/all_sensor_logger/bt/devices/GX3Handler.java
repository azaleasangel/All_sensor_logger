package com.example.paul.all_sensor_logger.bt.devices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.bluetooth.BluetoothDevice;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.util.SparseArray;

public class GX3Handler {
	private static final String TAG = GX3Handler.class.getName();
	private final List<SparseArray<SensorEventListener>> eventListeners = new ArrayList<SparseArray<SensorEventListener>>();
	private OutputStream outputStream;
	private InputStream inputStream;
	private BluetoothDevice device;
	
	private int SamplingRate = 250; //the sampling rate = 1000/Samplingrate
	private ByteBuffer buffer = ByteBuffer.allocate(10750 * 5); //buffer 3 sec data
	
	private AtomicBoolean isReceived = new AtomicBoolean(false);
	
	private long timer = 0L;
	private int miss = 0;
	private float acc[] = new float[3];
	private float mag[] = new float[3];
	private float gyro[] = new float[3];
	
	//GX3 command set
	private static final byte CMD_BAUD[]={(byte) 0xd9,(byte) 0xc3,(byte)0x55,(byte) 0x01, (byte) 0x01, (byte) 0x00, (byte)0x01, (byte)0xc2, (byte)0x00}; //set baud rate to 230400 (0x00038400) 
	private static final byte CMD_OPEN[]={(byte) 0xd6,(byte) 0xc6,(byte)0x6b,(byte) 0xcb}; //Continuous Preset, get acceleration, angular rate and magnetometer vectors continuously.
	private static final byte CMD_GET[]={(byte) 0xd4,(byte) 0xa3,(byte)0x47,(byte) 0x02}; //Set mode, put in continuous mode.
	private static final byte CMD_CLOSE[]={(byte) 0xfa,(byte) 0x75,(byte)0xb4}; //Stop continuous mode.
	
	private static final int STATUS_IDLE = 1 << 0;
	private static final int STATUS_SAMPLING = 1 << 1;
	private static final int STATUS_OPEN_CHANEEL= 1 << 2;
	private static final int STATUS_OPEN_CHANNEL_OK = 1 << 3;
	private static final int STATUS_RETREIVE_DATA = 1 << 4;
	private static final int STATUS_CLOSE_CHANEEL = 1 << 5;
	
	private int status = STATUS_IDLE;
	
	public GX3Handler() {
		
	}
	
	public void setInputStream(InputStream is) {
		this.inputStream = is;
	}
	
	public void setOutputStream(OutputStream os) {
		this.outputStream = os;
	}
	
	public void setBTDevice(BluetoothDevice d) {
		this.device = d;
	}
	
	public void registerSensorEventListener(SensorEventListener listener, final int type) {
		SparseArray<SensorEventListener> e = new SparseArray<SensorEventListener>();
		e.put(type, listener);
		this.eventListeners.add(e);
	}
	
	public void unRegisterSensorEventListener(SensorEventListener listener, final int type) {
		for (int i=0, size=eventListeners.size(); i<size; i++) {
			SensorEventListener l = eventListeners.get(i).get(type);
			if (l.equals(listener)) {
				eventListeners.remove(i);
				return;
			}
		}
	}
	
	public void open() {
		setSamplingRate();
		readingDataFromChannel();
	}
	
	public void close() {
		closeDataChannel();
	}
	
	private void setSamplingRate() {
		short samplingRate = (short) (1000/SamplingRate);
		byte CMD_SET_SAMPLE_RATE[] = new byte[20];
		CMD_SET_SAMPLE_RATE[0]=(byte) 0xdb;
		CMD_SET_SAMPLE_RATE[1]=(byte) 0xa8;
		CMD_SET_SAMPLE_RATE[2]=(byte) 0xb9;
		CMD_SET_SAMPLE_RATE[3]=(byte) 0x02;
		CMD_SET_SAMPLE_RATE[4] = (byte) ((samplingRate & 0xFF00)>>8);
		CMD_SET_SAMPLE_RATE[5] = (byte) (samplingRate & 0xFF);
		CMD_SET_SAMPLE_RATE[6]=(byte) 0xc0;
		CMD_SET_SAMPLE_RATE[7]=(byte) 0x00;
		CMD_SET_SAMPLE_RATE[8]=(byte) 0x02;
		CMD_SET_SAMPLE_RATE[9]=(byte) 0x02;
		
		for(int i =10;i<20;i++)
			CMD_SET_SAMPLE_RATE[i]=(byte)0x00;
		try {
			isReceived.set(true);
			status = STATUS_SAMPLING;
			Log.d(TAG, "Set sampling rate.");
			this.outputStream.write(CMD_SET_SAMPLE_RATE);
			this.outputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	private void readingDataFromChannel() {
		new Thread() {
			@Override
			public void run() {
				byte oneByte[] = new byte[1];
				while (isReceived.get()) {
					try {
						inputStream.read(oneByte);
						//Log.d(TAG, "Status:"+String.valueOf(isReceived.get()));
					
						switch (status) {
						case STATUS_IDLE:
							break;
						case STATUS_SAMPLING:							
							//Log.d(TAG, String.format("%02X", oneByte[0]));
							if (Byte.valueOf(oneByte[0]).equals(Byte.valueOf((byte) 0xdb))) {
								//Log.d(TAG, "got it!!");
								byte[] data = new byte[19];  
								inputStream.read(data);
								prepareDataChannel();
							}
							break;
						case STATUS_OPEN_CHANEEL: 
							//Log.d(TAG, String.format("%02X", oneByte[0]));
							if (Byte.valueOf(oneByte[0]).equals(Byte.valueOf((byte) 0xd6))) {
								//Log.d(TAG, "got it!!");
								byte[] data = new byte[3];  
								inputStream.read(data);
								startToGetDataFromChannel();
							}
							break;
						case STATUS_OPEN_CHANNEL_OK:
							//Log.d(TAG, String.format("%02X", oneByte[0]));
							if (Byte.valueOf(oneByte[0]).equals(Byte.valueOf((byte) 0xd4))) {
								//Log.d(TAG, "got it!!");
								byte[] data = new byte[3];  
								inputStream.read(data);
								status = STATUS_RETREIVE_DATA;
							}
							break;
						case STATUS_RETREIVE_DATA:
							//Log.d(TAG, String.format("%02X", oneByte[0]));
							if (Byte.valueOf(oneByte[0]).equals(Byte.valueOf((byte) 0xcb))) {
								//Log.d(TAG, "got it!!");
								byte[] data = new byte[42];  
								inputStream.read(data);
								fireEvent(data);	
							} 
							break;
						case STATUS_CLOSE_CHANEEL:
							//Log.d(TAG, String.format("%02X", oneByte[0]));
							status = STATUS_IDLE;
							break;
						}						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.w(TAG, "Socket may be closed!!");
						isReceived.set(false);
						//e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	private void prepareDataChannel() {
		try {
			this.status = STATUS_OPEN_CHANEEL;
			Log.d(TAG, "Set open channel");
			this.outputStream.write(CMD_OPEN);
			this.outputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void startToGetDataFromChannel() {
		try {
			this.status = STATUS_OPEN_CHANNEL_OK;
			Log.d(TAG, "Set receive data");
			this.outputStream.write(CMD_GET);
			this.outputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void closeDataChannel() {
		try {
			this.status = STATUS_CLOSE_CHANEEL;
			isReceived.set(false);
			Log.d(TAG, "Set close channel");
			this.outputStream.write(CMD_CLOSE);
			this.outputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean fireEvent(byte[] data) {
		byte[] pkt = new byte[42];
		byte[] AccXpkt = new byte[4];
		byte[] AccYpkt = new byte[4];
		byte[] AccZpkt = new byte[4];
		byte[] GyroXpkt = new byte[4];
		byte[] GyroYpkt = new byte[4];
		byte[] GyroZpkt = new byte[4];
		byte[] MagXpkt = new byte[4];
		byte[] MagYpkt = new byte[4];
		byte[] MagZpkt = new byte[4];
		int[] TimerPkt = new int[4];
		int[] CheckSum = new int[2];
		
		int wChecksum = 0;
		int wCalculatedCheckSum = 0;
		int type=0;
		int j=0;
		
		pkt = data;
		
		for(int i=0;i<data.length;i++)
		{
			if(i<40)
				wCalculatedCheckSum += (pkt[i] & 0xFF);
			//Log.d(">>>", pkt[i]+":"+((int)pkt[i] & 0xFF));
			if(type==0)
				AccXpkt[j]=pkt[i];
			else if(type==1)
				AccYpkt[j]=pkt[i];
			else if(type==2)
				AccZpkt[j]=pkt[i];
			else if(type==3)
				GyroXpkt[j]=pkt[i];
			else if(type==4)
				GyroYpkt[j]=pkt[i];
			else if(type==5)
				GyroZpkt[j]=pkt[i];
			else if(type==6)
				MagXpkt[j]=pkt[i];
			else if(type==7)
				MagYpkt[j]=pkt[i];
			else if(type==8)
				MagZpkt[j]=pkt[i];
			else if(type==9)
				TimerPkt[j]=(pkt[i] & 0xFF);
			else if(type==10)
				CheckSum[j]=(pkt[i] & 0xFF);
			j++;
			if(i%4==3)
			{
				type++;
				j=0;
			}
		}
		
		wCalculatedCheckSum +=0xcb;
		wChecksum =  ((CheckSum[0] <<8) + (CheckSum[1] & 0xFF));
		
		
		timer = ((((((TimerPkt[0]) << 8) & TimerPkt[1]) << 8) & TimerPkt[2]) << 8) & TimerPkt[3];
		timer = (TimerPkt[0] <<24) + (TimerPkt[1] <<16) + (TimerPkt[2] <<8) + (TimerPkt[3] & 0xFF);//convert2ulong(Timerpkt);
		float timer_f = (float)timer/62500;
		timer = (long)(timer_f*1000);
		
        if(wChecksum!=wCalculatedCheckSum)
		{
			miss++;
			//Log.w(TAG, "Data received error!! miss data:"+miss);
			return false;
		}
		
		acc[0] = FloatFromBytes(AccXpkt);
		acc[1] = FloatFromBytes(AccYpkt);
		acc[2] = FloatFromBytes(AccZpkt);
		gyro[0] = FloatFromBytes(GyroXpkt);
		gyro[1] = FloatFromBytes(GyroYpkt);
		gyro[2] = FloatFromBytes(GyroZpkt);
		mag[0] = FloatFromBytes(MagXpkt);
		mag[1] = FloatFromBytes(MagYpkt);
		mag[2] = FloatFromBytes(MagZpkt);
		
		/*SensorEvent e = new SensorEvent(SensorEvent.TYPE_ACCEROMETER, device, 3);
		for (int i=0, size=eventListeners.size(); i<size; i++) {
			if (eventListeners.get(i).get(SensorEvent.TYPE_ACCEROMETER) != null) {
				SensorEventListener l = eventListeners.get(i).get(SensorEvent.TYPE_ACCEROMETER);
				e.values[0] = (float)(acc[0]*9.8);
				e.values[1] = (float)(acc[1]*9.8);
				e.values[2] = (float)(acc[2]*9.8);
				l.onSensorChange(e);
				Log.d(TAG, "time="+timer+" gx="+e.values[0]+" gy="+e.values[1]+" gz="+e.values[2]+"\n");
			}
			
		}
		
		e = new SensorEvent(SensorEvent.TYPE_MAGNETIC_FIELD, device, 3);
		for (int i=0, size=eventListeners.size(); i<size; i++) {
			if (eventListeners.get(i).get(SensorEvent.TYPE_MAGNETIC_FIELD) != null) {
				SensorEventListener l = eventListeners.get(i).get(SensorEvent.TYPE_MAGNETIC_FIELD);
				e.values[0] = mag[0];
				e.values[1] = mag[1];
				e.values[2] = mag[2];
				l.onSensorChange(e);
			}
			
		}
		
		e = new SensorEvent(SensorEvent.TYPE_GYROSCOPE, device, 3);
		for (int i=0, size=eventListeners.size(); i<size; i++) {
			if (eventListeners.get(i).get(SensorEvent.TYPE_GYROSCOPE) != null) {
				SensorEventListener l = eventListeners.get(i).get(SensorEvent.TYPE_GYROSCOPE);
				e.values[0] = gyro[0];
				e.values[1] = gyro[1];
				e.values[2] = gyro[2];
				l.onSensorChange(e);
			}
			
		}*/
		
		return true;
	}
	
	static public float FloatFromBytes(byte[] bytes)
    {
        final int bits = IntFromBytes(bytes);
		return Float.intBitsToFloat(bits);
	}
	
	static public int IntFromBytes(final byte [] bytes)
	{
	        return ByteBuffer.wrap(bytes).getInt();
	}
	
	
}
