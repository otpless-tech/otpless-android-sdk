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
                mSnackBar.showText("You are not connected to internet.", "#FF9494", 0);
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
            switch (statusData.getStatus()) {
                case ENABLED:
                    if (mSnackBar != null) {
                        mSnackBar.showText("Back to online mode.", "#23D366", 5_000);
                    }
                    break;
                case DISABLED:
                    if (mSnackBar != null) {
                        mSnackBar.showText("You are not connected to internet.", "#FF9494", 0);
                    }
                    break;
            }
        });
    }
}
