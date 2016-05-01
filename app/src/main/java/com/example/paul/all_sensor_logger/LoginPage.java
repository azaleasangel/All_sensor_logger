package com.example.paul.all_sensor_logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class LoginPage extends AppCompatActivity {
    //login page, this page should only be seen by uses that
    //  1.loged out 2.haven't logged in before 3.password change after last login
    // in other cases, user will be redircted to main page
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String result="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this,NetworkCheckService.class);
        startService(intent);



        sharedPreferences = getSharedPreferences(getString(R.string.PREFS_NAME),0);
        editor=sharedPreferences.edit();

        if(NetworkCheck.isNetworkConnected(this)){

            String account=sharedPreferences.getString("account",null);
            String passwd=sharedPreferences.getString("passwd",null);
            if((account!=null)&&(passwd!=null))
            {
                //test auto login
                login(account,passwd);
                    //if fail , wait for user login
                }
        }
        else{
            //test fake login
            if(offline_login())
            {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "please connect to network and try again" + result, Toast.LENGTH_LONG).show();
              //  ( (Button)findViewById(R.id.loginbtn)).setEnabled(false);

            }
        }

        setContentView(R.layout.activity_loginpage);

        //check if just created account, auto fill in
        Bundle extras= getIntent().getExtras();
        if(extras!=null) {
            Log.d("Tag",extras.getString("account", ""));
            ((EditText) findViewById(R.id.account)).setText(extras.getString("account", ""));
        }
    }

    boolean offline_login()
    {
        String account=sharedPreferences.getString("account",null);
        String passwd=sharedPreferences.getString("passwd",null);
        Log.d("Tag","fake login");
        return (!(account==null)||(passwd==null));
    }

    public void login_click(final View view) {
       // Toast.makeText(view.getContext(),"loading...",Toast.LENGTH_SHORT).show();
        //check ac&pw not empty
        final String account =((EditText)findViewById(R.id.account)).getText().toString();
        String passwd=((EditText)findViewById(R.id.passwd)).getText().toString().trim();
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(account).matches())
        {
            Toast.makeText(view.getContext(),"account is a invaild email",Toast.LENGTH_LONG).show();
            return;
        }
        if(passwd.isEmpty()||passwd==null)
        {
            Toast.makeText(view.getContext(),"password can't be blank",Toast.LENGTH_LONG).show();
            return;
        }
        String key = "this is a key";
        SecretKey secret = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = null;
        byte[] cipherText = new byte[0];
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            cipherText = cipher.doFinal(passwd.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        final String encry_pw = new String(cipherText);
        //sent to server

        login(account,encry_pw);
    }

    public void create_click(View view) {
        //switch to register page
        Intent i = new Intent(getApplicationContext(), RegisterPage.class);
        startActivity(i);
    }

    public void account_click(View view) {
        ((EditText) findViewById(R.id.account)).setText("");
    }

    void login(final String account, final String passwd)
    {//auto jump to mainactivity if success,if fail pops out a toast and clears the passwd textbox
        Log.d("Tag","login btn click");
        API.login(account, passwd, new ResponseListener() {
            public void onResponse(JSONObject response) {
                try {
                    result = response.getString("result");
                    if (result.equals("login succeed")) {
                        //login success
                        String token = response.getJSONObject("data").getString("token");
                        editor.putString("token", token);
                        editor.putString("account", account);
                        editor.putString("passwd", passwd);
                        editor.commit();
                        Log.d("Tag","login success");
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "login fail, error code" + result, Toast.LENGTH_LONG).show();
                        ((EditText) findViewById(R.id.passwd)).setText("");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onErrorResponse(VolleyError error) {
                Log.d("Tag", "response error");
                Log.d("Tag", error.toString());
                Toast.makeText(getApplicationContext(), "login fail", Toast.LENGTH_LONG).show();
                ((EditText) findViewById(R.id.passwd)).setText("");
            }
        });

    }
}
