package com.otpless.otplesssample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.utils.Utility;
import com.otpless.views.OtplessManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OtplessManager.getInstance().init(this);
        Button button = (Button) findViewById(R.id.whatsapp_login);
        button.setOnClickListener(v -> {
            OtplessManager.getInstance().start(this::onOtplessResult);
        });
    }

    private void afterSessionId() {
        final Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void onOtplessResult(@Nullable OtplessResponse data) {
        if (data == null) {
            Toast.makeText(this, "data is null", Toast.LENGTH_LONG).show();
            return;
        }
        if (Utility.isNotEmpty(data.getWaId())) {
            afterSessionId();
        }
    }


}