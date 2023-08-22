package com.otpless.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otpless.R;

public class OtplessLoader extends FrameLayout {
    public OtplessLoader(@NonNull Context context) {
        super(context);
    }

    public OtplessLoader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OtplessLoader(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView(AttributeSet attrs) {
        final View loader = LayoutInflater.from(getContext()).inflate(R.layout.otpless_loader, this, false);
        addView(loader);
    }
}
