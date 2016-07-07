package com.example.paul.all_sensor_logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.paul.all_sensor_logger.bt.BTSerialDevice;
import com.example.paul.all_sensor_logger.bt.BTSerialPortCommunicationService;
import com.example.paul.all_sensor_logger.views.CustomAdapter;
import com.example.paul.all_sensor_logger.views.ModelObject;
import com.nullwire.trace.ExceptionHandler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends FragmentActivity{

    //Bluetooth
    private Button btScan;
    private BTSerialPortCommunicationService mBluetoothService =null;
    private BluetoothAdapter mBluetoothAdapter = null;
    public static ArrayList<BTSerialDevice> mDevices = new ArrayList<BTSerialDevice>();
    public static ArrayList<AtomicBoolean> mFlags = new ArrayList<AtomicBoolean>();
    public static ArrayList<Integer> mConnectedDeviceID = new ArrayList<Integer>();
    private static final int REQUEST_ENABLE_BT = 1;
    Intent bluetoothSerialPortServiceIntent = null;
    /* ListView Related */
    private ListView listView;
    private List<ModelObject> mObjects = new ArrayList<ModelObject>();
    private CustomAdapter mAdapter;
    //flag
    private boolean isInitialize = false;
    private boolean isClick = false;
    private boolean startScan = false;
    private boolean isFirstGps = true;
    private boolean isGpsDataReady = true;
    private boolean isSecondRead = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExceptionHandler.register(this, "http://nol.cs.nctu.edu.tw/~pstsao/server.php");
        //initialBTManager();

       /* LayoutInflater inflater = LayoutInflater.from(this);
        final View v1 = inflater.inflate(R.layout.popup_bt, null);
        listView = (ListView) v1.findViewById(R.id.listView);
        mAdapter = new CustomAdapter(this, mObjects);
        listView.setAdapter(mAdapter);
        btScan = (Button) v1.findViewById(R.id.bt_bt_connect);
        btScan.setOnClickListener(scanListener);

        final AlertDialog dialog_list = new AlertDialog.Builder(this)
                .setTitle("Choose Bluetooth device")
                .setView(v1)
                .setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_BACK:
                                Log.v("Tag", "KEYCODE_BACK");
                                return true;
                        }
                        return false;
                    }
                })
                .show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//响应listview中的item的点击事件

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                // TODO Auto-generated method stub
                BluetoothDevice device =mDevices.get(position).getDevice();

                Log.i("TAG", "Position "+String.valueOf(position));
                Log.i("TAG", "Choose device:" + device.getName()+" "+ device.getAddress());
                AtomicBoolean flag;
                int connectState = device.getBondState();
                switch(connectState)
                {
                    //Unpaired
                    case BluetoothDevice.BOND_NONE:
                        pairDevice(device);
                        flag = mFlags.get(position);
                        flag.set(true);
                        mConnectedDeviceID.add(Integer.valueOf(position));
                        break;
                    //Paired
                    case BluetoothDevice.BOND_BONDED:
                        //Log.d("TAG", "Connecting to device:"+macAddress);
                        BTSerialDevice d = mDevices.get(position);
                        d.SetStartTime(System.currentTimeMillis());
                        flag = mFlags.get(position);
                        flag.set(true);
                        mBluetoothService.connectToDevice(mAdapter.getData().get(position).getAddress());
                        mConnectedDeviceID.add(Integer.valueOf(position));
                        break;
                }



                dialog_list.cancel();
            }
        });*/

        /*deal with Layout*/
        //get contorl of TabHost
        FragmentTabHost mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.container);
        mTabHost.addTab(mTabHost.newTabSpec("one")
                .setIndicator("Main")
                , MainFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("two")
                .setIndicator("Setup")
                ,SetupFragment.class,null);

    }
    private void findview(){

        /*btLog = (Button) findViewById(R.id.bt_log);
        tv_durationTime = (TextView) findViewById(R.id.tv_durationtimeresult);

        tv_speedresult = (TextView) findViewById(R.id.tv_speedresult);
        tv_timeresult = (TextView) findViewById(R.id.tv_timeresult);
        tv_latresult = (TextView) findViewById(R.id.tv_latresult);
        tv_lngresult = (TextView) findViewById(R.id.tv_lngresult);
        tv_onRackStatus = (TextView) findViewById(R.id.tv_onracktestresult);
        tv_gpsStatus = (TextView) findViewById(R.id.tv_gpsStatus);*/

    }
    //BLUETOOTH
    /*private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothService = ((BTSerialPortCommunicationService.LocalBinder) service)
                    .getService();
            Log.i("TAG", "Initializing Bluetooth.....");
            if (!mBluetoothService.initialize()) {
                Log.e("TAG", "Unable to initialize Bluetooth");
                //finish();
            }
            Log.i("TAG", "Success!");
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothService = null;
        }
    };

    private final BroadcastReceiver mBTUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //("Start scaning.....");
                btScan.setText("Scaning...");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //("Scan complete.");
                btScan.setText("Disconnect");
                mBluetoothService.updatBoundedBTDevices();
                setupListView();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice d = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BTSerialDevice p = new BTSerialDevice(d, d.getName(), d.getAddress());
                int position = findBTDevice(d.getAddress());
                if (position == -1) {
                    AtomicBoolean flag = new AtomicBoolean(false);
                    mDevices.add(p);
                    mFlags.add(flag);
                    Log.i("TAG", "Find device:"+p.getDevice().getAddress());
                }

               // mBluetoothService.updatBoundedBTDevices();
               // setupListView();
            }*/ /*else if (BTSerialPortCommunicationService.ACTION_ACCEROMETER_DATA.equals(action)) {
                final float values [] = intent.getFloatArrayExtra(BTSerialPortCommunicationService.EXTRA_DATA);
                final String addr = intent.getStringExtra(BTSerialPortCommunicationService.EXTRA_NAME);
                final int position = findBTDevice(addr);
                final BTSerialDevice d;
                if (position != -1) {
                    d = mDevices.get(position);
                    d.accumulateReceivedData();

                    if (SystemParameters.isServiceRunning) {
                        double gX = 0,gY=0,gZ=0;
                        long timeStamp=0;
                        gX = values[0];
                        gY = values[1];
                        gZ = values[2];
                        timeStamp = System.currentTimeMillis() - SystemParameters.offset;

                        int numOfSec = (int) (System.currentTimeMillis() - systemStartTime)/1000;
                        if (numOfSec > 10 && SystemParameters.isOnRack !=2) {
                            SystemParameters.isOnRack = 2;
                            //tv_onRackStatus.setText("True");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                    tv_onRackStatus.setText("Ready");
                                }
                            });
                        }
                        try {
                            seq++;
                            //VC vc = new VC(seq,timeStamp,numOfSec,gX,gY,gZ,speed);
                            //vce.AccWriter.writeAccFile(timeStamp, gX, gY, gZ);
                            d.getAccWriter().writeAccFile(timeStamp, gX, gY, gZ);
                            SystemParameters.AccCount++;
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }*/



            	/*
            	runOnUiThread(new Runnable() {
                    public void run() {
                        displayAccData(position, values);
                    }
                });
                */

            /*} else if (BTSerialPortCommunicationService.ACTION_MAGNETIC_FIELD_DATA.equals(action)) {
                final float values [] = intent.getFloatArrayExtra(BTSerialPortCommunicationService.EXTRA_DATA);
                final String addr = intent.getStringExtra(BTSerialPortCommunicationService.EXTRA_NAME);
                final int position = findBTDevice(addr);
                final BTSerialDevice d;
                if (position != -1) {
                    d = mDevices.get(position);
                    //d.accumulateReceivedData();
                    if (SystemParameters.isServiceRunning) {
                        long timeStamp = System.currentTimeMillis() - SystemParameters.offset;
                        float mX = values[0];
                        float mY = values[1];
                        float mZ = values[2];

                        try {
                            //MagWriter.writeMagFile(timestamp, mX, mY, mZ);
                            d.getMagWriter().writeMagFile(timeStamp, mX, mY, mZ);
                            SystemParameters.MagCount++;
                            //Log.d("startBtThread", "get Mag data.");
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }


            } else if (BTSerialPortCommunicationService.ACTION_GYROSCOPE_DATA.equals(action)) {
                final float values [] = intent.getFloatArrayExtra(BTSerialPortCommunicationService.EXTRA_DATA);
                final String addr = intent.getStringExtra(BTSerialPortCommunicationService.EXTRA_NAME);
                final int position = findBTDevice(addr);
                final BTSerialDevice d;
                if (position != -1) {
                    d = mDevices.get(position);
                    //d.accumulateReceivedData();
                    if (SystemParameters.isServiceRunning) {
                        long timeStamp = System.currentTimeMillis() - SystemParameters.offset;
                        float aX = values[0];
                        float aY = values[1];
                        float aZ = values[2];

                        try {
                            //GyroWriter.writeGyroFile(timestamp, aX, aY, aZ);
                            d.getGyroWriter().writeGyroFile(timeStamp, aX, aY, aZ);
                            SystemParameters.GyroCount++;
                            //Log.d("startBtThread", "get Gyro data.");

                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }




            } else if (BTSerialPortCommunicationService.ACTION_GPS_DATA.equals(action)) {
                final long time = intent.getLongExtra(BTSerialPortCommunicationService.EXTRA_DATA, 0L);
                final double dValues [] = intent.getDoubleArrayExtra(BTSerialPortCommunicationService.EXTRA_DATA);
                final boolean bValues [] = intent.getBooleanArrayExtra(BTSerialPortCommunicationService.EXTRA_DATA);
                final float fValues [] = intent.getFloatArrayExtra(BTSerialPortCommunicationService.EXTRA_DATA);
                final String addr = intent.getStringExtra(BTSerialPortCommunicationService.EXTRA_NAME);
                final int position = findBTDevice(addr);
                final BTSerialDevice d;

                if (position != -1) {
                    d = mDevices.get(position);
                    d.accumulateReceivedData();
                    if (SystemParameters.isServiceRunning) {
                        SystemParameters.HGpsCount++;
                        try {
                            d.getGPSWriter().writeHGpsFile(time, dValues[0], dValues[1], dValues[2], fValues[0], fValues[1]);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    };*/
    /*private void initialBTManager() {
        //啟動BT service
        Log.d("initialBTManager", "init BT");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "The device don't support bluetooth", Toast.LENGTH_LONG).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        registerReceiver(mBTUpdateReceiver, makeBTUpdateIntentFilter());


        Intent bluetoothSerialPortServiceIntent = new Intent(this, BTSerialPortCommunicationService.class);
        bindService(bluetoothSerialPortServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }
    private static IntentFilter makeBTUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        //BT device discovery
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);*/

        //BT data service
        /*intentFilter.addAction(BTSerialPortCommunicationService.ACTION_ACCEROMETER_DATA);
        intentFilter.addAction(BTSerialPortCommunicationService.ACTION_MAGNETIC_FIELD_DATA);
        intentFilter.addAction(BTSerialPortCommunicationService.ACTION_GYROSCOPE_DATA);
        intentFilter.addAction(BTSerialPortCommunicationService.ACTION_GPS_DATA);*/

        /*return intentFilter;
    }

    private void setupListView() {
        mAdapter.getData().clear();
//        Log.i("TAG", "Initializing ListView....." + mAdapter.getData().size());
        for (int i = 0, size = mDevices.size(); i < size; i++) {
            BTSerialDevice d = mDevices.get(i);
            boolean isPaired = d.getDevice().getBondState()==BluetoothDevice.BOND_BONDED? true : false;
            ModelObject object = new ModelObject(d.getDevice().getName(), d.getDevice().getAddress(), String.valueOf(isPaired), "");
            mObjects.add(object);
        }
        Log.i("TAG", "Initialized ListView....."+ mAdapter.getData().size());

        mAdapter.notifyDataSetChanged();

    }

    private void scanBTDevice() {
        mBluetoothAdapter.startDiscovery();
        for (BluetoothDevice d :mBluetoothAdapter.getBondedDevices()) {
            BTSerialDevice btDevice = new BTSerialDevice(d, d.getName(), d.getAddress());
            mDevices.add(btDevice);
            AtomicBoolean flag = new AtomicBoolean(false);
            mFlags.add(flag);
        }
        setupListView();
        mBluetoothService.updatBoundedBTDevices();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int findBTDevice(String macAddr) {
        if (mDevices.size() == 0)
            return -1;
        for (int i=0; i<mDevices.size(); i++) {
            BTSerialDevice tmpDevice = mDevices.get(i);
            if (macAddr.matches(tmpDevice.getDevice().getAddress()))
                return i;
        }
        return -1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                //finish();
                Toast.makeText(getApplicationContext(), "Bluetooth not enabled.", Toast.LENGTH_LONG).show();
                return;
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Bluetooth enabled.", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private Button.OnClickListener scanListener = new Button.OnClickListener() {
        public void onClick(View v) {
            if (!startScan) {
                startScan = true;
                //btScan.setText("Disconnect");
                mDevices.clear();
                mFlags.clear();
                // Start to scan the ble device
                scanBTDevice();
            } else {
                startScan = false;
                resetBTConnection();
                setupListView();
                btScan.setText("Scan");
            }

        }
    };*/

    /*@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String macAddress = mAdapter.getData().get(position).getAddress();
        boolean isPaired = Boolean.valueOf(mAdapter.getData().get(position).getPair());

        if (isPaired) {
            Log.d("TAG", "Connecting to device:"+macAddress);
            BTSerialDevice d = mDevices.get(position);
            d.SetStartTime(System.currentTimeMillis());
            AtomicBoolean flag = mFlags.get(position);
            flag.set(true);
            mBluetoothService.connectToDevice(macAddress);
            mConnectedDeviceID.add(Integer.valueOf(position));
        }


    }*/

    /*public void resetBTConnection() {
        try {
            mBluetoothService.disconnect();
            mBluetoothService.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        /*for (BTSerialDevice d : mDevices) {
            if (d.getGPSWriter() != null) {
                d.getGPSWriter().closefile();
            }
            if (d.getAccWriter() != null) {
                d.getAccWriter().closefile();
            }
            if (d.getMagWriter() != null) {
                d.getMagWriter().closefile();
            }
            if (d.getGyroWriter() != null) {
                d.getGyroWriter().closefile();
            }
        }*/
       /* mDevices.clear();
        mFlags.clear();
        mConnectedDeviceID.clear();
    }*/


    public void onDestroy(){
        super.onDestroy();
        /*if (mServiceConnection != null)
            unbindService(mServiceConnection);
        Intent intent = new Intent(MainActivity.this,NetworkCheckService.class);
        stopService(intent);
        intent = new Intent(MainActivity.this, BTSerialPortCommunicationService.class);
        stopService(intent);*/


    }

}

