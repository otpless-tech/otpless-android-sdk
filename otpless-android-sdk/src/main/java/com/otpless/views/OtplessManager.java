package com.otpless.views;

import static com.otpless.utils.Utility.getSchemeHost;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.otpless.network.ApiManager;
import com.otpless.utils.SchemeHostMetaInfo;
import com.otpless.utils.Utility;

import org.json.JSONObject;

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
        this.mOtpImpl.initWebLauncher(activity);
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

    public void start(final OtplessUserDetailCallback callback) {
        this.mOtpImpl.startOtpless(callback, null);
    }

    public void start(final OtplessUserDetailCallback callback, @NonNull final JSONObject params) {
        this.mOtpImpl.startOtpless(callback, params);
    }
}
