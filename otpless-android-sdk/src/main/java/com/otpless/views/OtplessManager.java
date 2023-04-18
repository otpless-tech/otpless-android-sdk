package com.otpless.views;

import static com.otpless.utils.Utility.getSchemeHost;
import static com.otpless.utils.Utility.isNotEmpty;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.otpless.network.ApiManager;
import com.otpless.utils.SchemeHostMetaInfo;
import com.otpless.utils.Utility;

public class OtplessManager {

    private static OtplessManager sInstance = null;
    private static final String URL_PATTERN = "https://%s.authlink.me";
    public String redirectUrl = "";

    public static OtplessManager getInstance() {
        if (sInstance == null) {
            synchronized (OtplessManager.class) {
                if (sInstance != null) {
                    return sInstance;
                }
                sInstance = new OtplessManager();
            }
        }
        return sInstance;
    }

    private final OtplessImpl mOtpImpl;

    private OtplessManager() {
        this.mOtpImpl = new OtplessImpl();
    }

    public void init(final FragmentActivity activity) {
        this.setUrlRedirectURI(activity);
        this.mOtpImpl.add(activity);
    }

    private void setUrlRedirectURI(FragmentActivity activity) {
        if (Utility.isValid(redirectUrl, ApiManager.getInstance().baseUrl)) {
            return;
        }
        String packageName = activity.getApplicationContext().getPackageName();
        String domainHost = packageName.replace(".", "-");
        final String apiURl = String.format(URL_PATTERN, domainHost);
        ApiManager.getInstance().baseUrl = apiURl;
        final SchemeHostMetaInfo info = getSchemeHost(activity);
        if (info != null) {
            redirectUrl = apiURl + "?redirectUri=" + info.getScheme() + "://" + info.getHost();
        }
    }

    public void launch(final Context context, final String link, final OtplessUserDetailCallback callback) {
        // if redirectUri is invalid or passed link do not match with redirectUri
        // create redirectUri and apiUrl from passed link
        if (!Utility.isValid(redirectUrl) || !this.redirectUrl.equals(link)) {
            try {
                final Uri otplessUri = Uri.parse(link);
                final Uri.Builder builder = otplessUri.buildUpon();
                builder.clearQuery();
                final SchemeHostMetaInfo schemeHostMetaInfo = Utility.getSchemeHost(context);
                if (schemeHostMetaInfo != null) {
                    final String redirectUri = String.format("%s://%s", schemeHostMetaInfo.getScheme(), schemeHostMetaInfo.getHost());
                    builder.appendQueryParameter("redirectUri", redirectUri);
                    this.redirectUrl = builder.build().toString();
                }
                ApiManager.getInstance().baseUrl = otplessUri.getScheme() + "://" + otplessUri.getHost();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        this.mOtpImpl.launch(context, redirectUrl, callback);
    }

    public void setConfiguration(@NonNull final Context context, String backgroundColor,
                                 String loaderColor, String messageText, String messageColor,
                                 String cancelButtonText, String cancelButtonColor
    ) {
        final SharedPreferences pref = context.getSharedPreferences("otpless_configuration", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        if (isNotEmpty(backgroundColor)) {
            editor.putString("screen_bg_color", backgroundColor);
        }
        if (isNotEmpty(loaderColor)) {
            editor.putString("loader_color", loaderColor);
        }
        if (isNotEmpty(messageText)) {
            editor.putString("message_text", messageText);
        }
        if (isNotEmpty(messageColor)) {
            editor.putString("message_color", messageColor);
        }
        if (isNotEmpty(cancelButtonText)) {
            editor.putString("cancel_btn_text", cancelButtonText);
        }
        if (isNotEmpty(cancelButtonColor)) {
            editor.putString("cancel_btn_color", cancelButtonColor);
        }
        editor.apply();
    }

    /**
     * return string array of length 6
     */
    public String[] getConfiguration(final Context context) {
        final String[] result = new String[6];
        final SharedPreferences pref = context.getSharedPreferences("otpless_configuration", Context.MODE_PRIVATE);
        result[0] = pref.getString("screen_bg_color", null);
        result[1] = pref.getString("loader_color", null);
        result[2] = pref.getString("message_text", null);
        result[3] = pref.getString("message_color", null);
        result[4] = pref.getString("cancel_btn_text", null);
        result[5] = pref.getString("cancel_btn_color", null);
        return result;
    }
}
