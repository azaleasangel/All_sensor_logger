package com.example.paul.all_sensor_logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class AgreementPage extends AppCompatActivity {
    //if this page is entered, assume there is  internet connection
    //if not , we are in big problem...(will still check any way)
    //after argee is clicked, it should return to previous activity
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String version="";
    @Override
    public void onBackPressed() {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Tag","start areggment page");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreementpage);

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        if(NetworkCheck.isNetworkConnected(this)) {
            API.get_argeement_content(new ResponseListener() {
                public void onResponse(JSONObject response){
                    try {
                        TextView content = (TextView) findViewById(R.id.content);
                        content.setText(response.getString("data"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                public void onErrorResponse(VolleyError error) {
                    TextView content = (TextView) findViewById(R.id.content);
                    content.setText("can't get contract");
                }
            });

            API.get_argeement_version(new ResponseListener(){
                public void onResponse(JSONObject response) {
                    try {
                        version =response.getString("version");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                public void onErrorResponse(VolleyError error) {
                    ((Button)findViewById(R.id.argee)).setText("can't get contract version");
                }
            });
        }
        else
        {
            //cry, we do not have internet access
            TextView content=(TextView)findViewById(R.id.content);
            content.setText("no internet access");
            //((Button)findViewById(R.id.argee)).setEnabled(false);
        }

    }

    public void argee_click(View view) {
        Log.d("AgreementPage", version);
        editor.putString("VERSION", version);
        editor.commit();
        this.finish();
        Log.d("Tag","argeementpage end");
    }

    public void disargee_click(View view) {
        //exit app
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
        System.exit(0);
    }
}
