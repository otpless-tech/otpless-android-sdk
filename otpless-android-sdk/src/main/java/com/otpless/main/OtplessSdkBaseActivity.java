package com.otpless.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.network.NetworkStatusData;
import com.otpless.network.ONetworkStatus;
import com.otpless.network.OnConnectionChangeListener;
import com.otpless.network.OtplessNetworkManager;

abstract class OtplessSdkBaseActivity extends AppCompatActivity implements OnConnectionChangeListener {

    private NetworkSnackBar mSnackBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null && getWindow().getDecorView() != null) {
            mSnackBar = NetworkSnackBar.createView(getWindow().getDecorView());
        }
        OtplessNetworkManager.getInstance().addListeners(this, this);
        if (OtplessNetworkManager.getInstance().getNetworkStatus().getStatus() == ONetworkStatus.DISABLED) {
            if (mSnackBar != null) {
                mSnackBar.showText("You are not connected to internet.", null, 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OtplessNetworkManager.getInstance().removeListener(this, this);
    }

    @Override
    public void onConnectionChange(final NetworkStatusData statusData) {
        runOnUiThread(() -> {
            if (mSnackBar == null) return;
            if (statusData.getStatus() == ONetworkStatus.DISABLED) {
                mSnackBar.showText("You are not connected to internet.", null, 0);
            } else if (statusData.getStatus() == ONetworkStatus.ENABLED) {
                mSnackBar.remove();
            }
        });
    }
}
