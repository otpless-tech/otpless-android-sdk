package com.otpless.otplesssample;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.HeadlessRequest;
import com.otpless.dto.HeadlessChannel;
import com.otpless.dto.HeadlessResponse;
import com.otpless.dto.HeadlessChannelType;
import com.otpless.dto.OtplessRequest;
import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessManager;
import com.otpless.main.OtplessView;


public class MainActivity extends AppCompatActivity {

    OtplessView otplessView;

    private EditText inputEditText, otpEditText;

    private HeadlessChannelType channelType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTestingView();
        // copy this code in onCreate of your Login Activity
        otplessView = OtplessManager.getInstance().getOtplessView(this);
        otplessView.setHeadlessCallback(getHeadlessRequest(), this::onHeadlessCallback);
        findViewById(R.id.headless_sdk_btn).setOnClickListener(v -> {
            otplessView.startHeadless(getHeadlessRequest(), this::onHeadlessCallback);
        });
        findViewById(R.id.show_login_btn).setOnClickListener(v -> {
            final OtplessRequest request = new OtplessRequest("870OD5RME1UBYVDJPKL3");
            otplessView.setCallback(request, this::onOtplessCallback, true);
            otplessView.showOtplessLoginPage(request, this::onOtplessCallback);
        });
        findViewById(R.id.show_floater_btn).setOnClickListener(v -> {
            final OtplessRequest request = new OtplessRequest("870OD5RME1UBYVDJPKL3");
            otplessView.setCallback(request, this::onOtplessCallback, false);
            otplessView.showOtplessFab(false);
            otplessView.startOtpless(request, this::onOtplessCallback);
        });
        Log.d("Otpless", "Verify intent from onCreate");
        otplessView.verifyIntent(getIntent());
    }

    private HeadlessRequest getHeadlessRequest() {
        final HeadlessRequest request = new HeadlessRequest();
        request.setAppId("870OD5RME1UBYVDJPKL3");
        final String input = inputEditText.getText().toString();

        if (!input.isEmpty()) {
            try {
                // parse phone number
                Long.parseLong(input);
                request.setPhoneNumber("+91", input);
            } catch (Exception ex) {
                request.setEmail(input);
            }
        } else {
            request.setChannelType(this.channelType);
        }
        final String otp = otpEditText.getText().toString();
        if (!otp.isEmpty()) request.setOtp(otp);

        return request;
    }


    private void initTestingView() {
        otpEditText = findViewById(R.id.otp_et);
        RadioGroup channelRadioGroup = findViewById(R.id.channel_type_rg);
        channelRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.whatsapp_rb:
                    channelType = HeadlessChannelType.WHATSAPP;
                    break;
                case R.id.gmail_rb:
                    channelType = HeadlessChannelType.GMAIL;
                    break;
                case R.id.twitter_rb:
                    channelType = HeadlessChannelType.TWITTER;
                    break;
                case R.id.slack_rb:
                    channelType = HeadlessChannelType.SLACK;
                    break;
                case R.id.facebook_rb:
                    channelType = HeadlessChannelType.FACEBOOK;
                    break;
                case R.id.linkedin_rb:
                    channelType = HeadlessChannelType.LINKEDIN;
                    break;
                case R.id.microsoft_rb:
                    channelType = HeadlessChannelType.MICROSOFT;
                    break;
            }
            otplessView.setHeadlessCallback(getHeadlessRequest(), this::onHeadlessCallback);
        });
        inputEditText = findViewById(R.id.input_text_layout);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("Otpless", "Verify intent from onNewIntent");
        otplessView.verifyIntent(intent);
    }

    private void onHeadlessCallback(@NonNull final HeadlessResponse response) {
        String message;
        if (response.getError() == null) {
            message = response.getData().toString();
        } else {
            message = response.getError();
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void onOtplessCallback(OtplessResponse response) {
        if (response.getErrorMessage() != null) {
            Toast.makeText(this, response.getErrorMessage(), Toast.LENGTH_LONG).show();
        } else {
            final String token = response.getData().optString("token");
            Toast.makeText(this, token, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (otplessView.onBackPressed()) return;
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}