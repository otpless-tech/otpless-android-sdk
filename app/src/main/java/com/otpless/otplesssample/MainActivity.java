package com.otpless.otplesssample;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.views.OtplessManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OtplessManager.getInstance().start(this, this::onOtplessResult);
    }

    private void onOtplessResult(OtplessResponse data) {
        if (data == null) {
            Toast.makeText(this, "data is null", Toast.LENGTH_LONG).show();
        } else if (data.getData() == null) {
            Toast.makeText(this, data.getErrorMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, data.getData().toString(), Toast.LENGTH_LONG).show();
        }
    }
}