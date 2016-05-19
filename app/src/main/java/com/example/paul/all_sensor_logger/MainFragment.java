package com.example.paul.all_sensor_logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*Tab page class inhreits Fragment*/
public class MainFragment extends Fragment {
    //buttons
    private Button startbutton;
    private Button logoutbutton;
    private Button recordbutton;

    private SensorManager mSensorManager;
    private List<String> deviceSensorsName = new ArrayList<String>();
    private ListView lv;
    private List<Sensor> deviceSensors = new ArrayList<Sensor>();
    private FileOutputStream file_acc;
    private FileOutputStream file_gro;
    private FileOutputStream file_mag;
    private FileOutputStream file_lig;
    private FileOutputStream file_pre;
    private FileOutputStream file_gps;
    private String subdir;
    private String filename_acc;
    private String filename_gro;
    private String filename_mag;
    private String filename_lig;
    private String filename_pre;
    private String filename_gps;
    private String filename_audio;
    private MainActivity mMainActivity;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private TextView user;
    private String path;
    private String ts;
    private int IsFileOpen = 0;
    private Sensor[] SensorList = new Sensor[5];
    private boolean is_recording;
    private MediaRecorder mRecorder = null;
    private Thread timer;
    private LocationManager mgr;
    private String best;
    private int REQUEST_ENABLE_BT = 0;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        findBT();

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        init_buttons();

        //set text view
        user = (TextView) (getView().findViewById(R.id.user_name));
        user.append(sharedPreferences.getString("account", "N//A"));

        //flags
        is_recording = false;
        IsFileOpen = 0;

        /*get sensor*/
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        String t_cartype = sharedPreferences.getString("CarType1", null);
        String t_carage = sharedPreferences.getString("CarAge1", null);

        if (t_carage == null || t_cartype == null) {
            int CarInfoCounter = sharedPreferences.getInt("CarInfoCounter", 0);
            if (CarInfoCounter == 0) {
                editor.putInt("CarInfoCounter", 1);
                editor.putInt("CarInfoNow", 1);
                editor.commit();
            }
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View v1 = inflater.inflate(R.layout.popup_layout_profile, null);

            new AlertDialog.Builder(getActivity())
                    .setTitle("Please key in new car info")
                    .setView(v1)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String Pop_cartype = ((EditText) v1.findViewById(R.id.pop_cartype)).getText().toString();
                            String Pop_carage = ((EditText) v1.findViewById(R.id.pop_carage)).getText().toString();

                            if ("".equals(Pop_cartype)) {
                                Toast.makeText(getContext(), "Car type can't be empty", Toast.LENGTH_LONG).show();
                                try {
                                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, false);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else if ("".equals(Pop_carage)) {
                                Toast.makeText(getContext(), "Car age can't be empty", Toast.LENGTH_LONG).show();
                                try {
                                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else {
                                int CarInfoCounter = sharedPreferences.getInt("CarInfoCounter", 0);
                                if (CarInfoCounter == 0) {
                                    Log.v("Tag", "Something wrong in main 1");
                                } else {
                                    CarInfoCounter++;
                                    String a = "CarType1";
                                    String b = "CarAge1";
                                    editor.putString(a, Pop_cartype);
                                    editor.putString(b, Pop_carage);
                                    editor.commit();

                                }
                                try {
                                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    })
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

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mysensorListener);
        if (IsFileOpen == 1) {
            close_all();
            IsFileOpen = 0;
        }
        if (is_recording) {
            stopRecording();
        }
    }


    final SensorEventListener mysensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            long timeStamp = System.currentTimeMillis();
            String data = Long.toString(timeStamp);
            for (float val : event.values) {
                data = data + "," + val;
            }
            data += "\n";
            try {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        file_acc.write(data.getBytes());
                        IsFileOpen = 1;
                        break;

                    case Sensor.TYPE_GYROSCOPE:
                        file_gro.write(data.getBytes());
                        IsFileOpen = 1;
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:
                        file_mag.write(data.getBytes());
                        IsFileOpen = 1;
                        break;

                    case Sensor.TYPE_LIGHT:
                        file_lig.write(data.getBytes());
                        IsFileOpen = 1;
                        break;

                    case Sensor.TYPE_PRESSURE:
                        file_pre.write(data.getBytes());
                        IsFileOpen = 1;
                        break;

                    default:
                        Log.d("Tag", "unexcepted sensor type");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private Button.OnClickListener logoutbuttonListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            String version = sharedPreferences.getString("VERSION", null);
            editor.clear();
            editor.commit();
            editor.putString("VERSION", version);
            editor.commit();
            Intent i = new Intent(getContext(), LoginPage.class);
            startActivity(i);
        }
    };

    private Button.OnClickListener recordbuttonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            is_recording = !is_recording;
            if (is_recording) {
                startRecording();
                recordbutton.setText("Stop record");
            } else {
                stopRecording();
                recordbutton.setText("Start record");
            }
        }
    };

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        Long tsLong = System.currentTimeMillis() / 1000;
        mRecorder.setOutputFile(Environment.getExternalStorageDirectory().getPath() + "/Sensorlogger/" + tsLong.toString() + ".3gp");
        filename_audio=tsLong.toString() + ".3gp";
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("Tag", "prepare() failed");
        }
        timer = new Thread() {
            public void run() {
                while (is_recording) {
                    // do stuff in a separate thread
                    try {
                        Thread.sleep(30000);    // sleep for 30 seconds
                        uiCallback.sendEmptyMessage(0);
                    } catch (InterruptedException e) {

                    }

                }
            }
        };

        mRecorder.start();
        // timer.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        try {
            fileupload(Environment.getExternalStorageDirectory().getPath() + "/Sensorlogger/", filename_audio,"","");
        } catch (IOException e) {
            e.printStackTrace();
        }
        timer.interrupt();
    }

    private Handler uiCallback = new Handler() {
        public void handleMessage(Message msg) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            try {
                fileupload(Environment.getExternalStorageDirectory().getPath() + "/Sensorlogger/", filename_audio,"","");
            } catch (IOException e) {
                e.printStackTrace();
            }

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            Long tsLong = System.currentTimeMillis() / 1000;
            mRecorder.setOutputFile(Environment.getExternalStorageDirectory().getPath() + "/Sensorlogger/" + tsLong.toString() + ".3gp");
            filename_audio=tsLong.toString() + ".3gp";
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("Tag", "prepare() failed");
            }
            mRecorder.start();

        }
    };


    private Button.OnClickListener startbuttonListener = new Button.OnClickListener() {

        @Override

        public void onClick(View v) {
            if (startbutton.getText().equals("Stop")) {

                Log.d("Tag", "Stop");
                Log.d("Tag", sharedPreferences.getString("token", null));
                startbutton.setText("Start");

                mSensorManager.unregisterListener(mysensorListener);
                try {
                    mgr.removeUpdates(locationlistener);
                    mgr.removeGpsStatusListener(GPSstatusListener);
                } catch (SecurityException e) {
                    Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
                }
                int counter = sharedPreferences.getInt("CarInfoNow", 0);
                String t_cartype = sharedPreferences.getString("CarType" + counter, null);
                String t_carage = sharedPreferences.getString("CarAge" + counter, null);

                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Is Car Info is right?");
                dialog.setMessage(t_cartype + "," + t_carage);
                dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                        final View v1 = inflater.inflate(R.layout.popup_layout_carlist, null);

                        ArrayList<String> CarInfo = new ArrayList<>();
                        String temp = "";
                        int counter = sharedPreferences.getInt("CarInfoCounter", 0);
                        if (counter == 0) {
                            Log.v("Tag", "Something wrong in profile 1");
                        } else {
                            for (int i = 1; i <= counter; i++) {
                                temp = "";
                                String a = "CarType" + i;
                                String b = "CarAge" + i;
                                String t_cartype = sharedPreferences.getString(a, null);
                                String t_carage = sharedPreferences.getString(b, null);

                                if ("".equals(t_cartype)) {
                                    Log.v("Tag", "Something wrong in profile 2");
                                } else if ("".equals(t_carage)) {
                                    Log.v("Tag", "Something wrong in profile 3");
                                } else {
                                    temp = t_cartype + "," + t_carage;
                                    CarInfo.add(temp);
                                }
                            }
                        }

                        ListView listView = (ListView) v1.findViewById(R.id.car_list);
                        ArrayAdapter<String> listAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, CarInfo);
                        listView.setAdapter(listAdapter);

                        final AlertDialog dialog_list = new AlertDialog.Builder(getActivity())
                                .setTitle("Choose car info")
                                .setView(v1)
                                .setPositiveButton("None of above", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                                        final View v1 = inflater.inflate(R.layout.popup_layout_profile, null);

                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("Please key in new car info")
                                                .setView(v1)
                                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        String Pop_cartype = ((EditText) v1.findViewById(R.id.pop_cartype)).getText().toString();
                                                        String Pop_carage = ((EditText) v1.findViewById(R.id.pop_carage)).getText().toString();

                                                        if ("".equals(Pop_cartype)) {
                                                            Toast.makeText(getContext(), "Car type can't be empty", Toast.LENGTH_LONG).show();
                                                            try {
                                                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                                                field.setAccessible(true);
                                                                field.set(dialog, false);

                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }

                                                        } else if ("".equals(Pop_carage)) {
                                                            Toast.makeText(getContext(), "Car age can't be empty", Toast.LENGTH_LONG).show();
                                                            try {
                                                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                                                field.setAccessible(true);
                                                                field.set(dialog, false);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }

                                                        } else {
                                                            int CarInfoCounter = sharedPreferences.getInt("CarInfoCounter", 0);
                                                            if (CarInfoCounter == 0) {
                                                                Log.v("Tag", "Something wrong in profile 5");
                                                            } else {
                                                                CarInfoCounter++;

                                                                String a = "CarType" + CarInfoCounter;
                                                                String b = "CarAge" + CarInfoCounter;
                                                                editor.putInt("CarInfoCounter", CarInfoCounter);
                                                                editor.putInt("CarInfoNow", CarInfoCounter);
                                                                editor.putString(a, Pop_cartype);
                                                                editor.putString(b, Pop_carage);
                                                                editor.commit();

                                                            }

                                                            try {
                                                                close_all();
                                                                folderfileupload(path, Pop_cartype, Pop_carage);
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }

                                                            try {
                                                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                                                field.setAccessible(true);
                                                                field.set(dialog, true);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                    }
                                                })
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
                                    }
                                })
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
                            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                // TODO Auto-generated method stub
                                arg2++;
                                editor.putInt("CarInfoNow", arg2);
                                editor.commit();

                                String a = "CarType" + arg2;
                                String b = "CarAge" + arg2;
                                String t_cartype = sharedPreferences.getString(a, null);
                                String t_carage = sharedPreferences.getString(b, null);

                                if ("".equals(t_cartype)) {
                                    Log.v("Tag", "Something wrong in profile 7");
                                } else if ("".equals(t_carage)) {
                                    Log.v("Tag", "Something wrong in profile 8");
                                } else {
                                    try {
                                        close_all();
                                        folderfileupload(path, t_cartype, t_carage);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                dialog_list.cancel();
                            }
                        });

                    }

                });
                dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        // data upload ver 1

                        int CarInfoNow = sharedPreferences.getInt("CarInfoNow", 0);
                        if (CarInfoNow == 0) {
                            Log.v("Tag", "Something wrong in profile 9");
                        } else {
                            String a = "CarType" + CarInfoNow;
                            String b = "CarAge" + CarInfoNow;
                            String t_cartype = sharedPreferences.getString(a, null);
                            String t_carage = sharedPreferences.getString(b, null);

                            if ("".equals(t_cartype)) {
                                Log.v("Tag", "Something wrong in profile 7");
                            } else if ("".equals(t_carage)) {
                                Log.v("Tag", "Something wrong in profile 8");
                            } else {
                                try {
                                    close_all();
                                    folderfileupload(path, t_cartype, t_carage);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                        }

                    }

                });
                dialog.setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_BACK:
                                Log.v("Tag", "KEYCODE_BACK");
                                return true;
                        }
                        return false;
                    }
                });
                dialog.show();

            } else {
                /*Start  listening*/
                initialLocationManager();

                for (int i = 0; i < 5; i++) {
                    SensorList[i] = null;
                }
                SensorList[0] = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensorManager.registerListener(mysensorListener, SensorList[0], SensorManager.SENSOR_DELAY_FASTEST);
                SensorList[1] = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                mSensorManager.registerListener(mysensorListener, SensorList[1], SensorManager.SENSOR_DELAY_FASTEST);
                SensorList[2] = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                mSensorManager.registerListener(mysensorListener, SensorList[2], SensorManager.SENSOR_DELAY_FASTEST);
                SensorList[3] = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                mSensorManager.registerListener(mysensorListener, SensorList[3], SensorManager.SENSOR_DELAY_FASTEST);
                SensorList[4] = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
                mSensorManager.registerListener(mysensorListener, SensorList[4], SensorManager.SENSOR_DELAY_FASTEST);

                Long tsLong = System.currentTimeMillis() / 1000;
                ts = tsLong.toString();

                path = Environment.getExternalStorageDirectory().getPath() + "/Sensorlogger/" + ts;
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                filename_acc = ts + "_acc";
                filename_gro = ts + "_gro";
                filename_mag = ts + "_mag";
                filename_lig = ts + "_lig";
                filename_pre = ts + "_pre";
                filename_gps = ts + "_gps";

                try {
                    if (SensorList[0] != null) {
                        file_acc = new FileOutputStream(new File(path, (filename_acc)));
                        Log.d("Tag", "ACC fileopen");
                    }
                    if (SensorList[1] != null) {
                        file_gro = new FileOutputStream(new File(path, (filename_gro)));
                        Log.d("Tag", "GRO fileopen");
                    }
                    if (SensorList[2] != null) {
                        file_mag = new FileOutputStream(new File(path, (filename_mag)));
                        Log.d("Tag", "MAG fileopen");
                    }
                    if (SensorList[3] != null) {
                        file_lig = new FileOutputStream(new File(path, (filename_lig)));
                        Log.d("Tag", "LIG fileopen");
                    }
                    if (SensorList[4] != null) {
                        file_pre = new FileOutputStream(new File(path, (filename_pre)));
                        Log.d("Tag", "PRE fileopen");
                    }
                    file_gps = new FileOutputStream(new File(path, (filename_gps)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                startbutton.setText("Stop");
            }

        }
    };

    private void token_check() {
        API.check_token_valid(sharedPreferences.getString("token", null), new ResponseListener() {
            public void onResponse(JSONObject response) {

            }

            public void onErrorResponse(VolleyError error) {
                API.login(sharedPreferences.getString("account", null), sharedPreferences.getString("passwd", null), new ResponseListener() {
                    public void onResponse(JSONObject response) {
                        Log.d("Tag", "token expire,getting new token");
                        String token = null;
                        try {
                            token = response.getJSONObject("data").getString("token");
                            editor.putString("token", token);
                            editor.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "no token found while uploading, please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getContext(), LoginPage.class);
                        startActivity(i);
                    }
                });
            }
        });
    }

    private void fileupload(final String filepath,final String filename, final String CarType, final String CarAge) throws IOException {
        if (NetworkCheck.isNetworkConnected(getContext())) {
            token_check();
            final String token = sharedPreferences.getString("token", null);
            API.upload_file(filepath, token, CarType, CarAge, filename, sharedPreferences.getString("account", null), new ResponseListener() {
                public void onResponse(JSONObject response) {
                    File file = new File(filepath);
                    file.delete();
                    Toast.makeText(getContext(), "upload success@@", Toast.LENGTH_SHORT).show();
                }

                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getContext(), "something went wrong while uploading", Toast.LENGTH_SHORT).show();
                    JSONObject response = null;
                    try {
                        response = new JSONObject(new String(error.networkResponse.data));
                        Log.d("Tag", response.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("Tag", String.valueOf(error.networkResponse.statusCode));

                    editor.putString(ts + "_type", CarType);
                    editor.putString(ts + "_age", CarAge);
                    editor.putString(ts + "_account", sharedPreferences.getString("account", null));
                    editor.commit();
                }
            });

        } else {
            Toast.makeText(getContext(), "no network aviable now, will upload later", Toast.LENGTH_SHORT).show();
            editor.putString(ts + "_type", CarType);
            editor.putString(ts + "_age", CarAge);
            editor.putString(ts + "_account", sharedPreferences.getString("account", null));
            editor.commit();
        }
    }

    private void folderfileupload(final String dirpath, final String CarType, final String CarAge) throws IOException {
        File file_dir = new File(dirpath);
        File[] files = file_dir.listFiles();
        for (int i = 0; i < files.length; ++i) {
            String filename = files[i].getName();
            fileupload( dirpath,filename, CarType, CarAge);
        }
    }

    private void close_all() {
        try {
            if (SensorList[0] != null) {
                file_acc.close();
            }
            if (SensorList[1] != null) {
                file_gro.close();
            }
            if (SensorList[2] != null) {
                file_mag.close();
            }
            if (SensorList[3] != null) {
                file_lig.close();
            }
            if (SensorList[4] != null) {
                file_pre.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 5; i++) {
            SensorList[i] = null;
        }
    }

    private void initialLocationManager() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        best = mgr.getBestProvider(criteria, true);
        Location location = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (best != null)
            location = mgr.getLastKnownLocation(best);
        mgr.requestLocationUpdates("gps", 0, 0, locationlistener); // 讓locationlistener處理資料有變化時的事情
        mgr.addGpsStatusListener(GPSstatusListener);//to get GPS status
        Log.d("GPS", "GPS Ready");
    }

    private final LocationListener locationlistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            float speed = location.getSpeed() * (float) (3.6);
            long time = location.getTime();
            double height = location.getAltitude();
            float bearing = location.getBearing();

            String data = time + "," + lat + "," + lng + "," + speed + "," + height + "," + bearing + "\n";
            try {
                file_gps.write(data.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

    };

    private final GpsStatus.Listener GPSstatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            // TODO Auto-generated method stub
        }
    };

    private void init_buttons() {
        startbutton = (Button) getView().findViewById(R.id.start_button);
        startbutton.setOnClickListener(startbuttonListener);
        logoutbutton = (Button) getView().findViewById(R.id.logout_button);
        logoutbutton.setOnClickListener(logoutbuttonListener);
        recordbutton = (Button) getView().findViewById(R.id.record_button);
        recordbutton.setOnClickListener(recordbuttonListener);
    }

    private void findBT()
    {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getContext(), "The device don't support bluetooth", Toast.LENGTH_LONG).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1);
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v1 = inflater.inflate(R.layout.popup_layout_carlist, null);
        ListView listView = (ListView) v1.findViewById(R.id.car_list);
        listView.setAdapter(mArrayAdapter);

        final AlertDialog dialog_list = new AlertDialog.Builder(getActivity())
                .setTitle("Choose device")
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
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub

                dialog_list.cancel();
            }
        });
    }
}


