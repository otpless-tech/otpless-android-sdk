package com.otpless.otplesssample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.views.OtplessManager;
import com.otpless.views.OtplessWhatsappButton;

public class MainActivity extends AppCompatActivity {

    public static String Tag = "OTPless-main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // optional but mandatory for 100% working
        OtplessManager.registerCallback(this, this::onOtplessResult);

        OtplessWhatsappButton button = (OtplessWhatsappButton) findViewById(R.id.whatsapp_login);
        button.setOnClickListener(v -> {
            OtplessManager.openOtpless(this, Uri.parse("https://anubhav.authlink.me"));
        });
        OtplessManager.verify(this, getIntent(), this::onOtplessResult);

//        OtplessManager.setRedirectUrl("newschemeinmanifest://newhost");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        OtplessManager.verify(this, intent, this::onOtplessResult);
    }

    @Override
    public void onBackPressed() {
        if (OtplessManager.onBackPressed(this)) return;
        super.onBackPressed();
    }

    private void afterSessionId() {
        final Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void onOtplessResult(@Nullable OtplessResponse userDetail) {
        if (userDetail == null || userDetail.getWaId() == null) {
            // todo handle error cases
            return;
        }
        final String waId = userDetail.getWaId();
        Toast.makeText(this, userDetail.getWaId() + " " + userDetail.getUserNumber(), Toast.LENGTH_LONG).show();
        // todo with api work
    }
}