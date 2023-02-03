package com.otpless.views;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.otpless.R;
import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessResultContract;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;
import com.otpless.utils.Utility;

import org.json.JSONObject;

import java.util.List;

public class WhatsappLoginButton extends ConstraintLayout implements View.OnClickListener, LifecycleObserver {

    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";

    private String otplessLink = null;
    private TextView mTextView;

    private OtplessUserDetailCallback mUserCallback;
    private ActivityResultLauncher<Uri> launcher;

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
            this.otplessLink =  Utility.getUrlWithDeviceParams(getContext().getApplicationContext(),this.otplessLink);
            // parsing host name
            try {
                final Uri uri = Uri.parse(this.otplessLink);
                // base url created
                ApiManager.getInstance().baseUrl = uri.getScheme() + "://" + uri.getHost() + "/";
            } catch (Exception ignore) {
            }
            a.recycle();
        }
        addInternalViews(attrs);
        // setting background and style
        final Drawable background = ContextCompat.getDrawable(getContext(), R.drawable.whatsapp_btn_bg);
        setBackground(background);
        final int horPad = getResources().getDimensionPixelSize(R.dimen.button_padding_horizontal);
        final int verPad = getResources().getDimensionPixelSize(R.dimen.button_padding_vertical);
        setPadding(horPad, verPad, horPad, verPad);
        this.setOnClickListener(this);
    }

    private void addInternalViews(final AttributeSet attr) {
        mTextView = new TextView(getContext(), attr);
        mTextView.setId(View.generateViewId());
        final String text = "Continue with WhatsApp";
        mTextView.setText(text);
        mTextView.setTextColor(Color.WHITE);
        final Typeface typeface = mTextView.getTypeface();
        mTextView.setTypeface(typeface, Typeface.BOLD);
        mTextView.setTextSize(20);
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
        final Drawable whatsappBg = ContextCompat.getDrawable(getContext(), R.drawable.icons8_whatsapp_48);
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
        final Fragment fragment = getCurrentFragment();
        if (fragment != null) {
            launcher = fragment.registerForActivityResult(new OtplessResultContract(), this::onOtplessResult);
        } else if (getContext() instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) getContext();
            launcher = activity.registerForActivityResult(new OtplessResultContract(), this::onOtplessResult);
        }
        // if context is instance of lifecycle
        if (fragment != null) {
            fragment.getViewLifecycleOwner().getLifecycle().addObserver(this);
        } else if (getContext() instanceof LifecycleOwner) {
            final LifecycleOwner owner = (LifecycleOwner) getContext();
            owner.getLifecycle().addObserver(this);
        }
        checkForWaid();
    }

    private void checkForWaid() {
        final String waid = getContext().getSharedPreferences("otpless_storage_manager", Context.MODE_PRIVATE).getString("otpless_waid", null);
        if (waid == null) {
            return;
        }
        ApiManager.getInstance().verifyWaId(
                waid, new ApiCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        final String userNumber = Utility.parseUserNumber(data);
                        if (Utility.isNotEmpty(userNumber)) {
                            setText(userNumber);
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        exception.printStackTrace();
                        Utility.deleteWaId(getContext());
                    }
                }
        );
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
        if (otplessLink != null && otplessLink.length() > 0) {
            try {
                final Uri uri = Uri.parse(otplessLink);
                launcher.launch(uri);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * top fragment will be visible fragment
     * */
    @Nullable
    private Fragment getCurrentFragment() {
        if (getContext() instanceof FragmentActivity) {
            final FragmentActivity activity = (FragmentActivity) getContext();
            final FragmentManager manager = activity.getSupportFragmentManager();
            final List<Fragment> fragments = manager.getFragments();
            if (fragments.size() > 0) {
                return fragments.get(fragments.size() -1);
            }
        }
        return null;
    }

    public final void setResultCallback(final OtplessUserDetailCallback callback) {
        this.mUserCallback = callback;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroyed() {
    }
}
