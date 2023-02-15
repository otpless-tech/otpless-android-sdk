package com.otpless.main;

import static com.otpless.utils.Utility.isNotEmpty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (!Utility.isAppInstalled(getPackageManager(), Configuration.WHATSAPP_PACKAGE_NAME)) {
            returnWithError("whatsapp not installed");
            return;
        }
        if (uri != null && "otpless".equals(uri.getScheme())) {
            checkVerifyOtpless(intent);
            return;
        }
        // setting cancel callback
        mCancelTv.setOnClickListener((v) ->
            returnWithError("user cancelled")
        );
        setupUiFromConfig();

        final String waid = getSharedPreferences("otpless_storage_manager", Context.MODE_PRIVATE).getString("otpless_waid", null);
        if (waid != null) {
            ApiManager.getInstance().verifyWaId(
                    waid, new ApiCallback<JSONObject>() {
                        @Override
                        public void onSuccess(JSONObject data) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("waId", waid);
                            String userNumber = Utility.parseUserNumber(data);
                            resultIntent.putExtra("userNumber", userNumber);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }

                        @Override
                        public void onError(Exception exception) {
                            exception.printStackTrace();
                            Utility.deleteWaId(OtplessLoginActivity.this);
                            openActionView();
                        }
                    }
            );
        } else {
            openActionView();
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
            Integer color = Utility.parseColor(config[2]);
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

    private void openActionView() {
        final Intent intent = getIntent();
        Parcelable parcelable = intent.getParcelableExtra("otpless_request");
        if (parcelable instanceof Uri) {
            Uri request = (Uri) parcelable;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, request);
            startActivity(browserIntent);
        } else {
            finish();
        }
    }

    private void returnWithError(String message) {
        Intent intent = new Intent();
        intent.putExtra("error_message", message);
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    private void checkVerifyOtpless(Intent intent) {
        if (intent == null) return;
        Uri uri = intent.getData();
        if (uri == null) return;
        if (!"otpless".equals(uri.getScheme())) return;
        String waId = uri.getQueryParameter("waId");
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
                        // save waId in share pref
                        SharedPreferences sp = getSharedPreferences("otpless_storage_manager", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("otpless_waid", waId);
                        editor.apply();
                        finish();
                    }

                    @Override
                    public void onError(Exception exception) {
                        exception.printStackTrace();
                        Utility.deleteWaId(OtplessLoginActivity.this);
                        returnWithError(exception.getMessage());
                    }
                }
        );
    }
}