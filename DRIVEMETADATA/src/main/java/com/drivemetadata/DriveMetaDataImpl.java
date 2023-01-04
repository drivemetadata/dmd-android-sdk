package com.drivemetadata;

import static com.drivemetadata.utils.Utils.CLICK_ID;
import static com.drivemetadata.utils.Utils.INSTALL_APP_REFER_INSTALL_TIME;

import android.content.Context;
import android.os.RemoteException;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.drivemetadata.constants.SDKConstant;
import com.drivemetadata.constants.SDKRequestConstant;
import com.drivemetadata.network.HttpClient;
import com.drivemetadata.network.HttpResponseHandler;
import com.drivemetadata.network.URLEndPoint;
import com.drivemetadata.sharepreference.PreferenceUtil;
import com.drivemetadata.utils.DriveMetaDataLogger;
import com.drivemetadata.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.UUID;

public class DriveMetaDataImpl {

    private Context context;
    private DriveMetaDataLogger driveMetaDataLogger;
    private DriveMetaData driveMetaData;
    private HttpClient httpClient;

    DriveMetaDataImpl(Context mContext , DriveMetaDataLogger mdriveMetaDataLogger, DriveMetaData mDriveMetaData , HttpClient mHttpClient){
        this.context = mContext;
        this.driveMetaDataLogger = mdriveMetaDataLogger;
        this.driveMetaData = mDriveMetaData;
        this.httpClient = mHttpClient;
    }

    //Function to send data to server
    protected void uploadDataToServer(JSONObject jsonData) {
        if(jsonData!=null) {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(this.context);
            driveMetaDataLogger.debug("JSONObject : "+ jsonData.toString());

            httpClient.process(URLEndPoint.DATA_COLLECTOR, jsonData.toString(), false, new HttpResponseHandler() {
                public void handleResponse(String response, Object context) {
                    if (response != null) {
                        driveMetaDataLogger.debug("response : "+ response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Boolean success = jsonObject.getBoolean("success");
                            String message = jsonObject.getString("message");
                            if(!success){
                                driveMetaData.sendCallbackResponse(success, message , SDKConstant.SDK_DEFAULT_REQUEST_ACKNOWLEDGEMENT_ID);
                                return;
                            }

                            String requestAcknowledgementID = jsonObject.getString("requestAcknowledgementID");
                            driveMetaData.sendCallbackResponse(success, message , requestAcknowledgementID);
                        } catch (JSONException ex) {
                            driveMetaData.sendCallbackResponse(false, "Connection failure" ,SDKConstant.SDK_DEFAULT_REQUEST_ACKNOWLEDGEMENT_ID);
                            driveMetaDataLogger.error(ex, ex.getMessage());
                        }
                    } else {
                        driveMetaData.sendCallbackResponse(false, "Connection failure" , SDKConstant.SDK_DEFAULT_REQUEST_ACKNOWLEDGEMENT_ID);
                    }

                }

                public void handleHTTPError(int responseCode, HashMap<String, String> headers, Object context) {
                    driveMetaData.sendCallbackResponse(false, "Connection failure", SDKConstant.SDK_DEFAULT_REQUEST_ACKNOWLEDGEMENT_ID);
                }

                public void handleError(Exception e, String msg, Object context) {
                    driveMetaData.sendCallbackResponse(false, "Connection failure", SDKConstant.SDK_DEFAULT_REQUEST_ACKNOWLEDGEMENT_ID);
                }
            }, context);

        }
    }

    protected void performFlush(String eventType , JSONObject requestJSONData , int clientId) {
        String requestTime = Utils.RequestDataTime();
        Thread thread = new Thread(() -> {
            try {
                try {
                    requestJSONData.put(SDKConstant.SDK_IP, Utils.getIPAddress());
                    requestJSONData.put(SDKConstant.SDK_DEVICE_DETAILS, Utils.retrieveDeviceDetails(context));
                    requestJSONData.put(SDKConstant.SDK_ATTRIBUTION_DATA, Utils.getAttributionData());
                    requestJSONData.put(SDKConstant.SDK_APP_DETAILS,Utils.getAppDetails(context));
                    requestJSONData.put(SDKConstant.SDK_UTM_PARAMETER,Utils.getUTMParameter(context));
                    requestJSONData.put(SDKConstant.SDK_LOCATION,Utils.getLocation(context));
                    requestJSONData.put(SDKConstant.SDK_LIBRARY_NAME,Utils.getLibraryDetails(context));
                    requestJSONData.put(SDKConstant.SDK_NETWORK,Utils.getNetworkDetails(context));
                    requestJSONData.put(SDKConstant.SDK_USER_IDENTIFIER,Utils.userIdentifier(context));
                    requestJSONData.put(SDKConstant.SDK_USER_AGENT,"");
                    requestJSONData.put(SDKConstant.SDK_REQUEST_ID, UUID.randomUUID().toString());
                    requestJSONData.put(SDKConstant.SDK_REQUEST_RECEIVED_AT,requestTime);
                    requestJSONData.put(SDKConstant.SDK_REQUEST_SENT_AT,Utils.RequestDataTime());
                    requestJSONData.put(SDKConstant.SDK_TIME_STAMP,Utils.RequestDataTime());
                    requestJSONData.put(SDKConstant.SDK_EVENT_TYPE,eventType);
                    requestJSONData.put(SDKConstant.SDK_REQUEST_FORM,"1");
                    requestJSONData.put(SDKConstant.SDK_CLIENT_TOKEN,context);
                    requestJSONData.put(SDKConstant.SDK_CLIENT_ID,clientId);
                    requestJSONData.put(SDKConstant.SDK_LANGUAGE,"en-us");
                    PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                    if(!isRequestJSONValid(requestJSONData)){
                        return;
                    }

                    if(!preferenceUtil.getBoolean(SDKConstant.SDK_IS_FIRST_INSTALL)) {
                        if (requestJSONData != null) {
                            preferenceUtil.setBooleanData(SDKConstant.SDK_IS_FIRST_INSTALL,true);
                            JSONObject request = new JSONObject();
                            request.put(SDKConstant.SDK_META_DATA, requestJSONData);
                            uploadDataToServer(request);
                        }
                    }
                    else {
                        if(!eventType.equalsIgnoreCase(SDKConstant.SDK_APP_INSTALL)) {
                            JSONObject request = new JSONObject();
                            request.put(SDKConstant.SDK_META_DATA, requestJSONData);
                            uploadDataToServer(request);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    driveMetaDataLogger.error(e, "Exception occurred while initializing install referrer");
                } finally {

                }
            } catch (Exception e) {
                driveMetaDataLogger.error(e, "Exception occurred while initializing install referrer");
            }
        });

        thread.start();
    }


    protected void handleReferralApi() {
     driveMetaDataLogger.debug("Test handleReferralApi() ");
        InstallReferrerClient  referrerClient = InstallReferrerClient.newBuilder(context).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        ReferrerDetails response = null;
                        try {

                            response = referrerClient.getInstallReferrer();
                            String referrerUrl = response.getInstallReferrer();
                            Utils.INSTALL_APP_REFER_CLICK_TIME = response.getReferrerClickTimestampSeconds();
                            INSTALL_APP_REFER_INSTALL_TIME = response.getInstallBeginTimestampSeconds();
                            Utils.INSTALL_REFERRER = referrerUrl;
                            // utm parameter fetching
                            if (referrerUrl != null && !referrerUrl.isEmpty()) {
                                Utils.utm_medium = Utils.extractValueFromQueryStringAndKey(SDKConstant.SDK_UTM_MEDIUM, referrerUrl);
                                Utils.utm_campaign = Utils.extractValueFromQueryStringAndKey(SDKConstant.SDK_UTM_CAMPAIGN, referrerUrl);
                                Utils.utm_source = Utils.extractValueFromQueryStringAndKey(SDKConstant.SDK_UTM_SOURCE, referrerUrl);
                                Utils.utm_content = Utils.extractValueFromQueryStringAndKey(SDKConstant.SDK_UTM_CONTENT, referrerUrl);
                                Utils.utm_content = Utils.extractValueFromQueryStringAndKey(SDKConstant.SDK_UTM_TERM, referrerUrl);
                                Utils.CLICK_ID = Utils.extractValueFromQueryStringAndKey(SDKConstant.SDK_CLICKED_ID, referrerUrl);
                                driveMetaDataLogger.debug(SDKConstant.SDK_CLICKED_ID + CLICK_ID + referrerUrl);
                            }
                        }catch(RemoteException e){
                            // handling error case.
                            if (driveMetaDataLogger != null) {
                                driveMetaDataLogger.error(e, "Exception occurred while initializing install referrer");
                            }
                        }
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        driveMetaDataLogger.debug("Feature not supported..");
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        driveMetaDataLogger.debug("Fail to establish connection");
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                driveMetaDataLogger.debug("Service disconnected..");
            }
        });
    }


    /*
        This method will validate mandatory key and value in requestJSONData
        {
        "metaData": {
            "appDetails": {
            "name": "minecraftpe",
            "version": "545",
            "build": "3.0.1.545",
            "bundle": "com.mojang.minecraftpe",
            "apap_id": 1835
            },
            "attributionData": {
            "click_id": "{{click_id_value}}",
            "install_referrer": "{{referrer}}",
            "sdk_version": "3.2",
            "referrer_click_timestamp_seconds": "",
            "install_begin_timestamp_seconds": "",
            "referrer_click_timestamp_server_seconds": "",
            "install_begin_timestamp_server_seconds": "",
            "install_version": "",
            "google_play_instant": ""
        },

        "device": {
            "device_internal_id": "{{$guid}}",
            "google_advertising_id": "{{$guid}}",
            "ad_tracking_enabled": true,
            "make": "Apple",
            "model": "iPhone7,2",
            "platform": "macOS",
            "os_platform_version": "10.15",
            "name": "maguro",
            "device_type": "Desktop",
            "is_mobile": false,
            "is_tablet": false,
            "android_uuid": "",
            "cpu_architecture": "arm64",
            "screen": {
                "width": 320,
                "height": 568,
                "screen_dpi": 2
            }
        },
        "ip": "{{ipv4}}",
        "ua": "{{ua}}",
        "requestId": "{{$guid}}",
        "requestReceivedAt": "{{currentdate}}",
        "requestSentAt": "{{currentdate}}",
        "timestamp": "{{currentdate}}",
        "eventType": "install",
        "requestFrom": "1",
        "token": "4d17d90c78154c9a5569c073b67d8a5a22b2fabfc5c9415b6e7f709d68762054",
        "clientId": 1635
        }
    }

    */



    protected boolean isRequestJSONValid(JSONObject requestJSONData) throws JSONException {

        StringBuilder missingParameter = new StringBuilder();
        boolean isRequestJSONValid = true;

        if(requestJSONData.toString().isEmpty()){
            driveMetaData.sendCallbackResponse(false, "Input Json is missing" ,SDKConstant.SDK_DEFAULT_REQUEST_ACKNOWLEDGEMENT_ID);
            return false;
        }

        //App Details
        if(!requestJSONData.has(SDKConstant.SDK_APP_DETAILS) || requestJSONData.isNull(SDKConstant.SDK_APP_DETAILS)){
            isRequestJSONValid = false;
            missingParameter.append("App Details Missing ");
        }
        if(!requestJSONData.getJSONObject(SDKConstant.SDK_APP_DETAILS).has(SDKRequestConstant.APP_DETAILS_APP_ID) ||
                requestJSONData.getJSONObject(SDKConstant.SDK_APP_DETAILS).getString(SDKRequestConstant.APP_DETAILS_APP_ID).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("App Id Missing ");
        }

        //Attribution Data
        if(!requestJSONData.has(SDKConstant.SDK_ATTRIBUTION_DATA) || requestJSONData.isNull(SDKConstant.SDK_ATTRIBUTION_DATA)){
            isRequestJSONValid = false;
            missingParameter.append("Attribution Data Missing ");
        }
        if(!requestJSONData.getJSONObject(SDKConstant.SDK_ATTRIBUTION_DATA).has(SDKRequestConstant.ATTRIBUTION_DATA_CLICK_ID) ||
                requestJSONData.getJSONObject(SDKConstant.SDK_ATTRIBUTION_DATA).getString(SDKRequestConstant.ATTRIBUTION_DATA_CLICK_ID).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("Click ID Missing ");
        }
        if(!requestJSONData.getJSONObject(SDKConstant.SDK_ATTRIBUTION_DATA).has(SDKRequestConstant.ATTRIBUTION_DATA_INSTALL_REFERRER) ||
                requestJSONData.getJSONObject(SDKConstant.SDK_ATTRIBUTION_DATA).getString(SDKRequestConstant.ATTRIBUTION_DATA_INSTALL_REFERRER).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("Install Referrer Missing ");
        }
        if(!requestJSONData.getJSONObject(SDKConstant.SDK_ATTRIBUTION_DATA).has(SDKRequestConstant.ATTRIBUTION_DATA_SDK_VERSION) ||
                requestJSONData.getJSONObject(SDKConstant.SDK_ATTRIBUTION_DATA).getString(SDKRequestConstant.ATTRIBUTION_DATA_SDK_VERSION).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("SDK Version Missing ");
        }
        if(!requestJSONData.has(SDKConstant.SDK_DEVICE_DETAILS) || requestJSONData.isNull(SDKConstant.SDK_DEVICE_DETAILS)){
            isRequestJSONValid = false;
            missingParameter.append("Device Details Missing ");
        }
        if(!requestJSONData.getJSONObject(SDKConstant.SDK_DEVICE_DETAILS).has(SDKRequestConstant.DEVICE_DETAIL_DEVICE_INTERNAL_ID) ||
                requestJSONData.getJSONObject(SDKConstant.SDK_DEVICE_DETAILS).getString(SDKRequestConstant.DEVICE_DETAIL_DEVICE_INTERNAL_ID).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("Device Internal Id Missing ");
        }
        if(!requestJSONData.getJSONObject(SDKConstant.SDK_DEVICE_DETAILS).has(SDKRequestConstant.DEVICE_DETAIL_GOOGLE_ADVERTISING_ID) ||
                requestJSONData.getJSONObject(SDKConstant.SDK_DEVICE_DETAILS).getString(SDKRequestConstant.DEVICE_DETAIL_GOOGLE_ADVERTISING_ID).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("Device Google Adv Id Missing ");
        }
        if(!requestJSONData.has(SDKConstant.SDK_IP) ||
                requestJSONData.getString(SDKConstant.SDK_IP).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("Device IP Missing ");
        }
        if(!requestJSONData.has(SDKConstant.SDK_USER_AGENT) ||
                requestJSONData.getString(SDKConstant.SDK_USER_AGENT).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("UA Missing ");
        }
        if(!requestJSONData.has(SDKConstant.SDK_REQUEST_ID) ||
                requestJSONData.getString(SDKConstant.SDK_REQUEST_ID).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("RequestId Missing ");
        }
        if(!requestJSONData.has(SDKConstant.SDK_REQUEST_RECEIVED_AT) ||
                requestJSONData.getString(SDKConstant.SDK_REQUEST_RECEIVED_AT).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("Request Received At Missing ");
        }
        if(!requestJSONData.has(SDKConstant.SDK_REQUEST_SENT_AT) ||
                requestJSONData.getString(SDKConstant.SDK_REQUEST_SENT_AT).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("RequestSentAt Missing ");
        }
        if(!requestJSONData.has(SDKConstant.SDK_TIME_STAMP) ||
                requestJSONData.getString(SDKConstant.SDK_TIME_STAMP).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("timestamp Missing ");
        }
        if(!requestJSONData.has(SDKConstant.SDK_EVENT_TYPE) ||
                requestJSONData.getString(SDKConstant.SDK_EVENT_TYPE).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("Event Type Missing ");
        }
        if(!requestJSONData.has(SDKConstant.SDK_REQUEST_FORM) ||
                requestJSONData.getString(SDKConstant.SDK_REQUEST_FORM).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("Request From Missing ");
        }
        if(!requestJSONData.has(SDKConstant.SDK_CLIENT_TOKEN) ||
                requestJSONData.getString(SDKConstant.SDK_CLIENT_TOKEN).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("Token Missing ");
        }
        if(!requestJSONData.has(SDKConstant.SDK_CLIENT_ID) ||
                requestJSONData.getString(SDKConstant.SDK_CLIENT_ID).isEmpty()){
            isRequestJSONValid = false;
            missingParameter.append("Client ID Missing ");
        }

        if(!isRequestJSONValid)
            driveMetaData.sendCallbackResponse(false, missingParameter.toString() ,SDKConstant.SDK_DEFAULT_REQUEST_ACKNOWLEDGEMENT_ID);

        return isRequestJSONValid;
    }
}
