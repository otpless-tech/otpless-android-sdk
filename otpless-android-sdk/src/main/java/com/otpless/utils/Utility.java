package com.otpless.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.otpless.BuildConfig;

import org.json.JSONObject;

public class Utility {

    private static final String DEVICE_ID = "deviceId";
    private static final String PACKAGENAME = "package";
    private static final String PLATFORM = "platform";
    private static final String OSVERSION = "osVersion";
    private static final String MANUFACTURER = "manufacturer";
    private static final String APP_VERSION_NAME = "appVersionName";
    private static final String APP_VERSION_CODE = "appVersionCode";
    private static final String SDKVERSION = "sdkVersion";
    private static final String SDKVERSIONVALUE = BuildConfig.OTPLESS_VERSION_NAME;


    public static boolean isAppInstalled(final PackageManager packageManager, final String packageName) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String parseUserNumber(final JSONObject jsonObject) {
        JSONObject user = jsonObject.optJSONObject("data");
        if (user != null) {
            return user.optString("userMobile");
        }
        return null;
    }

    public static boolean isNotEmpty(final String str) {
        return str != null && str.length() > 0;
    }

    public static void deleteWaId(final Context context) {
        SharedPreferences sp = context.getSharedPreferences("otpless_storage_manager", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("otpless_waid");
        editor.apply();
    }

    public static String getUrlWithDeviceParams(Context context, String url){
        if (url == null)
            return url;
        try{
            StringBuffer urlBuffer = new StringBuffer(url);
            String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            urlBuffer.append("&"+DEVICE_ID+"="+deviceId);
            String packageName = context.getPackageName();
            urlBuffer.append("&"+PACKAGENAME+"="+packageName);
            String platform = "android";
            urlBuffer.append("&"+PLATFORM+"="+platform);
            String osVersion = String.valueOf(Build.VERSION.SDK_INT);
            urlBuffer.append("&"+OSVERSION+"="+osVersion);
            String manufacturer = Build.MANUFACTURER;
            urlBuffer.append("&"+MANUFACTURER+"="+manufacturer);
            String versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            urlBuffer.append("&"+APP_VERSION_NAME+"="+versionName);
            String versionCode = String.valueOf( context.getPackageManager()
                     .getPackageInfo(context.getPackageName(), 0).versionCode);
            urlBuffer.append("&"+APP_VERSION_CODE+"="+versionCode);
            urlBuffer.append("&"+SDKVERSION+"="+SDKVERSIONVALUE);
            return urlBuffer.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return url;
    }

    @Nullable
    public static Integer parseColor(String color) {
        try {
            return Color.parseColor(color);
        } catch (Exception exception) {
            return null;
        }
    }

    public static boolean isValid(String... args) {
        for (String str: args) {
            if (str == null || str.length() == 0) {
                return false;
            }
        }
        return true;
    }

    public static SchemeHostMetaInfo getSchemeHost(final Context context) {
        // check the scheme and host with from manifest
        try {
            final ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String host = info.metaData.getString("otpless.deeplink.host");
            String scheme = info.metaData.getString("otpless.deeplink.scheme");
            // host and scheme will always be
            return new SchemeHostMetaInfo(scheme, host);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
