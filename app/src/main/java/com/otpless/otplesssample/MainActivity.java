package com.otpless.otplesssample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.utils.Utility;
import com.otpless.views.OtplessManager;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OtplessManager.getInstance().init(this);
        Button button = (Button) findViewById(R.id.whatsapp_login);
        button.setOnClickListener(v -> {
            openOtpless();
        });
        CheckBox checkBox = (CheckBox) findViewById(R.id.show_btn_cb);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            OtplessManager.getInstance().showFabButton(isChecked);
        });
        openOtpless();
    }

    private void openOtpless() {
        final JSONObject params = new JSONObject();
        try {
            params.put("method", "get");
            final JSONObject data = new JSONObject();
            data.put("name", "digvijay");
            data.put("lastName", "singh");
            data.put("hobby", "wandering");
            params.put("params", data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        OtplessManager.getInstance().start(this::onOtplessResult, params);
    }

    private void afterSessionId() {
        final Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void onOtplessResult(OtplessResponse data) {
        if (data == null) {
            Toast.makeText(this, "data is null", Toast.LENGTH_LONG).show();
            return;
        } else if (data.getData() == null) {
            Toast.makeText(this, data.getErrorMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, data.getData().toString(), Toast.LENGTH_LONG).show();
        }
    }
}