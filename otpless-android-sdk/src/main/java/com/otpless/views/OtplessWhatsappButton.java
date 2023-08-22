package com.otpless.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.otpless.R;

public class OtplessWhatsappButton extends  WhatsappLoginButton{

    public OtplessWhatsappButton(Context context) {
        super(context);
    }

    public OtplessWhatsappButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OtplessWhatsappButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(AttributeSet attrs) {
        // parsing otpless link attribute
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WhatsappLoginButton);
            String size = a.getString(R.styleable.WhatsappLoginButton_otplessTextSize);
            try {
                mTextSize = getIntFromAttr(size);
            } catch (IllegalArgumentException ignore) {
            }
            String radiusStr = a.getString(R.styleable.WhatsappLoginButton_otplessCornerRadius);

            try {
                mCornerRadiusInDp = getIntFromAttr(radiusStr);
            } catch (Exception ignore) {}
            a.recycle();
        }
        addInternalViews(attrs);
        // setting background and style
        final GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(getContext(), R.color.otpless_primary));
        // convert corner radius from dp to float
        final DisplayMetrics matrics = getContext().getResources().getDisplayMetrics();
        final float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mCornerRadiusInDp, matrics);
        drawable.setCornerRadius(radius);
        setBackground(drawable);
        final int horPad = getResources().getDimensionPixelSize(R.dimen.button_padding_horizontal);
        final int verPad = getResources().getDimensionPixelSize(R.dimen.button_padding_vertical);
        setPadding(horPad, verPad, horPad, verPad);
    }
}
