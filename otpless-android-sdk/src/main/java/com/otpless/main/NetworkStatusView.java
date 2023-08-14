package com.otpless.main;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.otpless.R;

public class NetworkStatusView extends FrameLayout {

    @NonNull
    private String text = "";

    private TextView mTextView;
    private int textColorCode;

    public NetworkStatusView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public NetworkStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NetworkStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NetworkStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(final Context context) {
        // setting layout params
        final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP;
        this.setLayoutParams(params);
        // setting background color
        this.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_snack_bar_no_internet));
        // default color code is black
        this.textColorCode = Color.parseColor("#FFFFFF");
        // adding view
        final View view = LayoutInflater.from(context).inflate(R.layout.no_network_view, this, false);
        mTextView = view.findViewById(R.id.otpless_no_internet_tv);
        this.addView(view);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (text.isEmpty()) return;
        mTextView.setTextColor(textColorCode);
        mTextView.setText(text);
    }

    public void setText(final String text, final Integer textColorCode) {
        this.text = text;
        if (textColorCode != null) {
            this.textColorCode = textColorCode;
        }
        if (mTextView != null) {
            mTextView.setTextColor(this.textColorCode);
            mTextView.setText(text);
        }
    }
}
