package com.otpless.views;

import static com.otpless.utils.Utility.isNotEmpty;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.otpless.network.ApiManager;
import com.otpless.utils.Utility;

public class OtplessManager {

    private static OtplessManager sInstance = null;
    private String urlDump = "https://*.authlink.me";
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
        this.mOtpImpl.add(activity
        );
    }
    private void setUrlRedirectURI(FragmentActivity activity){
        if (this.redirectUrl != null && this.redirectUrl.length() > 0 && this.apiURl != null && this.apiURl.length() > 0){
            return;
        }
        try {
            ApplicationInfo ai = activity.getPackageManager().getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
            Object schemeObj = (Object)ai.metaData.get("otpless.deeplink.scheme");
            Object hostObj = (Object)ai.metaData.get("otpless.deeplink.host");
            String scheme = schemeObj.toString();
            String host = hostObj.toString();
            if (this.apiURl == null || this.apiURl.length() == 0){
                String packageName = activity.getApplicationContext().getPackageName();
                String domainHost = packageName.replace(".","-");
                this.apiURl = this.urlDump.replace("*",domainHost);
                ApiManager.getInstance().baseUrl = this.apiURl ;
            }
            this.redirectUrl = this.apiURl + "?redirectUri=" + scheme + "://" + host;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    public String getApiURl(Context context){
        if (this.apiURl != null && this.apiURl.length() > 0){
            return this.apiURl;
        } else {

            String packageName = context.getApplicationContext().getPackageName();
            String domainHost = packageName.replace(".","-");
            this.apiURl = this.urlDump.replace("*",domainHost);
            return  this.apiURl;
        }
    }
    public void launch(final Context context, final String link, final OtplessUserDetailCallback callback) {
       if (this.redirectUrl != link){
           this.redirectUrl = link;
           String baseUrl = ApiManager.getInstance().baseUrl;
           if (link != null) {
               final Uri uri = Uri.parse(link);
               baseUrl = uri.getScheme() + "://" + uri.getHost();
               ApiManager.getInstance().baseUrl = baseUrl;
               this.apiURl = uri.getScheme() + "://" + uri.getHost();
           }
       }
        this.mOtpImpl.launch(context, link, callback);
    }
    public void launch(final Context context, final OtplessUserDetailCallback callback) {
        this.mOtpImpl.launch(context,this.redirectUrl,callback);
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
     * NOT RECOMMENDED until and unless these is a specific requirement to clear all session
     */
    public void signOut(final Context context){
        Utility.deleteWaId(context);
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
