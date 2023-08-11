package com.otpless.otplesssample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.otpless.main.OtplessManager;
import com.otpless.main.OtplessView;


public class MainActivity extends AppCompatActivity {

    OtplessView otplessView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // copy this code in onCreate of your Login Activity

        otplessView = OtplessManager.getInstance().getOtplessView(this);
        otplessView.startOtpless(null);
    }

    @Override
    public void onBackPressed() {
        if (otplessView.onBackPressed()) return;
        super.onBackPressed();
    }
}