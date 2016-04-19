package com.example.paul.all_sensor_logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.Duration;

/*Tab page class inhreits Fragment*/
public class MainFragment extends Fragment
{
    //show contents
    private SensorManager mSensorManager;
    private List<String> deviceSensorsName= new ArrayList<String>();
    private ListView lv;
    private List<Sensor> deviceSensors=new ArrayList<Sensor>();
    private FileOutputStream file_acc;
    private FileOutputStream file_gro;
    private FileOutputStream file_mag;
    private FileOutputStream file_lig;
    private FileOutputStream file_pre;
    private String subdir;
    private String filename_acc;
    private String filename_gro;
    private String filename_mag;
    private String filename_lig;
    private String filename_pre;
    private MainActivity mMainActivity;
    private Button startbutton;
    private Button logoutbutton;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private TextView user;
    private String path;
    private String ts;
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_layout_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        startbutton=(Button)getView().findViewById(R.id.start_button);
        startbutton.setOnClickListener(startbuttonListener);
        logoutbutton=(Button)getView().findViewById(R.id.logout_button);
        logoutbutton.setOnClickListener(logoutbuttonListener);
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor=sharedPreferences.edit();
        user=(TextView)(getView().findViewById(R.id.user_name));
        user.append(sharedPreferences.getString("account","N//A"));
        /*get sensor*/
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        String a = "CarType1";
        String b = "CarAge1";
        String t_cartype = sharedPreferences.getString(a,null);
        String t_carage = sharedPreferences.getString(b,null);

        if(t_carage == null || t_cartype ==null)
        {
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

                            } else{
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
    public void onDestroy(){
        super.onDestroy();
        mSensorManager.unregisterListener(mysensorListener);
        close_all();
    }


    final SensorEventListener mysensorListener = new SensorEventListener() {
        @Override
        public  void onSensorChanged(SensorEvent event) {
            long timeStamp = System.currentTimeMillis();
            String data = Long.toString(timeStamp);
            for (float val : event.values) {
                data =data+ ","+val;
            }
            data += "\n";
            try {
                switch (event.sensor.getType())
                {
                    case Sensor.TYPE_ACCELEROMETER:
                        file_acc.write(data.getBytes());
                        break;

                    case Sensor.TYPE_GYROSCOPE:
                        file_gro.write(data.getBytes());
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:
                        file_mag.write(data.getBytes());
                        break;

                    case Sensor.TYPE_LIGHT:
                        file_lig.write(data.getBytes());
                        break;

                    case Sensor.TYPE_PRESSURE:
                        file_pre.write(data.getBytes());
                        break;

                    default:
                        Log.d("Tag","unexcepted sensor type");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private  Button.OnClickListener logoutbuttonListener = new Button.OnClickListener() {
        @Override

        public void onClick(View v)
        {
            String version=sharedPreferences.getString("VERSION",null);
            editor.clear();
            editor.commit();
            editor.putString("VERSION", version);
            editor.commit();
            Intent i = new Intent(getContext(), LoginPage.class);
            startActivity(i);
        }
    };

    private Button.OnClickListener startbuttonListener = new Button.OnClickListener() {

        @Override

        public void onClick(View v)
        {
            if (startbutton.getText().equals("Stop"))
            {

                Log.d("Tag","Stop");
                Log.d("Tag", sharedPreferences.getString("token", null));
                startbutton.setText("Start");
                mSensorManager.unregisterListener(mysensorListener);

                int counter = sharedPreferences.getInt("CarInfoNow",0);
                String a = "CarType" + counter;
                String b = "CarAge" + counter;
                String t_cartype = sharedPreferences.getString(a,null);
                String t_carage = sharedPreferences.getString(b, null);


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

                        ListView listView = (ListView)v1.findViewById(R.id.car_list);
                        ArrayAdapter<String> listAdapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,CarInfo);
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

                                                        } else{
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
                                                                fileupload(path, Pop_cartype,Pop_carage);
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
                            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
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
                                        fileupload(path, t_cartype,t_carage);
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
                                    fileupload(path, t_cartype,t_carage);
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

            }
            else
            {
                Long tsLong = System.currentTimeMillis()/1000;
                ts = tsLong.toString();

                path = Environment.getExternalStorageDirectory().getPath();
                File dir = new File(path + "/Sensorlogger/"+ts);
                if (!dir.exists()){
                    dir.mkdirs();
                }
                path=path+"/Sensorlogger/"+ts;
                filename_acc = ts+"_acc";
                filename_gro = ts+"_gro";
                filename_mag = ts+"_mag";
                filename_lig = ts+"_lig";
                filename_pre = ts+"_pre";

                File tempfile_acc=new File(path,(filename_acc));
                File tempfile_gro=new File(path,(filename_gro));
                File tempfile_mag=new File(path,(filename_mag));
                File tempfile_lig=new File(path,(filename_lig));
                File tempfile_pre=new File(path,(filename_pre));

                try {
                    file_acc=new FileOutputStream(tempfile_acc);
                    Log.d("Tag","ACC fileopen");
                    file_gro=new FileOutputStream(tempfile_gro);
                    Log.d("Tag","GRO fileopen");
                    file_mag=new FileOutputStream(tempfile_mag);
                    Log.d("Tag","MAG fileopen");
                    file_lig=new FileOutputStream(tempfile_lig);
                    Log.d("Tag","LIG fileopen");
                    file_mag=new FileOutputStream(tempfile_pre);
                    Log.d("Tag","PRE fileopen");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                startbutton.setText("Stop");

                /*Start  listening*/
                mSensorManager.registerListener(mysensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
                mSensorManager.registerListener(mysensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
                mSensorManager.registerListener(mysensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
                mSensorManager.registerListener(mysensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_FASTEST);
                mSensorManager.registerListener(mysensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_FASTEST);
            }

        }
    };
    private void token_check()
    {
        API.check_token_valid(sharedPreferences.getString("token", null), new ResponseListener() {
            public void onResponse(JSONObject response) {

            }

            public void onErrorResponse(VolleyError error) {
                API.login(sharedPreferences.getString("account",null),sharedPreferences.getString("passwd",null), new ResponseListener(){
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(getContext(),"no token found while uploading, please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getContext(), LoginPage.class);
                        startActivity(i);
                    }
                });
            }
        });
    }
    private void fileupload(final String filepath, final String CarType, final String CarAge) throws  IOException {
        File file_dir=new File(filepath);
        File[] files = file_dir.listFiles();
        for (int i = 0; i < files.length; ++i) {
            String filename=files[i].getName();

            if (NetworkCheck.isNetworkConnected(getContext())) {
                token_check();
                final String token = sharedPreferences.getString("token", null);
                API.upload_file(filepath, token, CarType, CarAge, filename,sharedPreferences.getString("account",null), new ResponseListener() {
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
                        editor.putString(ts+"_account",sharedPreferences.getString("account",null));
                        editor.commit();
                    }
                });

            } else {
                Toast.makeText(getContext(), "no network aviable now, will upload later", Toast.LENGTH_SHORT).show();
                editor.putString(ts + "_type", CarType);
                editor.putString(ts + "_age", CarAge);
                editor.putString(ts+"_account",sharedPreferences.getString("account",null));
                editor.commit();
            }
        }
    }
    private void  close_all()
    {
        try {
            file_acc.close();
            file_gro.close();
            file_mag.close();
            file_lig.close();
            file_pre.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

