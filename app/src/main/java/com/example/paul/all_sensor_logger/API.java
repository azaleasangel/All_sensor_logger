package com.example.paul.all_sensor_logger;

import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by PsTsao on 2016/3/27.
 */
public class API {

    static  private String aURL="http://cswwwdev.cs.nctu.edu.tw:7122";

    public static  void get_argeement_version(final ResponseListener res)
    {
        JsonObjectRequest versionRequest=new JsonObjectRequest(Request.Method.GET, aURL+"/api/agreements/version", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                res.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               res.onErrorResponse(error);
            }
        });
        VolleyController.getInstance().addToRequestQueue(versionRequest);
    }

    public static  void get_argeement_content(final ResponseListener res)
    {
        final JsonObjectRequest contentRequest=new JsonObjectRequest(Request.Method.GET,aURL+"/api/agreements/data", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                res.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               res.onErrorResponse(error);
            }
        });
        VolleyController.getInstance().addToRequestQueue(contentRequest);
    }

    public  static void login(final String account,final String passwd,final ResponseListener res)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account);
        params.put("passwd", passwd);
        final JsonObjectRequest loginRequest=new JsonObjectRequest(Request.Method.POST, aURL+"/api/login",new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                res.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                res.onErrorResponse(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
        VolleyController.getInstance().addToRequestQueue(loginRequest);
    }

    public  static  void creste_user(final  String account,final  String passwd, final ResponseListener res)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account);
        params.put("passwd", passwd);
        params.put("auth_source", "0");

        JSONObject jsonObject = new JSONObject(params);
        final JsonObjectRequest createrequest=new JsonObjectRequest(Request.Method.POST,aURL+"/api/users",jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
               res.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                res.onErrorResponse(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
        VolleyController.getInstance().addToRequestQueue(createrequest);
    }

    public  static  void upload_file(final String filepath,final String token,final String CarType,final String CarAge,final String filename,final String account, final ResponseListener res)
    {
        FileInputStream file_acc_in = null;
        try {
            file_acc_in = new FileInputStream(filepath+"/"+filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder();
        int ch;
        try {
            while ((ch = file_acc_in.read()) != -1) {
                builder.append((char) ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String filecontent = builder.toString();
        JSONObject meta_data=new JSONObject();
        JSONObject params=new JSONObject();
        try {
            meta_data.put("CarAge",CarAge);
            meta_data.put("CarType",CarType);
            meta_data.put("account",account);
            params.put("data", filecontent);
            params.put("meta",meta_data);
            params.put("filename", filename);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        final JsonObjectRequest senddata = new JsonObjectRequest(Request.Method.POST, aURL + "/api/data", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
               res.onResponse(response);
            }
        },
            new Response.ErrorListener() {
                @Override
            public void onErrorResponse(VolleyError error) {
                res.onErrorResponse(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Authorization", "Bearer " + token);
               /* headers.put("CarType", CarType);
                headers.put("CarAge", CarAge);*/
                return headers;
            }
        };
        VolleyController.getInstance().addToRequestQueue(senddata);
    }

    public static void check_token_valid(final String token, final ResponseListener res)
    {
        final JsonObjectRequest check_token = new JsonObjectRequest(Request.Method.GET, aURL + "/api/auth",new JSONObject() , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                res.onResponse(response);
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                res.onErrorResponse(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleyController.getInstance().addToRequestQueue(check_token);

    }
    public static  int delete_user(final String user,final String token) throws IOException {
        URL url = new URL(aURL+"/api/users/"+user);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        httpCon.setRequestProperty("Authorization", "Bearer " + token);
        httpCon.setRequestMethod("DELETE");
        int responseCode = httpCon.getResponseCode();
        Log.d("Tag", "code:" + responseCode);

        return responseCode;
    }

    public static  int change_passwd(final String account,final String token,final String pw) throws IOException {
        URL url = new URL(aURL+"/api/users/"+account);
        //URL url = new URL("http://requestb.in/1mv3slj1");
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        httpCon.setRequestProperty("Authorization", "Bearer " + token);
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        String data = "{\"account\":\""+account + "\",\"new_passwd\":\""+pw+"\"}";
        Log.d("Tag", "data:"+data);
        out.write(data);
        out.flush();
        out.close();
        int responseCode = httpCon.getResponseCode();
        Log.d("Tag", "code:"+responseCode);
        return responseCode;
    }

}
