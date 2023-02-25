package com.otpless.otplesssample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.otpless.views.OtplessManager;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        OtplessManager.getInstance().init(this);
        Button button = (Button) findViewById(R.id.logoutBtn);
        button.setOnClickListener((v) -> {
            final Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}