package com.otpless.views;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.fragment.app.FragmentActivity;

import com.otpless.network.ApiManager;

public class OtplessManager {

    private static OtplessManager sInstance = null;
    private final String urlDump = "https://*.authlink.me";
    public String redirectUrl = "";
    public String apiURl = "";

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
        this.mOtpImpl.initWebLauncher(activity);
    }

    private void setUrlRedirectURI(FragmentActivity activity) {
        if (this.redirectUrl != null && this.redirectUrl.length() > 0 && this.apiURl != null && this.apiURl.length() > 0) {
            return;
        }
        try {
            ApplicationInfo ai = activity.getPackageManager().getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
            Object schemeObj = ai.metaData.get("otpless.deeplink.scheme");
            Object hostObj = ai.metaData.get("otpless.deeplink.host");
            String scheme = schemeObj.toString();
            String host = hostObj.toString();
            if (this.apiURl == null || this.apiURl.length() == 0) {
                String packageName = activity.getApplicationContext().getPackageName();
                String domainHost = packageName.replace(".", "-");
                this.apiURl = this.urlDump.replace("*", domainHost);
                ApiManager.getInstance().baseUrl = this.apiURl;
            }
            this.redirectUrl = this.apiURl + "?redirectUri=" + scheme + "://" + host;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getApiURl(Context context) {
        if (this.apiURl != null && this.apiURl.length() > 0) {
            return this.apiURl;
        } else {

            String packageName = context.getApplicationContext().getPackageName();
            String domainHost = packageName.replace(".", "-");
            this.apiURl = this.urlDump.replace("*", domainHost);
            return this.apiURl;
        }
    }

    public void launchOtplessWeb(final OtplessUserDetailCallback callback) {
        this.mOtpImpl.launchOtplessWeb(callback);
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
