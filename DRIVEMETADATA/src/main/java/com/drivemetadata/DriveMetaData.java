package com.drivemetadata;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.drivemetadata.callbacks.DriveMetaDataCallbacks;
import com.drivemetadata.constants.SDKRequestConstant;
import com.drivemetadata.network.HttpClient;
import com.drivemetadata.sharepreference.PreferenceUtil;
import com.drivemetadata.utils.DriveMetaDataLogger;
import com.drivemetadata.utils.Utils;
import org.json.JSONObject;

public class DriveMetaData {
    private static DriveMetaData driveMetaDataSingleton = null;
    private static Application mContext;
    private static DriveMetaDataCallbacks driveMetaDataCallbacks = null;
    private static HttpClient httpClient;
    static JSONObject requestJSONData = null;
    private static DriveMetaDataLogger driveMetaDataLogger = null;
    private static int clientId ;
    private static String clientToken ="";
    public static  int clientAppId ;
    private String requestTime ="";
    private DriveMetaDataImpl driveMetaDataImpl = null;

    private DriveMetaData(Application context, int clientId, String token, int appId) {
        DriveMetaData.mContext = context;
        DriveMetaData.clientId = clientId;
        DriveMetaData.clientAppId = appId;
        DriveMetaData.clientToken = token;
        driveMetaDataLogger = new DriveMetaDataLogger("com.drivemetadata.DriveMetadata");
        Utils.saveDriveMetaData(clientId, token, appId , context);
        httpClient = new HttpClient();
        driveMetaDataImpl = new DriveMetaDataImpl(context, driveMetaDataLogger, this,  httpClient);
        driveMetaDataImpl.handleReferralApi();
    }

    public synchronized void sendTags(String metaDataProperty,String eventType) {
        try {
            if (metaDataProperty != null) {
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(mContext);
                preferenceUtil.setBooleanData("DataReceived",true);
                requestJSONData = new JSONObject(metaDataProperty);
                executeTask(eventType);
            }else {
                if (driveMetaDataCallbacks != null)
                    driveMetaDataCallbacks.onResponse(false, "Validation error", "0");
            }
        } catch (Exception exception) {
            driveMetaDataLogger.error(exception, exception.getMessage());
        }
    }

    public void setDriveMetaDataCallbacks(DriveMetaDataCallbacks driveMetaDataCallbacks) {
        DriveMetaData.driveMetaDataCallbacks = driveMetaDataCallbacks;
    }

    void sendCallbackResponse(boolean status, String message, String requestAcknowledgementID) {
        try {
            if (driveMetaDataCallbacks != null) {
                driveMetaDataCallbacks.onResponse(status, message , requestAcknowledgementID );
            }
        }
        catch (Exception ex) {
            driveMetaDataLogger.error(ex, ex.getMessage());
        }
    }

    private void executeTask(String eventType) {
        PreferenceUtil preferenceUtil =PreferenceUtil.getInstance(mContext);
        new Handler(Looper.getMainLooper()).post(() -> {
            if(preferenceUtil.getBoolean("DataReceived")) {
                driveMetaDataImpl.performFlush(eventType, requestJSONData,clientId);
            }
            else{
                driveMetaDataLogger.debug("Response No Data Found");
            }
        });
    }

    public static DriveMetaData with(Context context) {
        if (driveMetaDataSingleton == null) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            synchronized (DriveMetaData.class) {
                if (driveMetaDataSingleton == null) {

                    PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(mContext);
                    int clientId =  preferenceUtil.getIntData(SDKRequestConstant.KEY_CLIENT_ID );
                    int appId = preferenceUtil.getIntData(SDKRequestConstant.KEY_CLIENT_APP_ID );
                    String token = preferenceUtil.getStringData(SDKRequestConstant.KEY_CLIENT_TOKEN);
                    Builder builder = new Builder(context, clientId,token ,appId);
                    driveMetaDataSingleton = builder.build();
                }
            }
        }
        return driveMetaDataSingleton;
    }

    /**
     * Set the global instance returned from {@link #with}.
     *
     * <p>This method must be called before any calls to {@link #with} and may only be called once.
     */
    public static void setSingletonInstance(DriveMetaData mDriveMetaDataSingleton) {
        synchronized (DriveMetaData.class) {
            if (driveMetaDataSingleton != null) {
                throw new IllegalStateException("Singleton instance already exists.");
            }
            driveMetaDataSingleton = mDriveMetaDataSingleton;
        }
    }

    public static class Builder {
        private  Application application;
        private  int clientId;
        private  String token;
        private  int appId;

        /**
         * Start building a new {@link DriveMetaData} instance.
         *
         *
         */
        public Builder( Context context,   int mClientId,  String mToken,  int mAppId) {
            this.application = (Application) context.getApplicationContext();
            this.clientId = mClientId;
            this.token = mToken;
            this.appId = mAppId;
        }


        /** Create a {@link DriveMetaData} client. */
        public DriveMetaData build() {
            return new DriveMetaData(
                    this.application,
                    this.clientId,
                    this.token,
                    this.appId);
        }
    }
}
