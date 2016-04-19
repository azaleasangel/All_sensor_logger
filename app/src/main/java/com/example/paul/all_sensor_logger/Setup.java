package com.example.paul.all_sensor_logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by NOL on 2016/3/7.
 */
public class Setup extends Fragment
{
    private MainActivity mMainActivity;

    private String a;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        //取得MainActivity的方法，將文字放入text字串
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //導入Tab分頁的Fragment Layout
        return inflater.inflate(R.layout.fragment_layout_setupcontainer, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog().build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor=sharedPreferences.edit();

        Button profileBtn;
        Button passwordBtn;
        Button deleteBtn;
        Button accdeleteBtn;
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor=sharedPreferences.edit();

        profileBtn = (Button)getView().findViewById(R.id.button_profile);
        passwordBtn = (Button)getView().findViewById(R.id.button_password);
        deleteBtn=(Button)getView().findViewById(R.id.delete_file_button);
        accdeleteBtn=(Button)getView().findViewById(R.id.delete_account);

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile newFragment = new Profile();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

        passwordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Password newFragment = new Password();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Environment.getExternalStorageDirectory().getPath()+"/Sensorlogger/";
                kill_all(new File(path));
                Toast.makeText(getContext(),"File delete success!!",Toast.LENGTH_SHORT).show();
            }

        });

        accdeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("This account will be delete");
                dialog.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int num) {
                            String user = sharedPreferences.getString("account", "N//A");
                            String token = sharedPreferences.getString("token", null);
                            Log.d("Tag", user);
                            Log.d("Tag", token);
                            try {
                                int responseCode = API.delete_user(user, token);
                                if (responseCode == 200) {
                                    Toast.makeText(getContext(), "User delete success!!", Toast.LENGTH_SHORT).show();
                                    String version = sharedPreferences.getString("VERSION", null);
                                    editor.clear();
                                    editor.commit();
                                    editor.putString("VERSION", version);
                                    editor.commit();
                                    Intent i = new Intent(getContext(), LoginPage.class);
                                    startActivity(i);
                                } else {
                                    Toast.makeText(getContext(), "User delete failed!!", Toast.LENGTH_SHORT).show();
                                    Log.d("Tag", "Error code:" + responseCode);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                dialog.setNegativeButton("No",
                    new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialoginterface, int i){
                            //do nothing
                        }
                    });
                dialog.show();
                /**/
            }

        });
    }
    public void kill_all(File dir)
    {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    kill_all(file);
                    file.delete();
                } else {
                    // do something here with the file
                    file.delete();
                }
            }
        }
    }
}
