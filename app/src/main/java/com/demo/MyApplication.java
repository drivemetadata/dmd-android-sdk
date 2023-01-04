package com.demo;

import android.app.Application;

import com.drivemetadata.DriveMetaData;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DriveMetaData driveMetadata = new DriveMetaData.Builder(this, 1635,
                "4d17d90c78154c9a5569c073b67d8a5a22b2fabfc5c9415b6e7f709d68762054",3020).build();
        // Set the initialized instance as a globally accessible instance.
        DriveMetaData.setSingletonInstance(driveMetadata);
    }
}


