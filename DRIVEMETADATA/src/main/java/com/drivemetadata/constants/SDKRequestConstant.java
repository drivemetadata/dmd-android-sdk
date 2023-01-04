package com.drivemetadata.constants;

public interface SDKRequestConstant {

    // App Details
       String  APP_DETAILS_NAME = "name";
       String  APP_DETAILS_VERSION = "version";
       String  APP_DETAILS_BUILD  = "build";
       String  APP_DETAILS_BUNDLE  = "bundle";
       String  APP_DETAILS_APP_ID = "app_id";

    //Attribution Data
       String  ATTRIBUTION_DATA_CLICK_ID = "click_id";
       String  ATTRIBUTION_DATA_INSTALL_REFERRER = "install_referrer";
       String  ATTRIBUTION_DATA_SDK_VERSION = "sdk_version";
       String  ATTRIBUTION_DATA_REFERRER_CLICK_TIMESTAMP_SECONDS = "referrer_click_timestamp_seconds";
       String  ATTRIBUTION_DATA_INSTALL_BEGIN_TIMESTAMP_SECONDS = "install_begin_timestamp_seconds";
       String  ATTRIBUTION_DATA_REFERRER_CLICK_TIMESTAMP_SERVER_SECONDS = "referrer_click_timestamp_server_seconds";
       String  ATTRIBUTION_DATA_INSTALL_VERSION ="install_version";
       String  ATTRIBUTION_DATA_GOOGLE_PLAY_INSTANT ="google_play_instant";

    //Device Details
       String  DEVICE_DETAIL_DEVICE_INTERNAL_ID = "device_internal_id";
       String  DEVICE_DETAIL_GOOGLE_ADVERTISING_ID = "google_advertising_id";
       String  DEVICE_DETAIL_MAKE = "make";
       String  DEVICE_DETAIL_MODEL = "model";
       String  DEVICE_DETAIL_ANDROID_UUID = "android_uuid";
       String  DEVICE_DETAIL_PLATFORM = "platform";
       String  DEVICE_DETAIL_OS_PLATFORM_VERSION = "os_platform_version";
       String  DEVICE_DETAIL_NAME = "name";
       String  DEVICE_DETAIL_DEVICE_TYPE = "device_type";
       String  DEVICE_DETAIL_IS_MOBILE = "is_mobile";
       String  DEVICE_DETAIL_IS_TABLET = "is_tablet";
       String  DEVICE_DETAIL_CPU_ARCHITECTURE =  "cpu_architecture";
       String  DEVICE_DETAIL_AD_TRACKING_ENABLED = "ad_tracking_enabled";
       String  DEVICE_DETAIL_WIDTH = "width";
       String  DEVICE_DETAIL_HEIGHT = "height";
       String  DEVICE_DETAIL_SCREEN_DPI = "screen_dpi";
       String  DEVICE_DETAIL_SCREEN = "screen";

      // UTM Details
       String  UTM_CAMPAIGN = "utm_campaign";
       String  UTM_TERM = "utm_term";
       String  UTM_SOURCE  = "utm_source";
       String  UTM_MEDIUM  = "utm_medium";
       String  UTM_CONTENT = "utm_content";

       //Location Details
       String  LOCATION_CITY = "city";
       String  LOCATION_COUNTRY = "country";
       String  LOCATION_ISO_COUNTRY_CODE = "iso_country_code";
       String  LOCATION_REGION_CODE = "region_code";
       String  LOCATION_POSTAL_CODE = "postal_code";
       String  LOCATION_SOURCE = "location_source";
       String  LOCATION_LATITUDE = "latitude";
       String  LOCATION_LONGITUDE = "longitude";
       String  LOCATION_SPEED = "speed";

       //Network Details
       String  NETWORK_BLUETOOTH = "bluetooth";
       String  NETWORK_CARRIER = "carrier";
       String  NETWORK_CELLULAR = "cellular";
       String  NETWORK_WIFI = "wifi";

       //Preference
       String  KEY_CLIENT_ID = "clientID";
       String  KEY_CLIENT_APP_ID = "clientAppId";
       String  KEY_CLIENT_TOKEN = "clientToken";

}
