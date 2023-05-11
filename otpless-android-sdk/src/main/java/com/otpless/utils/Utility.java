package com.otpless.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.otpless.BuildConfig;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Utility {

    @NonNull
    private static final HashMap<String, String> mAdditionalAppInfo = new HashMap<>();

    public static void addContextInfo(final Context context) {
        final Context applicationContext = context.getApplicationContext();
        mAdditionalAppInfo.put("manufacturer", Build.MANUFACTURER);
        mAdditionalAppInfo.put("androidVersion", String.valueOf(Build.VERSION.SDK_INT));
        mAdditionalAppInfo.put("model", Build.MODEL);
        // adding sdk version
        mAdditionalAppInfo.put("sdkVersion", BuildConfig.OTPLESS_VERSION_NAME);
        try {
            mAdditionalAppInfo.put("appPackageName", applicationContext.getPackageName());
            final PackageInfo pInfo = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0);
            mAdditionalAppInfo.put("appVersion", pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // adding android id
        String androidId = Settings.Secure.getString(
                applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID
        );
        mAdditionalAppInfo.put("hasWhatsapp", String.valueOf(isWhatsAppInstalled(context)));
        mAdditionalAppInfo.put("deviceId", androidId);
        mAdditionalAppInfo.put("installerName", getInstallerName(context));
        mAdditionalAppInfo.put("appSignature", getAppSignature(context));
    }

    public static boolean isAppInstalled(final PackageManager packageManager, final String packageName) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isValid(String... args) {
        for (String str : args) {
            if (str == null || str.length() == 0) {
                return false;
            }
        }
        return true;
    }

    public static Uri combineQueries(@NonNull final Uri mainUri, @NonNull final Uri secondUri) {
        final HashMap<String, String> queryMap = new HashMap<>();
        // add queries from main uri
        for (final String key : mainUri.getQueryParameterNames()) {
            final String value = mainUri.getQueryParameter(key);
            if (value == null || value.length() == 0) continue;
            queryMap.put(key, value);
        }
        // add queries from second uri
        for (final String key : secondUri.getQueryParameterNames()) {
            final String value = secondUri.getQueryParameter(key);
            if (value == null || value.length() == 0) continue;
            queryMap.put(key, value);
        }
        final Uri.Builder builder = mainUri.buildUpon().clearQuery();
        for (final Map.Entry<String, String> entry : queryMap.entrySet()) {
            if ("login_uri".equals(entry.getKey())) continue;
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        // check and add login_uri at last
        if (queryMap.containsKey("login_uri")) {
            builder.appendQueryParameter("login_uri", queryMap.get("login_uri"));
        }
        return builder.build();
    }

    @NonNull
    private static String mAppSign = "";
    private static final String HASHING_ALGO = "SHA-256";
    private static final int HASH_BYTE_SIZE = 9;
    private static final int HASH_BASE64_SIZE = 11;

    /**
     * use to push web events
     */
    public static void pushEvent(final String eventName) {
        pushEvent(eventName, new JSONObject());
    }

    public static void pushEvent(final String eventName, final JSONObject eventParams) {
        final JSONObject eventData = new JSONObject();
        try {
            eventData.put("event_name", eventName);
            eventData.put("platform", "android");
            eventData.put("sdk_version", BuildConfig.OTPLESS_VERSION_NAME);
            // adding other values in event params
            for (Map.Entry<String, String> entry : mAdditionalAppInfo.entrySet()) {
                eventParams.put(entry.getKey(), entry.getValue());
            }
            eventData.put("event_params", eventParams.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiManager.getInstance().pushEvents(eventData, new ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                Log.d("PUSH_EVENT", data.toString());
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    @NonNull
    public static HashMap<String, String> getAdditionalAppInfo() {
        return mAdditionalAppInfo;
    }

    public static boolean isWhatsAppInstalled(final Context context) {
        final PackageManager manager = context.getPackageManager();
        return isAppInstalled(manager, "com.whatsapp") || isAppInstalled(manager, "com.whatsapp.w4b");
    }

    @NonNull
    private static String getInstallerName(final Context context) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            final String installerName = packageManager.getInstallerPackageName(context.getPackageName());
            if (installerName != null && installerName.length() > 0) {
                return installerName;
            }
            return "NA";
        } catch (Throwable ignore) {
            return "NA";
        }
    }

    public static String getAppSignature(final Context context) {
        if (!mAppSign.isEmpty()) {
            return mAppSign;
        }
        final Context appContext = context.getApplicationContext();
        final PackageManager packageManager = appContext.getPackageManager();
        final String packageName = appContext.getPackageName();
        try {
            final Signature[] signs;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                final SigningInfo info = packageManager.getPackageInfo(
                        packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES)
                ).signingInfo;
                if (info.hasMultipleSigners()) {
                    signs = info.getApkContentsSigners();
                } else {
                    signs = info.getSigningCertificateHistory();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                final SigningInfo info = packageManager.getPackageInfo(
                        packageName, PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo;
                if (info.hasMultipleSigners()) {
                    signs = info.getApkContentsSigners();
                } else {
                    signs = info.getSigningCertificateHistory();
                }
            } else {
                signs = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures;
            }
            final ArrayList<String> signList = new ArrayList<>();
            for (final Signature sign : signs) {
                try {
                    final String info = String.format("%s %s", packageName, sign.toCharsString());
                    final MessageDigest digest = MessageDigest.getInstance(HASHING_ALGO);
                    digest.update(info.getBytes("UTF-8"));
                    final byte[] digestedBytes = digest.digest();
                    final String base64 = Base64.encodeToString(
                            digestedBytes, Base64.NO_PADDING | Base64.NO_WRAP
                    );
                    signList.add(base64);
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException exception) {
                    return "";
                }
            }
            if (!signList.isEmpty()) {
                mAppSign = signList.get(0);
            }
            Log.d("OTP-less", String.format("app signature: %s", mAppSign));
            return mAppSign;
        } catch (PackageManager.NameNotFoundException | UnsupportedOperationException exception) {
            return "";
        }
    }
}
