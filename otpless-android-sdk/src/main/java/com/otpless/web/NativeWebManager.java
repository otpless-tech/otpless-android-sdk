package com.otpless.web;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;

import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.otpless.BuildConfig;
import com.otpless.dto.Triple;
import com.otpless.dto.Tuple;
import com.otpless.main.NativeWebListener;
import com.otpless.main.WebActivityContract;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;
import com.otpless.utils.OtpReaderManager;
import com.otpless.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeWebManager implements OtplessWebListener {

    private static final String OtplessPreferenceStore = "otpless_shared_pref_store";
    private static final int OTPLESS_PHONE_HINT_REQUEST = 9767355;

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
        Utility.addContextInfo(mActivity);
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
    public void openDeeplink(@NonNull final String deeplink, @Nullable final JSONObject extra) {
        try {
            final Uri deeplinkUrl = Uri.parse(deeplink);
            if (extra != null && extra.optBoolean("cct", false)) {
                Utility.openChromeCustomTab(mActivity, deeplinkUrl);
            } else {
                final Intent whatsappIntent = new Intent(Intent.ACTION_VIEW, deeplinkUrl);
                mActivity.startActivity(whatsappIntent);
            }
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
        map.put("hasWhatsapp", String.valueOf(Utility.isWhatsAppInstalled(mActivity)));
        map.put("appSignature", Utility.getAppSignature(mActivity));
        //adding other chatting app
        final PackageManager packageManager = applicationContext.getPackageManager();
        final List<Triple<String, String, Boolean>> messagingApps = Utility.getMessagingInstalledAppStatus(packageManager);
        for (final Triple<String, String, Boolean> installStatus : messagingApps) {
            map.put("has" + installStatus.getFirst(), String.valueOf(installStatus.getThird()));
        }
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
            public void onError(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }


    public void setNativeWebListener(NativeWebListener nativeWebListener) {
        this.nativeWebListener = nativeWebListener;
    }

    // key 16
    @Override
    public void otpAutoRead(final boolean enable) {
        if (enable) {
            OtpReaderManager.getInstance().startOtpReader(
                    this.mActivity, otpResult -> {
                        if (otpResult.isSuccess()) {
                            mWebView.callWebJs("onOtpReadSuccess", otpResult.getOtp());
                        } else {
                            mWebView.callWebJs("onOtpReadError", otpResult.getErrorMessage());
                        }
                    }
            );
        } else {
            OtpReaderManager.getInstance().stopOtpReader();
        }
    }

    // key 17
    @Override
    public void phoneNumberSelection() {
        mActivity.runOnUiThread(() -> {
            final GetPhoneNumberHintIntentRequest request = GetPhoneNumberHintIntentRequest.builder().build();
            Identity.getSignInClient(this.mActivity)
                    .getPhoneNumberHintIntent(request)
                    .addOnSuccessListener(result -> {
                        if (nativeWebListener == null) return;
                        try {
                            if (nativeWebListener.getPhoneNumberHintLauncher() != null) {
                                final IntentSenderRequest senderRequest = new IntentSenderRequest.Builder(result.getIntentSender()).build();
                                nativeWebListener.getPhoneNumberHintLauncher().launch(senderRequest);
                            } else {
                                mActivity.startIntentSenderForResult(
                                        result.getIntentSender(), OTPLESS_PHONE_HINT_REQUEST, null, 0, 0, 0
                                );
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            onPhoneNumberSelectionResult(new Tuple<>(null, e));
                        }

                    })
                    .addOnFailureListener(e -> {
                        onPhoneNumberSelectionResult(new Tuple<>(null, e));
                    });
        });
    }

    public void onPhoneNumberSelectionResult(final Tuple<String, Exception> data) {
        if (data.getSecond() == null) {
            mWebView.callWebJs("onPhoneNumberSelectionSuccess", data.getFirst());
        } else {
            String reason = data.getSecond().getMessage();
            if (reason == null) {
                reason = "Failed with exception with no reason.";
            }
            mWebView.callWebJs("onPhoneNumberSelectionError", reason);
        }
    }

    //key 26
    @Override
    public void initWebAuthnRegistration(JSONObject request) {
        try {
            this.contract.getWebAuthnManager().initRegistration(
                    request, new ApiCallback<String>() {
                        @Override
                        public void onSuccess(String data) {
                            mWebView.callWebJs("onWebAuthnRegistrationSuccess", data);
                        }

                        @Override
                        public void onError(Throwable exception) {
                            String error = exception.getMessage();
                            if (error == null) {
                                error = "Web authn registration failed.";
                            }
                            mWebView.callWebJs("onWebAuthnRegistrationError", error);
                        }
                    }
            );
        } catch (Exception e) {
            String error = e.getMessage();
            if (error == null) {
                error = "Web authn registration failed.";
            }
            mWebView.callWebJs("onWebAuthnRegistrationError", error);
        }
    }

    //key 27
    @Override
    public void initWebAuthnSignin(JSONObject request) {
        try {
            this.contract.getWebAuthnManager().initLogin(
                    request, new ApiCallback<String>() {
                        @Override
                        public void onSuccess(String data) {
                            mWebView.callWebJs("onWebAuthnSigninSuccess", data);
                        }

                        @Override
                        public void onError(Throwable exception) {
                            String error = exception.getMessage();
                            if (error == null) {
                                error = "Web authn signin failed.";
                            }
                            mWebView.callWebJs("onWebAuthnSigninError", error);
                        }
                    }
            );
        } catch (Exception e) {
            String error = e.getMessage();
            if (error == null) {
                error = "Web authn signin failed.";
            }
            mWebView.callWebJs("onWebAuthnSigninError", error);
        }
    }

    public NativeWebListener getNativeWebListener() {
        return nativeWebListener;
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == OTPLESS_PHONE_HINT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    final String phoneNumber = data.getStringExtra("phone_number_hint_result");
                    if (phoneNumber != null) {
                        onPhoneNumberSelectionResult(new Tuple<>(phoneNumber, null));
                        return;
                    }
                    onPhoneNumberSelectionResult(new Tuple<>(null, new Exception("No phone number data found in intent")));
                }
            } else {
                onPhoneNumberSelectionResult(new Tuple<>(null, new Exception("User cancelled the hint selection")));
            }
        }
    }
}
