package com.otpless.otplesssample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.utils.Utility;
import com.otpless.views.OtplessManager;
import com.otpless.views.WhatsappLoginButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OtplessManager.getInstance().init(this);
        WhatsappLoginButton button = (WhatsappLoginButton) findViewById(R.id.whatsapp_login);
        button.setResultCallback((data) -> {
            if (Utility.isNotEmpty(data.getWaId())) {
                afterSessionId();
            }
        });
    }

    private void afterSessionId() {
        final Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void onOtplessResult(@Nullable OtplessResponse userDetail) {
        if (userDetail == null) return;
        String message = userDetail.toString();
        message = userDetail.getWaId() + "\n" + message;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d("MainActivity", message);
    }


}