package com.otpless.otplesssample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.biometric.Biometric;
import com.otpless.biometric.OtplessBiometricManager;
import com.otpless.biometric.models.AuthenticateParameters;
import com.otpless.biometric.models.RegisterParameters;
import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessManager;
import com.otpless.main.OtplessView;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


public class MainActivity extends AppCompatActivity {

    OtplessView otplessView;

    private EditText inputEt;
    private TextView outputTv;
    private Button encryptButton, decryptButton;

    private Biometric biometric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // assigning the view
        inputEt = findViewById(R.id.input_token_et);
        outputTv = findViewById(R.id.output_tv);
        encryptButton = findViewById(R.id.encrypt_data_btn);
        decryptButton = findViewById(R.id.decrypt_data_btn);


        try {
            biometric = OtplessBiometricManager.getOtplessBiometric(this);
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        encryptButton.setOnClickListener(v -> {
            if (biometric == null) return;
            String input = inputEt.getText().toString();
            if (input.isEmpty()) return;
            biometric.register(new RegisterParameters(this), input, arg -> {
                String message = "data save: " + arg;
                outputTv.setText(message);
            }, arg -> {
                outputTv.setText(arg.getMessage());
            });
        });

        decryptButton.setOnClickListener(v -> {
            if (biometric == null) return;
            biometric.authenticate(new AuthenticateParameters(this), arg -> {
                String message = "data save: " + arg;
                outputTv.setText(message);
            }, arg -> {
                outputTv.setText(arg.getMessage());
            });
        });


        // copy this code in onCreate of your Login Activity
        otplessView = OtplessManager.getInstance().getOtplessView(this);
//        otplessView.showOtplessLoginPage(this::onOtplessCallback);
        otplessView.setCallback(this::onOtplessCallback, null, true);
        findViewById(R.id.otpless_btn).setOnClickListener(v -> {
            otplessView.showOtplessLoginPage(this::onOtplessCallback);
        });
        findViewById(R.id.sign_in_complete).setOnClickListener(v -> {
            otplessView.onSignInCompleted();
        });
        otplessView.verifyIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        otplessView.verifyIntent(intent);
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