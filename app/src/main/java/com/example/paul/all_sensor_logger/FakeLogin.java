package com.example.paul.all_sensor_logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;


public class FakeLogin extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String result="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        else
        {
            Intent i = new Intent(getApplicationContext(), LoginPage.class);
            startActivity(i);
        }
        setContentView(R.layout.activity_fake_login);
    }
    void login(final String account, final String passwd)
    {//auto jump to mainactivity if success,if fail pops out a toast and clears the passwd textbox
        Log.d("Tag", "login btn click");
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
                        Log.d("Tag", "login success");
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "login fail, error code" + result, Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getApplicationContext(), LoginPage.class);
                        startActivity(i);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fake_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
