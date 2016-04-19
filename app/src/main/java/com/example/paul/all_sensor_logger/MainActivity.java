package com.example.paul.all_sensor_logger;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;

public class MainActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*deal with Layout*/
        //get contorl of TabHost
        FragmentTabHost mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.container);
        mTabHost.addTab(mTabHost.newTabSpec("one")
                .setIndicator("Main")
                , MainFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("two")
                .setIndicator("Setup")
                ,SetupFragment.class,null);

    }
    public void onDestroy(){
        super.onDestroy();
        Intent intent = new Intent(MainActivity.this,NetworkCheckService.class);
        stopService(intent);

    }

}

