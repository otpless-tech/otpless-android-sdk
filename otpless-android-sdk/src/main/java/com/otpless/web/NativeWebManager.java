package com.otpless.web;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.otpless.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NativeWebManager implements OtplessWebListener {

    private static final String OtplessPreferenceStore = "otpless_shared_pref_store";

    @NonNull
    private final FragmentActivity mActivity;
    @NonNull
    private final OtplessWebView mWebView;

    private boolean mBackSubscription = false;

    public NativeWebManager(@NonNull final FragmentActivity fragmentActivity, @NonNull final OtplessWebView webView) {
        mActivity = fragmentActivity;
        mWebView = webView;
    }

    // key 1
    @Override
    public void showLoader(final String message) {
        mWebView.callWebJs("showLoader", message);
    }

    // key 2
    @Override
    public void hideLoader() {
        mWebView.callWebJs("hideLoader");
    }

    // key 3
    @Override
    public void subscribeBackPress(final boolean subscribe) {
        mBackSubscription = subscribe;
    }

    public boolean getBackSubscription() {
        return mBackSubscription;
    }

    // key 6
    @Override
    public void openDeeplink(@NonNull final String deeplink) {
        try {
            final Uri deeplinkUrl = Uri.parse(deeplink);
            final Intent whatsappIntent = new Intent(Intent.ACTION_VIEW, deeplinkUrl);
            mActivity.startActivity(whatsappIntent);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    // key 4
    @Override
    public void saveString(@NonNull String infoKey, @NonNull String infoValue) {
        final SharedPreferences preferences = mActivity.getSharedPreferences(OtplessPreferenceStore, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(infoKey, infoValue);
        editor.apply();
    }

    // key 5
    @Override
    public void getString(@NonNull String infoKey) {
        final SharedPreferences preferences = mActivity.getSharedPreferences(OtplessPreferenceStore, Context.MODE_PRIVATE);
        final String infoValue = preferences.getString(infoKey, "");
        mWebView.callWebJs("onStorageValueSuccess", infoKey, infoValue);
    }

    // key 8
    @Override
    public void appInfo() {
        final JSONObject json = new JSONObject();
        for (Map.Entry<String, String> entry : getAppInfo().entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        final String jsonString = json.toString();
        mWebView.callWebJs("onAppInfoResult", jsonString);
    }

    private Map<String, String> getAppInfo() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("platform", "android");
        map.put("manufacturer", Build.MANUFACTURER);
        map.put("androidVersion", String.valueOf(Build.VERSION.SDK_INT));
        map.put("model", Build.MODEL);
        // adding sdk version
        map.put("sdkVersion", BuildConfig.OTPLESS_VERSION_NAME);
        // adding containing application info and version info
        final Context applicationContext = mActivity.getApplicationContext();
        try {
            map.put("packageName", applicationContext.getPackageName());
            final PackageInfo pInfo = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0);
            map.put("appVersion", pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // adding android id
        String androidId = Settings.Secure.getString(
                applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID
        );
        map.put("deviceId", androidId);
        return map;
    }
}
