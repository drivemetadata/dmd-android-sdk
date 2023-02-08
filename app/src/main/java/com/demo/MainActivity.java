package com.demo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.drivemetadata.DriveMetaData;
import com.drivemetadata.callbacks.DriveMetaDataCallbacks;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements DriveMetaDataCallbacks {

    Button btnSend;
    Button btnValidate;
    final String TAG = "com.demo.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.demo.R.layout.maint_activity);
       // permission();
        btnSend = findViewById(com.demo.R.id.permissionFIle);
        btnValidate = findViewById(com.demo.R.id.beginDebugFile);
        DriveMetaData.with(this).setDriveMetaDataCallbacks(this::onResponse);

        btnSend.setOnClickListener(view -> {
            JSONObject userDetails = new JSONObject();
            JSONObject userObject =  new JSONObject();
            try {
                userDetails.put("first_name", "testUser");
                userObject.put("userDetails",userDetails );
                DriveMetaData.with(this).sendTags(userObject.toString(),"update");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });
    }




    @Override
    public void onResponse(boolean status, String message, String requestAcknowledgementID) {
        Log.d(TAG , "status "+status+" message "+ message+" requestAcknowledgementID "+ requestAcknowledgementID);
    }
}