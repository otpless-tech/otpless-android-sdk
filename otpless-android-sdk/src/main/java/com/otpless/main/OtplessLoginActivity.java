package com.otpless.main;

import static com.otpless.utils.Utility.isNotEmpty;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.otpless.R;
import com.otpless.config.Configuration;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;
import com.otpless.utils.Utility;
import com.otpless.views.OtplessManager;

import org.json.JSONObject;


public class OtplessLoginActivity extends AppCompatActivity {

    private TextView mCancelTv;
    private Bundle mOnCreateBundle = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOnCreateBundle = savedInstanceState;
        setContentView(R.layout.activity_otpless_login);
        initView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) return;
        checkVerifyOtpless(intent);
    }

    private void initView() {
        mCancelTv =  findViewById(R.id.cancel_tv);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if (!Utility.isAppInstalled(getPackageManager(), Configuration.WHATSAPP_PACKAGE_NAME) && !Utility.isAppInstalled(getPackageManager(), Configuration.WHATSAPP_BUSINESS_PACKAGE)) {
            returnWithError("whatsapp not installed");
            return;
        }
        // setting cancel callback
        mCancelTv.setOnClickListener((v) ->
            returnWithError("user cancelled")
        );
        setupUiFromConfig();

        // check deeplink has waId in query and
        final Uri actionUri = getActionUri();
        if (actionUri != null) {
            // open whats app uri
            if (mOnCreateBundle == null) {
                // activity is not restored and on new intent will not be called
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, actionUri);
                startActivity(browserIntent);
            }
        } else {
            returnWithError("redirecturi is null");
        }
    }

    private void setupUiFromConfig() {
        final String[] config = OtplessManager.getInstance().getConfiguration(this);
        if (isNotEmpty(config[0])) {
            // setup background color
            Integer color = Utility.parseColor(config[0]);
            if (color != null) {
                final ConstraintLayout cl = findViewById(R.id.otpless_parent_cl);
                cl.setBackgroundColor(color);
            }
        }
        if (isNotEmpty(config[1])) {
            // setup loader color
            Integer color = Utility.parseColor(config[1]);
            if (color != null) {
                ProgressBar bar = findViewById(R.id.otpless_progress_bar);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                    bar.setIndeterminateTintList(ColorStateList.valueOf(color));
                }
            }
        }

        final TextView textView = findViewById(R.id.otpless_msg_tv);
        if (isNotEmpty(config[2])) {
            // setup loader text
            textView.setText(config[2]);
        }
        if (isNotEmpty(config[3])) {
            Integer color = Utility.parseColor(config[3]);
            if (color != null) {
                textView.setTextColor(color);
            }
        }

        if (isNotEmpty(config[4])) {
            // setup cancel button text
            mCancelTv.setText(config[4]);
        }
        if (isNotEmpty(config[5])) {
            // setup cancel button color
            Integer color = Utility.parseColor(config[5]);
            if (color != null) {
                mCancelTv.setTextColor(color);
            }
        }
    }

    @Nullable
    private Uri getActionUri() {
        final Intent intent = getIntent();
        Parcelable parcelable = intent.getParcelableExtra("otpless_request");
        if (parcelable != null && parcelable instanceof Uri) {
            return (Uri) parcelable;
        }
        return null;
    }

    private void returnWithError(String message) {
        Intent intent = new Intent();
        intent.putExtra("error_message", message);
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    private void checkVerifyOtpless(Intent intent) {
        if (intent == null){
            returnWithError("Intent is null");
            return;
        }

        Uri uri = intent.getData();
        if (uri == null){
            returnWithError("Uri is null");
            return;
        }

        String waId = uri.getQueryParameter("waId");
        if (waId == null || waId.length() == 0){
            returnWithError("Waid is null");
            return;
        }
        mCancelTv.setVisibility(View.GONE);
        // check the validity of waId with otpless
        ApiManager.getInstance().verifyWaId(
                waId, new ApiCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("waId", waId);
                        String userNumber = Utility.parseUserNumber(data);
                        resultIntent.putExtra("userNumber", userNumber);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }

                    @Override
                    public void onError(Exception exception) {
                        exception.printStackTrace();
                        returnWithError(exception.getMessage());
                    }
                }
        );
    }
}