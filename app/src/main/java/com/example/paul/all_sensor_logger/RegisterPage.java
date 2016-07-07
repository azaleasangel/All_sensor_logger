package com.example.paul.all_sensor_logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nullwire.trace.ExceptionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterPage extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExceptionHandler.register(this, "http://nol.cs.nctu.edu.tw/~pstsao/server.php");
        setContentView(R.layout.activity_register_page);
        sharedPreferences = getSharedPreferences(getString(R.string.PREFS_NAME),0);
        editor=sharedPreferences.edit();
    }

    public void sumit_click(final View view) {
        Log.v("Tag", "submit click");
        final String account =((EditText)findViewById(R.id.account)).getText().toString();
        String passwd=((EditText)findViewById(R.id.passwd)).getText().toString().trim();
        String passwd2=((EditText)findViewById(R.id.passwd2)).getText().toString().trim();
        String CarType=((EditText)findViewById(R.id.cartype)).getText().toString();
        String CarAge=((EditText)findViewById(R.id.carage)).getText().toString();
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(account).matches())
        {
            Log.v("Tag","bad account");
            Toast.makeText(view.getContext(), "account is a invaild email", Toast.LENGTH_LONG).show();
            return;
        }
        if(passwd==null||passwd.isEmpty())
        {
            Log.v("Tag","no passwd");
            Toast.makeText(view.getContext(),"password can't be blank",Toast.LENGTH_LONG).show();
            return;
        }

        if(passwd2==null||passwd2.isEmpty())
        {
            Log.v("Tag", "no passwd2");
            Toast.makeText(view.getContext(),"please comfirm password",Toast.LENGTH_LONG).show();
            return;
        }
        if(!passwd.equals(passwd2))
        {
            Log.v("Tag","passwd not equal");
            Log.v("Tag","passwd"+passwd+".");
            Log.v("Tag","passwd2"+passwd2+".");
            Toast.makeText(view.getContext(),"two passwd different, please type again",Toast.LENGTH_LONG).show();
            ((EditText)findViewById(R.id.passwd)).setText("");
            ((EditText)findViewById(R.id.passwd2)).setText("");
            return;

        }
        if("".equals(CarType))
        {
            Toast.makeText(view.getContext(),"Car type can't be empty",Toast.LENGTH_LONG).show();
            return;
        }
        else if("".equals(CarAge))
        {
            Toast.makeText(view.getContext(),"Car age can't be empty",Toast.LENGTH_LONG).show();
            return;
        }
        else
        {
            Log.v("Tag","save car info");
            Log.v("Tag",CarType);
            Log.v("Tag",CarAge);
            editor.putString("CarType1", CarType);
            editor.putString("CarAge1", CarAge);
            editor.putInt("CarInfoCounter", 1);
            editor.putInt("CarInfoNow", 1);
            editor.commit();
        }
        //TODO encrypt pw
        final String encry_pw=passwd;
        //sent to server

       API.creste_user(account,encry_pw,new ResponseListener(){
           public void onResponse(JSONObject response)
           {
               try {
                   String result =response.getString("result");
                   if(result.equals("create users success"))
                   {
                       Toast.makeText(view.getContext(),"account created successfully,switching back to login page",Toast.LENGTH_LONG).show();
                       Intent i = new Intent(getApplicationContext(), LoginPage.class);
                       i.putExtra("account",account);
                       Log.d("Tag",account);
                       startActivity(i);
                       finish();
                   }
               } catch (JSONException e) {
                   e.printStackTrace();
                   Log.d("Tag:Create", e.getMessage());
               }
           }

           public void onErrorResponse(VolleyError error){
               Toast.makeText(view.getContext(),"account created error",Toast.LENGTH_LONG).show();
               JSONObject response= null;
               try {
                   response = new JSONObject(new String(error.networkResponse.data));
                   Toast.makeText(view.getContext(),response.getJSONObject("data").getString("account"),Toast.LENGTH_LONG).show();
               } catch (JSONException e) {
                   e.printStackTrace();
               }

           }
       });
    }
}
