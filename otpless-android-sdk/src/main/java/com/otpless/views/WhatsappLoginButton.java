package com.otpless.views;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleObserver;

import com.otpless.R;
import com.otpless.dto.OtplessResponse;
import com.otpless.network.ApiManager;
import com.otpless.utils.SchemeHostMetaInfo;
import com.otpless.utils.Utility;

public class WhatsappLoginButton extends ConstraintLayout implements View.OnClickListener, LifecycleObserver {

    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";

    private String otplessLink = null;
    private TextView mTextView;
    private int mTextSize = 20;
    private int mCornerRadiusInDp = 12;

    private OtplessUserDetailCallback mUserCallback;

    public WhatsappLoginButton(Context context) {
        super(context);
        init(null);
    }

    public WhatsappLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WhatsappLoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // parsing otpless link attribute
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WhatsappLoginButton);
            this.otplessLink = a.getString(R.styleable.WhatsappLoginButton_otpless_link);
            // check redirectUri query in [otplessLink] if not present then new redirection url is added
            // redirectUrl=packagename://otpless, scheme is application package name and host is otpless
            try {
                final Uri otplessUri = Uri.parse(otplessLink);
                final Uri.Builder builder = otplessUri.buildUpon();
                builder.clearQuery();
                final SchemeHostMetaInfo schemeHostMetaInfo = Utility.getSchemeHost(getContext());
                if (schemeHostMetaInfo != null) {
                    final String redirectUri = String.format("%s://%s", schemeHostMetaInfo.getScheme(), schemeHostMetaInfo.getHost());
                    builder.appendQueryParameter("redirectUri", redirectUri);
                    otplessLink = builder.build().toString();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
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
        this.setOnClickListener(this);
    }

    private int getIntFromAttr(final String str) throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException("no value supplied");
        }
        final String trimmed = str.replace("sp", "").replace("dp", "");
        float f = Float.parseFloat(trimmed);
        return (int) f;
    }

    private void addInternalViews(final AttributeSet attr) {
        mTextView = new TextView(getContext(), attr);
        mTextView.setId(View.generateViewId());
        final String text = "Continue with WhatsApp";
        mTextView.setText(text);
        mTextView.setTextColor(Color.WHITE);
        final Typeface typeface = mTextView.getTypeface();
        mTextView.setTypeface(typeface, Typeface.BOLD);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
        mTextView.setAllCaps(false);
        final ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
        );
        final int horPad = getResources().getDimensionPixelSize(R.dimen.otpless_btn_top_margin);
        params.topMargin = horPad;
        params.bottomMargin = horPad;
        // setting constraints
        params.bottomToBottom = this.getId();
        params.topToTop = this.getId();
        params.leftToLeft = this.getId();
        params.rightToRight = this.getId();
        mTextView.setLayoutParams(params);
        addView(mTextView);

        // adding whatsapp image
        final int imageSize = getResources().getDimensionPixelSize(R.dimen.otpless_whatsapp_dim);
        final ConstraintLayout.LayoutParams imgParam = new ConstraintLayout.LayoutParams(
                imageSize, imageSize
        );
        final ImageView whatsappIv = new ImageView(getContext(), attr);
        whatsappIv.setId(View.generateViewId());
        // setting up constraints
        imgParam.topToTop = mTextView.getId();
        imgParam.bottomToBottom = mTextView.getId();
        imgParam.rightToLeft = mTextView.getId();
        imgParam.rightMargin = horPad;
        // setting up layout params
        whatsappIv.setLayoutParams(imgParam);
        // setting drawable
        final Drawable whatsappBg = ContextCompat.getDrawable(getContext(), R.drawable.otpless_whatsapp_icons);
        whatsappIv.setBackground(whatsappBg);
        // adding view in constraint layout
        addView(whatsappIv);
    }

    private void setText(final String text) {
        mTextView.setText(text);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // check if whatsapp package are not installed then mark the view gone
        PackageManager pm = getContext().getPackageManager();
        if (!Utility.isAppInstalled(pm, WHATSAPP_PACKAGE) && !Utility.isAppInstalled(pm, WHATSAPP_BUSINESS_PACKAGE)) {
            this.setVisibility(View.GONE);
            return;
        }
        // base url
        String baseUrl = ApiManager.getInstance().baseUrl;
        if (baseUrl == null || baseUrl.length() == 0 && this.otplessLink != null) {
            final Uri uri = Uri.parse(this.otplessLink);
            baseUrl = uri.getScheme() + "://" + uri.getHost();
            ApiManager.getInstance().baseUrl = baseUrl;
            OtplessManager.getInstance().redirectUrl = this.otplessLink;
        }
    }

    private void onOtplessResult(@Nullable OtplessResponse userDetail) {
        if (this.mUserCallback != null) {
            this.mUserCallback.onOtplessUserDetail(userDetail);
        }
        // set userNumber on button
        if (userDetail != null && userDetail.getUserNumber() != null) {
            setText(userDetail.getUserNumber());
        }
    }

    @Override
    public void onClick(View v) {
        Context context = getContext();
        // case of hilt dependency injection, it will point to wrapper object not activity context
        if (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) getContext()).getBaseContext();
        }
        if(this.otplessLink != null) {
            OtplessManager.getInstance().launch(
                    context, this.otplessLink, this::onOtplessResult
            );
        }
        else if (Utility.isValid(OtplessManager.getInstance().redirectUrl)){
            OtplessManager.getInstance().launch(
                    context, OtplessManager.getInstance().redirectUrl, this::onOtplessResult
            );
        } else {
            OtplessManager.getInstance().launch(
                    context, "", this::onOtplessResult
            );
        }
    }

    public final void setResultCallback(final OtplessUserDetailCallback callback) {
        this.mUserCallback = callback;
    }
}
