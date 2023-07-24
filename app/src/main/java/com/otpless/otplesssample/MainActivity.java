package com.otpless.otplesssample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.otpless.main.OtplessEventCallback;
import com.otpless.main.OtplessEventData;
import com.otpless.views.OtplessManager;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // copy this code in onCreate of your Login Activity
        OtplessManager.getInstance().start(this, data -> {
            if (data.getData() == null) {
                Log.e("OTP-less", data.getErrorMessage());
            } else {
                final JSONObject json = data.getData();
                final String token = json.optString("token");
                if (!token.isEmpty()) {
                    Log.d("OTP-less", String.format("token: %s", token));
                    // todo pass this token to backend to fetch user detail
                }
            }
        });
        // todo remove before mergin
        OtplessManager.getInstance().setEventCallback(
                new OtplessEventCallback() {
                    @Override
                    public void onOtplessEvent(OtplessEventData event) {

                    }

                    @Override
                    public void onInternetError() {
                        Log.d("OtpMain", "Otpless main no internet error");
                        Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}