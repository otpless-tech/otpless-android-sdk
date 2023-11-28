package com.otpless.web;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.otpless.BuildConfig;
import com.otpless.main.NativeWebListener;
import com.otpless.main.OtplessEventCode;
import com.otpless.main.OtplessEventData;
import com.otpless.main.WebActivityContract;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;
import com.otpless.network.NetworkStatusData;
import com.otpless.network.OtplessNetworkManager;
import com.otpless.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NativeWebManager implements OtplessWebListener {

    private static final String OtplessPreferenceStore = "otpless_shared_pref_store";

    @NonNull
    private final Activity mActivity;
    @NonNull
    private final OtplessWebView mWebView;
    @NonNull
    private final WebActivityContract contract;

    private boolean mBackSubscription = false;

    private NativeWebListener nativeWebListener;

    public NativeWebManager(@NonNull final Activity activity, @NonNull final OtplessWebView webView, @NonNull WebActivityContract contract) {
        mActivity = activity;
        mWebView = webView;
        this.contract = contract;
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
            //region ==== sending the event ====
            final String channel = deeplinkUrl.getScheme() + "://" + deeplinkUrl.getHost();

            final JSONObject params = new JSONObject();
            params.put("channel", channel);
            Utility.pushEvent("intent_redirect_out", params);
            final JSONObject data = new JSONObject();
            data.put("event_action", "button_clicked");
            if (!deeplinkUrl.getScheme().equals("https")) {
                data.put("channel", channel);
            }
            if (nativeWebListener != null) {
                nativeWebListener.onOtplessEvent(new OtplessEventData(OtplessEventCode.BUTTON_CLICK, data));
            }
            //endregion
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
        map.put("hasWhatsapp", String.valueOf(Utility.isWhatsAppInstalled(mActivity)));
        map.put("appSignature", Utility.getAppSignature(mActivity));
        return map;
    }

    // key 11
    @Override
    public void codeVerificationStatus(@NonNull JSONObject json) {
        mActivity.runOnUiThread(() ->
            contract.onVerificationResult(Activity.RESULT_OK, json)
        );
        Utility.pushEvent("auth_completed");
    }

    // key 12
    @Override
    public void changeWebViewHeight(@NonNull final Integer heightPercent) {
        // get the height of screen
        int originalHeight = mActivity.getResources().getDisplayMetrics().heightPixels;
        int percent = heightPercent;
        if (heightPercent > 100 || heightPercent < 0) {
            percent = 100;
        }
        final int newHeight = (originalHeight * percent) / 100;
        // do update in evaluation on main thread
        mActivity.runOnUiThread(() -> {
            final ViewGroup parentView = contract.getParentView();
            final ViewGroup.LayoutParams params = parentView.getLayoutParams();
            params.height = newHeight;
            parentView.setLayoutParams(params);
        });
    }

    // key 13
    @Override
    public void extraParams() {
        final JSONObject result;
        if (contract.getExtraParams() == null) {
            result = new JSONObject();
        } else {
            result = contract.getExtraParams();
        }
        mActivity.runOnUiThread(() -> {
            mWebView.callWebJs("onExtraParamResult", result.toString());
        });
    }

    // key 14
    @Override
    public void closeActivity() {
        mActivity.runOnUiThread(() ->
            contract.onVerificationResult(Activity.RESULT_CANCELED, null)
        );
        Utility.pushEvent("user_abort");
    }

    // key 15
    @Override
    public void pushEvent(JSONObject eventData) {
        try {
            eventData.put("sdk_version", BuildConfig.OTPLESS_VERSION_NAME);
            // add additional event params
            final JSONObject additionalInfo = new JSONObject();
            for (Map.Entry<String, String> entry : Utility.getAdditionalAppInfo().entrySet()) {
                additionalInfo.put(entry.getKey(), entry.getValue());
            }
            eventData.put("additional_event_params", additionalInfo.toString());
            eventData.put("platform", "android");
            eventData.put("caller", "web");
        } catch (JSONException ignore) {
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


    public void setNativeWebListener(NativeWebListener nativeWebListener) {
        this.nativeWebListener = nativeWebListener;
    }

    // key 17 network connection type
    @Override
    public void networkConnectionType() {
        try {
            final JSONObject data = getNetworkTypeDataInJson();
            mWebView.callWebJs("onNetworkConnectionTypeCallback", data.toString());
        } catch (JSONException e) {
            final JSONObject event = new JSONObject();
            try {
                event.put("error", "query_network_type");
                event.put("message", e.getMessage());
            } catch (JSONException ignore) {
            }
            pushEvent(event);
        }
    }

    private JSONObject getNetworkTypeDataInJson() throws JSONException {
        final NetworkStatusData data = OtplessNetworkManager.getInstance().getNetworkStatus();
        final JSONObject result = new JSONObject();
        if (data.isCellular()) {
            result.put("type", "cellular");
        } else if (data.isWifi()) {
            result.put("type", "wifi");
        } else {
            result.put("type", "none");
        }
        return result;
    }
}
