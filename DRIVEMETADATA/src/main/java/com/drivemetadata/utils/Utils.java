package com.drivemetadata.utils;



import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drivemetadata.DriveMetaData;
import com.drivemetadata.constants.SDKRequestConstant;
import com.drivemetadata.location.GPSTracker;
import com.drivemetadata.sharepreference.PreferenceUtil;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    public static String CLICK_ID = "";
    public static String INSTALL_REFERRER = "";
    public static String SDK_VERSION = "1.0.7";
    public static long INSTALL_APP_REFER_INSTALL_TIME;
    public static long INSTALL_APP_REFER_CLICK_TIME;
    public static  String utm_campaign ="";
    public static  String utm_content ="";
    public static  String utm_medium ="";
    public static  String utm_source ="";
    public static  String utm_term ="";
    public static final String THREAD_PREFIX = "test-";

    private static final int PERMISSION_CHECK_REPEAT_MAX_COUNT = 2;
    public static boolean isNotNull(String value) {
        return value != null;
    }


    public static void saveDriveMetaData(int clientId, String token, int appId , Context context ) {
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        preferenceUtil.setIntData(SDKRequestConstant.KEY_CLIENT_ID, clientId);
        preferenceUtil.setIntData(SDKRequestConstant.KEY_CLIENT_APP_ID, appId);
        preferenceUtil.setStringData(SDKRequestConstant.KEY_CLIENT_TOKEN, token);
    }

    //fetching public IP
    public static String getIPAddress() {
        try {
            URL ip = new URL("https://checkip.amazonaws.com");
            BufferedReader br = new BufferedReader(new InputStreamReader(ip.openStream()));
            String pip = br.readLine();
            System.out.println("Public/External IP Address = " +pip);
            return pip;
        }
        catch(Exception e)
        {
            System.out.println("Exception: " +e);
        }
        return "";
    }

    public static String getDeviceName() {
        return Build.MANUFACTURER;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getCpuArch() {
        return System.getProperty("os.arch");
    }

    public static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release + ")";
    }

    private static String getDeviceID(Context mContext) {
        String android_id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        return md5(android_id).toUpperCase();
    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isTablet(Context ctx) {
        return (ctx.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static JSONObject retrieveDeviceDetails(Context context) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_DEVICE_INTERNAL_ID, getDeviceID(context));
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_GOOGLE_ADVERTISING_ID, Utils.adID(context));
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_MAKE, getDeviceName());
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_MODEL, getDeviceModel());
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_ANDROID_UUID,getDeviceID(context));
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_PLATFORM, "android");
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_OS_PLATFORM_VERSION, getAndroidVersion());
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_NAME, getDeviceName());
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_DEVICE_TYPE, "Mobile");
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_IS_MOBILE, !isTablet(context));
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_IS_TABLET, isTablet(context));
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_CPU_ARCHITECTURE, getCpuArch());
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_AD_TRACKING_ENABLED, true);
            JSONObject deviceResolution = new JSONObject();
            deviceResolution.put(SDKRequestConstant.DEVICE_DETAIL_WIDTH, getScreenWidth());
            deviceResolution.put(SDKRequestConstant.DEVICE_DETAIL_HEIGHT, getScreenHeight());
            deviceResolution.put(SDKRequestConstant.DEVICE_DETAIL_SCREEN_DPI, getDeviceDPI());
            jsonObject.put(SDKRequestConstant.DEVICE_DETAIL_SCREEN, deviceResolution);

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return jsonObject;
    }
    public static String adID(Context context)
    {
        AdvertisingIdClient.Info idInfo;
        String advertisingId ="";
        try {
            idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            advertisingId = idInfo.getId();
            Log.e("Advertisement",advertisingId);
            return advertisingId;

            //do sth with the id

        } catch (IOException e) {
            e.printStackTrace();

        } catch (GooglePlayServicesNotAvailableException e) {
            return  "";

        } catch (GooglePlayServicesRepairableException e) {
            return  "";
        }
        return advertisingId;

    }
    public static JSONObject getAttributionData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(SDKRequestConstant.ATTRIBUTION_DATA_CLICK_ID, CLICK_ID);
            jsonObject.put(SDKRequestConstant.ATTRIBUTION_DATA_INSTALL_REFERRER, INSTALL_REFERRER);
            jsonObject.put(SDKRequestConstant.ATTRIBUTION_DATA_SDK_VERSION, SDK_VERSION);
            jsonObject.put(SDKRequestConstant.ATTRIBUTION_DATA_REFERRER_CLICK_TIMESTAMP_SECONDS, INSTALL_APP_REFER_CLICK_TIME);
            jsonObject.put(SDKRequestConstant.ATTRIBUTION_DATA_INSTALL_BEGIN_TIMESTAMP_SECONDS, INSTALL_APP_REFER_INSTALL_TIME);
            jsonObject.put(SDKRequestConstant.ATTRIBUTION_DATA_REFERRER_CLICK_TIMESTAMP_SERVER_SECONDS, INSTALL_APP_REFER_CLICK_TIME);
            jsonObject.put(SDKRequestConstant.ATTRIBUTION_DATA_REFERRER_CLICK_TIMESTAMP_SERVER_SECONDS, INSTALL_APP_REFER_INSTALL_TIME);
            jsonObject.put(SDKRequestConstant.ATTRIBUTION_DATA_INSTALL_VERSION, SDK_VERSION);
            jsonObject.put(SDKRequestConstant.ATTRIBUTION_DATA_GOOGLE_PLAY_INSTANT, "");

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return jsonObject;
    }
    public static JSONObject getAppDetails(Context context)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(SDKRequestConstant.APP_DETAILS_NAME, getApplicationName(context));
            jsonObject.put(SDKRequestConstant.APP_DETAILS_VERSION, appVersion(context));
            jsonObject.put(SDKRequestConstant.APP_DETAILS_BUILD, appVersionCode(context));
            jsonObject.put(SDKRequestConstant.APP_DETAILS_BUNDLE, appPackageName(context));
            jsonObject.put(SDKRequestConstant.APP_DETAILS_APP_ID, DriveMetaData.clientAppId);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return jsonObject;
    }
    public static  JSONObject getUTMParameter(Context context)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(SDKRequestConstant.UTM_CAMPAIGN, Utils.utm_campaign);
            jsonObject.put(SDKRequestConstant.UTM_TERM, Utils.utm_term);
            jsonObject.put(SDKRequestConstant.UTM_SOURCE, Utils.utm_source);
            jsonObject.put(SDKRequestConstant.UTM_MEDIUM, Utils.utm_medium);
            jsonObject.put(SDKRequestConstant.UTM_CONTENT, Utils.utm_content);


        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return jsonObject;
    }
    public static  JSONObject getLocation(Context context) {
        JSONObject jsonObject = new JSONObject();

        GPSTracker gpsTracker = new GPSTracker(context);
        if (gpsTracker.getIsGPSTrackingEnabled()) {
            try {
                jsonObject.put(SDKRequestConstant.LOCATION_CITY, ""+gpsTracker.getCountryName(context));
                jsonObject.put(SDKRequestConstant.LOCATION_COUNTRY,""+ gpsTracker.getCountryName(context));
                jsonObject.put(SDKRequestConstant.LOCATION_ISO_COUNTRY_CODE, ""+gpsTracker.getGeocoderAddress(context));
                jsonObject.put(SDKRequestConstant.LOCATION_REGION_CODE, "91");
                jsonObject.put(SDKRequestConstant.LOCATION_POSTAL_CODE, gpsTracker.getPostalCode(context));
                jsonObject.put(SDKRequestConstant.LOCATION_SOURCE, "lat_long");
                jsonObject.put(SDKRequestConstant.LOCATION_LATITUDE, gpsTracker.getLatitude());
                jsonObject.put(SDKRequestConstant.LOCATION_LONGITUDE, gpsTracker.getLongitude());
                jsonObject.put(SDKRequestConstant.LOCATION_SPEED, 0);


            } catch (Exception exception) {
               // exception.printStackTrace();
            }
        } else {
            try {
                jsonObject.put(SDKRequestConstant.LOCATION_CITY, "");
                jsonObject.put(SDKRequestConstant.LOCATION_COUNTRY, "");
                jsonObject.put(SDKRequestConstant.LOCATION_ISO_COUNTRY_CODE, "");
                jsonObject.put(SDKRequestConstant.LOCATION_REGION_CODE, "");
                jsonObject.put(SDKRequestConstant.LOCATION_POSTAL_CODE, "");
                jsonObject.put(SDKRequestConstant.LOCATION_SOURCE, "lat_long");
                jsonObject.put(SDKRequestConstant.LOCATION_LATITUDE, 0.00);
                jsonObject.put(SDKRequestConstant.LOCATION_LONGITUDE, 0.00);
                jsonObject.put(SDKRequestConstant.LOCATION_SPEED, 0);

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }


    return jsonObject;
    }
    public static  JSONObject getNetworkDetails(Context context)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(SDKRequestConstant.NETWORK_BLUETOOTH, false);
            jsonObject.put(SDKRequestConstant.NETWORK_CARRIER, "");
            jsonObject.put(SDKRequestConstant.NETWORK_CELLULAR, true);
            jsonObject.put(SDKRequestConstant.NETWORK_WIFI, false);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return jsonObject;
    }
    public static  JSONObject getLibraryDetails(Context context)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "android_sdk");
            jsonObject.put("version", SDK_VERSION);


        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return jsonObject;
    }
    public static String getApplicationName(Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }
    private static String  appVersion(Context context)
    {
        PackageManager pm = context.getPackageManager();
        String pkgName = context.getPackageName();
        Log.e("Packagename",pkgName);
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pm.getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String ver = pkgInfo.versionName;
        return ver;
    }
    private static String  appPackageName(Context context)
    {
        PackageManager pm = context.getPackageManager();
        String pkgName = context.getPackageName();
        Log.e("Packagename",pkgName);
        return pkgName;
    }
    private static int  appVersionCode(Context context)
    {
        PackageManager pm = context.getPackageManager();
        String pkgName = context.getPackageName();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pm.getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int ver = pkgInfo.versionCode;
        return ver;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }
    public static JSONObject userIdentifier(Context context) {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("customer_id", "1234567");
                jsonObject.put("customer_alias", "");
                jsonObject.put("email", "amit@gmail.com");
                jsonObject.put("twitter", "");
                jsonObject.put("google", "");
                jsonObject.put("microsoft", "");
                jsonObject.put("other", "");
                jsonObject.put("twitter", "");
                jsonObject.put("other", "");
                jsonObject.put("other_id2", "");
                jsonObject.put("other_id3", "");
                jsonObject.put("other_id4", "");
                jsonObject.put("other_id5", "");
                jsonObject.put("other_id6", "");
                jsonObject.put("other_id7", "");
                jsonObject.put("other_id8", "");
                jsonObject.put("other_id9", "");
                jsonObject.put("other_id10", "");
                jsonObject.put("mobile1", "");
                jsonObject.put("phone1", "");
                jsonObject.put("phone2", "");


            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return jsonObject;
        }



    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private static float getDeviceDPI() {
        return Resources.getSystem().getDisplayMetrics().density;
    }

    public static String RequestDataTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return  sdf.format(c.getTime());
    }



    /** Creates a mutable HashSet instance containing the given elements in unspecified order */
    public static <T> Set<T> newSet(T... values) {
        Set<T> set = new HashSet<>(values.length);
        Collections.addAll(set, values);
        return set;
    }

    // TODO: Migrate other coercion methods.

    /**
     * Returns the float representation at {@code value} if it exists and is a float or can be
     * coerced to a float. Returns {@code defaultValue} otherwise.
     */
    public static float coerceToFloat(Object value, float defaultValue) {
        if (value instanceof Float) {
            return (float) value;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else if (value instanceof String) {
            try {
                return Float.valueOf((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    /** Returns true if the application has the given permission. */
    public static boolean hasPermission(Context context, String permission) {
        return hasPermission(context, permission, 0);
    }

    private static boolean hasPermission(Context context, String permission, int repeatCount) {
        try {
            return context.checkCallingOrSelfPermission(permission) == PERMISSION_GRANTED;
        } catch (Exception e) {
            // exception during permission check means we need to assume it is not granted
            return repeatCount < PERMISSION_CHECK_REPEAT_MAX_COUNT
                    && hasPermission(context.getApplicationContext(), permission, repeatCount + 1);
        }
    }

    /** Returns true if the application has the given feature. */
    public static boolean hasFeature(Context context, String feature) {
        return context.getPackageManager().hasSystemFeature(feature);
    }

    /** Returns the system service for the given string. */
    @SuppressWarnings("unchecked")
    public static <T> T getSystemService(Context context, String serviceConstant) {
        return (T) context.getSystemService(serviceConstant);
    }

    /** Returns true if the string is null, or empty (once trimmed). */
    public static boolean isNullOrEmpty(CharSequence text) {
        return isEmpty(text) || getTrimmedLength(text) == 0;
    }

    /**
     * Returns true if the string is null or 0-length.
     *
     * <p>Copied from {@link TextUtils#isEmpty(CharSequence)}
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * Returns true if the string is empty (once trimmed).
     *
     * @param str the string to be examined
     * @return true if the string is empty (once trimmed) or 0-length
     */
    public static boolean isEmptyOrBlank(@NotNull CharSequence str) {
        return str.length() == 0 || getTrimmedLength(str) == 0;
    }

    /**
     * Returns the length that the specified CharSequence would have if spaces and control
     * characters were trimmed from the start and end, as by {@link String#trim}.
     *
     * <p>Copied from {@link TextUtils#getTrimmedLength(CharSequence)}
     */
    private static int getTrimmedLength(@NonNull CharSequence s) {
        int len = s.length();

        int start = 0;
        while (start < len && s.charAt(start) <= ' ') {
            start++;
        }

        int end = len;
        while (end > start && s.charAt(end - 1) <= ' ') {
            end--;
        }

        return end - start;
    }

    /** Returns true if the collection is null or has a size of 0. */
    public static boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    /** Returns true if the array is null or has a size of 0. */
    public static <T> boolean isNullOrEmpty(T[] data) {
        return data == null || data.length == 0;
    }

    /** Returns true if the map is null or empty, false otherwise. */
    public static boolean isNullOrEmpty(Map map) {
        return map == null || map.size() == 0;
    }

    /** Throws a {@link NullPointerException} if the given text is null or empty. */
    @NonNull
    public static String assertNotNullOrEmpty(String text, @Nullable String name) {
        if (isNullOrEmpty(text)) {
            throw new NullPointerException(name + " cannot be null or empty");
        }
        return text;
    }

    /** Throws a {@link NullPointerException} if the given map is null or empty. */
    @NonNull
    public static <K, V> Map<K, V> assertNotNullOrEmpty(Map<K, V> data, @Nullable String name) {
        if (isNullOrEmpty(data)) {
            throw new NullPointerException(name + " cannot be null or empty");
        }
        return data;
    }

    /** Throws a {@link NullPointerException} if the given object is null. */
    @NonNull
    public static <T> T assertNotNull(T object, String item) {
        if (object == null) {
            throw new NullPointerException(item + " == null");
        }
        return object;
    }

    /** Returns an immutable copy of the provided map. */
    @NonNull
    public static <K, V> Map<K, V> immutableCopyOf(@NonNull Map<K, V> map) {
        // TODO (major version change) change this out to allow @Nullable input and guard against it
        //  @NonNull
        //  public static <K, V> Map<K, V> immutableCopyOf(@Nullable Map<K, V> map) {
        //    if (isNullOrEmpty(map)) {
        //      return Collections.emptyMap();
        //    }
        //    return Collections.unmodifiableMap(new LinkedHashMap<>(map));
        //  }
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    /** Returns an immutable copy of the provided list. */
    @NonNull
    public static <T> List<T> immutableCopyOf(@Nullable List<T> list) {
        if (isNullOrEmpty(list)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    /** Returns a shared preferences for storing any library preferences. */
    public static SharedPreferences getSegmentSharedPreferences(Context context, String tag) {
        return context.getSharedPreferences("analytics-android-" + tag, MODE_PRIVATE);
    }

    /** Get the string resource for the given key. Returns null if not found. */
    public static String getResourceString(Context context, String key) {
        int id = getIdentifier(context, "string", key);
        if (id != 0) {
            return context.getResources().getString(id);
        } else {
            return null;
        }
    }

    /** Get the identifier for the resource with a given type and key. */
    private static int getIdentifier(Context context, String type, String key) {
        return context.getResources().getIdentifier(key, type, context.getPackageName());
    }

    /**
     * Returns {@code true} if the phone is connected to a network, or if we don't have the enough
     * permissions. Returns {@code false} otherwise.
     */
    public static boolean isConnected(Context context) {
        if (!hasPermission(context, ACCESS_NETWORK_STATE)) {
            return true; // Assume we have a connection and try to upload.
        }
        ConnectivityManager cm = getSystemService(context, CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission")
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /** Return {@code true} if a class with the given name is found. */
    public static boolean isOnClassPath(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            // ignored
            return false;
        }
    }

    /**
     * Close the given {@link Closeable}. If an exception is thrown during {@link
     * Closeable#close()}, this will quietly ignore it. Does nothing if {@code closeable} is {@code
     * null}.
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    /** Buffers the given {@code InputStream}. */
    public static BufferedReader buffer(InputStream is) {
        return new BufferedReader(new InputStreamReader(is));
    }

    /** Reads the give {@code InputStream} into a String. */
    public static String readFully(InputStream is) throws IOException {
        return readFully(buffer(is));
    }

    /** Reads the give {@code BufferedReader} into a String. */
    public static String readFully(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String line; (line = reader.readLine()) != null; ) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static InputStream getInputStream(HttpURLConnection connection) throws IOException {
        try {
            return connection.getInputStream();
        } catch (IOException ignored) {
            return connection.getErrorStream();
        }
    }

    /**
     * Transforms the given map by replacing the keys mapped by {@code mapper}. Any keys not in the
     * mapper preserve their original keys. If a key in the mapper maps to null or a blank string,
     * that value is dropped.
     *
     * <p>e.g. transform({a: 1, b: 2, c: 3}, {a: a, c: ""}) -&gt; {$a: 1, b: 2} - transforms a to $a
     * - keeps b - removes c
     */
    public static <T> Map<String, T> transform(Map<String, T> in, Map<String, String> mapper) {
        Map<String, T> out = new LinkedHashMap<>(in.size());
        for (Map.Entry<String, T> entry : in.entrySet()) {
            String key = entry.getKey();
            if (!mapper.containsKey(key)) {
                out.put(key, entry.getValue()); // keep the original key.
                continue;
            }
            String mappedKey = mapper.get(key);
            if (!isNullOrEmpty(mappedKey)) {
                out.put(mappedKey, entry.getValue());
            }
        }
        return out;
    }

    /**
     * Return a copy of the contents of the given map as a {@link JSONObject}. Instead of failing on
     * {@code null} values like the {@link JSONObject} map constructor, it cleans them up and
     * correctly converts them to {@link JSONObject#NULL}.
     */
    public static JSONObject toJsonObject(Map<String, ?> map) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            Object value = wrap(entry.getValue());
            try {
                jsonObject.put(entry.getKey(), value);
            } catch (JSONException ignored) {
                // Ignore values that JSONObject doesn't accept.
            }
        }
        return jsonObject;
    }

    /**
     * Wraps the given object if necessary. {@link JSONObject#wrap(Object)} is only available on API
     * 19+, so we've copied the implementation. Deviates from the original implementation in that it
     * always returns {@link JSONObject#NULL} instead of {@code null} in case of a failure, and
     * returns the {@link Object#toString} of any object that is of a custom (non-primitive or
     * non-collection/map) type.
     *
     * <p>If the object is null returns {@link JSONObject#NULL}. If the object is a {@link
     * JSONArray} or {@link JSONObject}, no wrapping is necessary. If the object is {@link
     * JSONObject#NULL}, no wrapping is necessary. If the object is an array or {@link Collection},
     * returns an equivalent {@link JSONArray}. If the object is a {@link Map}, returns an
     * equivalent {@link JSONObject}. If the object is a primitive wrapper type or {@link String},
     * returns the object. Otherwise returns the result of {@link Object#toString}. If wrapping
     * fails, returns JSONObject.NULL.
     */
    private static Object wrap(Object o) {
        if (o == null) {
            return JSONObject.NULL;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }
        if (o.equals(JSONObject.NULL)) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                return new JSONArray((Collection) o);
            } else if (o.getClass().isArray()) {
                final int length = Array.getLength(o);
                JSONArray array = new JSONArray();
                for (int i = 0; i < length; ++i) {
                    array.put(wrap(Array.get(array, i)));
                }
                return array;
            }
            if (o instanceof Map) {
                //noinspection unchecked
                return toJsonObject((Map) o);
            }
            if (o instanceof Boolean
                    || o instanceof Byte
                    || o instanceof Character
                    || o instanceof Double
                    || o instanceof Float
                    || o instanceof Integer
                    || o instanceof Long
                    || o instanceof Short
                    || o instanceof String) {
                return o;
            }
            // Deviate from original implementation and return the String representation of the
            // object
            // regardless of package.
            return o.toString();
        } catch (Exception ignored) {
        }
        // Deviate from original and return JSONObject.NULL instead of null.
        return JSONObject.NULL;
    }

    public static <T> Map<String, T> createMap() {
        return new NullableConcurrentHashMap<>();
    }

    /**
     * Ensures that a directory is created in the given location, throws an IOException otherwise.
     */
    public static void createDirectory(File location) throws IOException {
        if (!(location.exists() || location.mkdirs() || location.isDirectory())) {
            throw new IOException("Could not create directory at " + location);
        }
    }

    /** Copies all the values from {@code src} to {@code target}. */
    public static void copySharedPreferences(SharedPreferences src, SharedPreferences target) {
        SharedPreferences.Editor editor = target.edit();
        for (Map.Entry<String, ?> entry : src.getAll().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Set) {
                editor.putStringSet(key, (Set<String>) value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            }
        }
        editor.apply();
    }

    /** Returns the referrer who started the Activity. */
    public static Uri getReferrer(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return activity.getReferrer();
        }
        return getReferrerCompatible(activity);
    }

    /** Returns the referrer on devices running SDK versions lower than 22. */
    private static Uri getReferrerCompatible(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Intent intent = activity.getIntent();
            Uri referrerUri = intent.getParcelableExtra(Intent.EXTRA_REFERRER);
            if (referrerUri != null) {
                return referrerUri;
            }
            // Intent.EXTRA_REFERRER_NAME
            String referrer = intent.getStringExtra("android.intent.extra.REFERRER_NAME");
            if (referrer != null) {
                // Try parsing the referrer URL; if it's invalid, return null
                try {
                    return Uri.parse(referrer);
                } catch (android.net.ParseException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private Utils() {
        throw new AssertionError("No instances");
    }

    public static class AnalyticsNetworkExecutorService extends ThreadPoolExecutor {

        private static final int DEFAULT_THREAD_COUNT = 1;
        // At most we perform two network requests concurrently
        private static final int MAX_THREAD_COUNT = 2;

        public AnalyticsNetworkExecutorService() {
            //noinspection Convert2Diamond
            super(
                    DEFAULT_THREAD_COUNT,
                    MAX_THREAD_COUNT,
                    0,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new AnalyticsThreadFactory());
        }
    }

    public static class AnalyticsThreadFactory implements ThreadFactory {

        @SuppressWarnings("NullableProblems")
        public Thread newThread(Runnable r) {
            return new AnalyticsThread(r);
        }
    }

    private static class AnalyticsThread extends Thread {

        private static final AtomicInteger SEQUENCE_GENERATOR = new AtomicInteger(1);

        public AnalyticsThread(Runnable r) {
            super(r, THREAD_PREFIX + SEQUENCE_GENERATOR.getAndIncrement());
        }

        @Override
        public void run() {
            Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
            super.run();
        }
    }

    /** A {@link ConcurrentHashMap} that rejects null keys and values instead of failing. */
    public static class NullableConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

        public NullableConcurrentHashMap() {
            super();
        }

        public NullableConcurrentHashMap(Map<? extends K, ? extends V> m) {
            super(m);
        }

        @Override
        public V put(K key, V value) {
            if (key == null || value == null) {
                return null;
            }
            return super.put(key, value);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            for (Entry<? extends K, ? extends V> e : m.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }
    }

    public static String extractValueFromQueryStringAndKey(String key,
                                                           String queryString) {
        String foundValue = null;
        if(key!= "" && queryString!="") {
            try {
                StringTokenizer queryItems = new StringTokenizer(queryString, "&");
                while (queryItems.hasMoreTokens() && foundValue == null) {
                    String queryParameter = queryItems.nextToken();
                    StringTokenizer paramTokenizer = new StringTokenizer(
                            queryParameter, "=");
                    String currentKey = paramTokenizer.nextToken();
                    String currentValue = paramTokenizer.nextToken();
                    if (currentKey.equalsIgnoreCase(key)) {
                        foundValue = currentValue;
                    }
                }
                return foundValue;
            } catch (Exception ex) {
                return foundValue;


            }
        }
        else {
            return foundValue;

        }
    }

}


