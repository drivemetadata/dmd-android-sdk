package com.demo;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.drivemetadata.DriveMetaData;
import com.drivemetadata.callbacks.DriveMetaDataCallbacks;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements DriveMetaDataCallbacks {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    Button btnSend;
    Button btnValidate;
    final String TAG = "com.demo.MainActivity";

    @RequiresApi(api = Build.VERSION_CODES.O)
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

    public void permission(){
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return ;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // Permission Denied
                    Toast.makeText( this,"your message" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onResponse(boolean status, String message, String requestAcknowledgementID) {
        Log.d(TAG , "status "+status+" message "+ message+" requestAcknowledgementID "+ requestAcknowledgementID);
    }
}