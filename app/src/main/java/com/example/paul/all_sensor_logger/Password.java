package com.example.paul.all_sensor_logger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by NOL on 2016/3/4.
 */
public class Password extends Fragment
{
    private MainActivity mMainActivity;
    private String a;
    private Button sendBtn;
    private Button backBtn;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String result="";


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
        return inflater.inflate(R.layout.fragment_layout_password, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        sendBtn = (Button)getView().findViewById(R.id.send_password);
        backBtn = (Button)getView().findViewById(R.id.back_setup);


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText oldPw = (EditText) getView().findViewById(R.id.oldpw_data);
                EditText newPw = (EditText) getView().findViewById(R.id.newpw_data);
                EditText newPw2 = (EditText) getView().findViewById(R.id.newpw2_data);

                String Pw = newPw.getText().toString();
                String Check_Pw = newPw2.getText().toString();
                String oPw = oldPw.getText().toString();

                if (Pw.compareTo(Check_Pw) == 0) {// new password is checked!
                    sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
                    editor = sharedPreferences.edit();

                    if (NetworkCheck.isNetworkConnected(mMainActivity)) {

                        String account = sharedPreferences.getString("account", null);
                        String passwd = sharedPreferences.getString("passwd", null);
                        if ((account != null) && (passwd != null)) {
                            //test auto login
                            Log.d("Tag","login start");
                            login(account, passwd, Pw);
                            Log.d("Tag","login finish");

                            //if fail , wait for user login
                        } else {
                            Toast.makeText(mMainActivity.getApplication(), "Please login first", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(mMainActivity.getApplication(), "Please connect to network", Toast.LENGTH_LONG).show();
                    }


                } else {
                    Toast.makeText(mMainActivity.getApplication(), "The retype password and the new password do not match", Toast.LENGTH_LONG).show();
                }

            }

        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetupFragment newFragment = new SetupFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_password, newFragment);
                transaction.commit();
            }

        });

    }

    void login(final String account, final String passwd, final String newPw)
    {//auto jump to mainactivity if success,if fail pops out a toast and clears the passwd textbox
        Log.d("Tag", "login btn click");
        API.login(account, passwd, new ResponseListener() {
            public void onResponse(JSONObject response) {
                try {
                    result = response.getString("result");
                    if (result.equals("login succeed")) {
                        Log.d("Tag", "login success ,start chang pw");
                        String token = sharedPreferences.getString("token", null);
                        Log.d("Tag", "token=" + token);
                        int responseCode = API.change_passwd(account, token, newPw);
                        if (responseCode == 200) {
                            editor.putString("passwd", newPw);
                            Toast.makeText(mMainActivity.getApplication(), "Password change success", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getContext(), "change password failed!!", Toast.LENGTH_SHORT).show();
                            Log.d("Tag", "Error code:" + responseCode);
                        }
                        Log.d("Tag", "login success ,end chang pw");

                    } else {
                        Toast.makeText(mMainActivity.getApplication(), "login fail, error code" + result, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void onErrorResponse(VolleyError error) {
                Log.d("Tag", "response error");
                Log.d("Tag", error.toString());
                Toast.makeText(mMainActivity.getApplication(), "login fail" + result, Toast.LENGTH_LONG).show();
            }
        });

    }

    /*void sendPw(final String Pw)
    {//auto jump to mainactivity if success,if fail pops out a toast and clears the passwd textbox

        Map<String, String> params = new HashMap<String, String>();
        params.put("passwd", Pw);
        params.put(" ", null);

        final JsonObjectRequest sendPwRequest=new JsonObjectRequest(Request.Method.POST, getString(R.string.URL)+"/api/changepw",new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    result =response.getString("result");
                    if(result.equals("Password change succeed"))
                    {
                        //login success
                        editor.putString("passwd",Pw );
                        editor.commit();
                    }
                    else
                    {
                        Toast.makeText(getActivity().getApplicationContext(),"Password change fail, error code"+result,Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Tag", "response error");
                Log.d("Tag", error.toString());
                Toast.makeText(getActivity().getApplicationContext(),"Password change fail",Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                String token = sharedPreferences.getString("token", null);
                if (token == null) {
                } else {
                    headers.put("Authorization", "Bearer" + token);
                }
                return headers;
            }
        };
        VolleyController.getInstance().addToRequestQueue(sendPwRequest);
    }*/
}
