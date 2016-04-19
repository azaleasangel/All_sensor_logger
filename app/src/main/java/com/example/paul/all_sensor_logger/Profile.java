package com.example.paul.all_sensor_logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by NOL on 2016/3/4.
 */
public class Profile extends Fragment {
    private MainActivity mMainActivity;
    private String a;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private Spinner spinner;
    private ArrayAdapter<String> CarInfoList;
    private int pos;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //取得MainActivity的方法，將文字放入text字串
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //導入Tab分頁的Fragment Layout
        return inflater.inflate(R.layout.fragment_layout_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        spinner = (Spinner)getView().findViewById(R.id.profile_spinner);

        ArrayList<String> CarInfo = new ArrayList<>();
        String temp = "";

        check_CarInfo();

        int counter = sharedPreferences.getInt("CarInfoCounter",0);
        pos = -1;

        if(counter == 0)
        {
            Log.v("Tag", "Something wrong in profile 1");
        }
        else
        {
            for(int i = 1; i <= counter;i++)
            {
                temp = "";
                String a = "CarType" + i;
                String b = "CarAge" + i;
                String t_cartype = sharedPreferences.getString(a,null);
                String t_carage = sharedPreferences.getString(b, null);

                if("".equals(t_cartype))
                {
                    Log.v("Tag", "Something wrong in profile 2");
                }
                else if("".equals(t_carage)){
                    Log.v("Tag", "Something wrong in profile 3");
                }
                else
                {
                    temp = t_cartype+","+t_carage;
                    CarInfo.add(temp);
                }
            }
            CarInfoList = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, CarInfo);
            spinner.setAdapter(CarInfoList);
        }


        Button sendBtn = (Button) getView().findViewById(R.id.send_profile);
        Button backBtn = (Button) getView().findViewById(R.id.back_setup);
        Button saveBtn = (Button) getView().findViewById(R.id.save_profile);


        ShowCarInfo();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                  @Override
                  public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                      //Toast.makeText(mContext, "你選的是"+lunch[position], Toast.LENGTH_SHORT).show();
                      //Log.v("Tag", "Position"+position);
                      position++;
                      pos = position;

                  }

                  @Override
                  public void onNothingSelected(AdapterView<?> arg0) {
                  }
              }

        );
        saveBtn.setOnClickListener(new View.OnClickListener()

               {
                   @Override
                   public void onClick(View v) {
                       if(pos > -1) {
                           editor.putInt("CarInfoNow", pos);
                           editor.commit();
                           pos = -1;
                           ShowCarInfo();
                       }
                   }
               }
        );


        sendBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

                   LayoutInflater inflater = LayoutInflater.from(getActivity());
                   final View v1 = inflater.inflate(R.layout.popup_layout_profile, null);

                   //語法一：new AlertDialog.Builder(主程式類別).XXX.XXX.XXX;
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
                                       return;
                                   }
                                   if ("".equals(Pop_carage)) {
                                       Toast.makeText(getContext(), "Car age can't be empty", Toast.LENGTH_LONG).show();
                                       return;
                                   }

                                   int CarInfoCounter = sharedPreferences.getInt("CarInfoCounter", 0);
                                   if (CarInfoCounter == 0) {
                                       Log.v("Tag", "Something wrong in profile 2");
                                   } else {
                                       CarInfoCounter++;

                                       String a = "CarType" + CarInfoCounter;
                                       String b = "CarAge" + CarInfoCounter;

        /*Log.v("Tag", a);
        Log.v("Tag", b);*/

                                       editor.putInt("CarInfoCounter", CarInfoCounter);
                                       editor.putString(a, Pop_cartype);
                                       editor.putString(b, Pop_carage);
                                       editor.commit();

                                       Toast.makeText(getContext(), "CarInfo add success, please refresh is page", Toast.LENGTH_LONG).show();

                                   }

                               }
                           })
                           .show();
               }

           }

        );
        backBtn.setOnClickListener(new View.OnClickListener()

                                   {
                                       @Override
                                       public void onClick(View v) {
               SetupFragment newFragment = new SetupFragment();
               FragmentTransaction transaction = getFragmentManager().beginTransaction();
               transaction.replace(R.id.fragment_profile, newFragment);
               transaction.commit();
           }

       }

        );


    }
    void ShowCarInfo()
    {
        int Now_CarInfo = sharedPreferences.getInt("CarInfoNow",0);
        TextView carType = (TextView) getView().findViewById(R.id.cartype_data);
        TextView carAge = (TextView) getView().findViewById(R.id.carage_data);

        String CarType = "";
        String CarAge = "";
        String a = "CarType" + Now_CarInfo;
        String b = "CarAge" + Now_CarInfo;
        CarType = sharedPreferences.getString(a, null);
        CarAge = sharedPreferences.getString(b, null);
        Log.v("Tag", CarType);
        Log.v("Tag", CarAge);

        if (CarType != null && CarAge != null) {
            carType.setText(CarType);
            carType.setTextSize(20);
            carAge.setText(CarAge);
            carAge.setTextSize(20);
        } else {
            Log.v("Tag", "Something wrong in profile");
        }

        Log.v("Tag", "Car info in progile" + Now_CarInfo);
    }

    void check_CarInfo()
    {
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

}
