package com.otpless.main;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;

public class OtplessManager {

    private static OtplessManager sInstance = null;
    private final ActivityLifeManager lifeManager = new ActivityLifeManager();
    private final HashSet<OtplessViewImpl> providedViewSet = new HashSet<>();

    private boolean isViewRemovalNotifierEnabled = true;

    public static OtplessManager getInstance() {
        if (sInstance == null) {
            synchronized (OtplessManager.class) {
                if (sInstance != null) {
                    return sInstance;
                }
                sInstance = new OtplessManager();
            }
        }
        return sInstance;
    }

    private final OtplessViewRemovalNotifier viewRemovalNotifier = who -> {
        if (!isViewRemovalNotifierEnabled) return;
        // remove all view
        for (final OtplessViewImpl view : providedViewSet) {
            // no changes in current activity and when activity is getting finished
            if (view == who || view.getActivity().isFinishing()) continue;
            view.closeView();
        }
    };

    public OtplessView getOtplessView(final Activity activity) {
        for (final OtplessViewImpl view : providedViewSet) {
            // otpless view is already available
            if (view.getActivity() == activity) {
                return view;
            }
        }
        // create new view
        final OtplessViewImpl view = new OtplessViewImpl(activity);
        activity.getApplication().registerActivityLifecycleCallbacks(lifeManager);
        providedViewSet.add(view);
        view.viewRemovalNotifier = this.viewRemovalNotifier;
        return view;
    }

    public void disableViewScrapping() {
        this.isViewRemovalNotifierEnabled = false;
    }

    private class ActivityLifeManager implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            Application.ActivityLifecycleCallbacks.super.onActivityPostCreated(activity, savedInstanceState);
            for (OtplessViewImpl view : providedViewSet) {
                if (view.getActivity() == activity) {
                    view.registerPhoneHintForResult();
                    break;
                }
            }
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            // only place to remove the otpless view
            for (OtplessViewImpl view : providedViewSet) {
                if (view.getActivity() == activity) {
                    providedViewSet.remove(view);
                    break;
                }
            }
        }
    }
}

interface OtplessViewRemovalNotifier {
    void onOtplessViewRemoved(final OtplessViewImpl who);
}
