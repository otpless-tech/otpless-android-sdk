package com.otpless.utils;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.otpless.main.OtplessLauncher;

import java.util.HashMap;

public class FragmentLauncherProvider {

    private static FragmentLauncherProvider sInstance;
    private static final HashMap<Fragment, OtplessLauncher> mLauncherMap = new HashMap<>();
    public static FragmentLauncherProvider getInstance() {
        if (sInstance == null) {
            synchronized (FragmentLauncherProvider.class) {
                if (sInstance != null) return sInstance;
                sInstance = new FragmentLauncherProvider();
            }
        }
        return sInstance;
    }

    private FragmentLauncherProvider() {
    }

    public void addLauncher(final Fragment fragment, final OtplessLauncher launcher) {
        mLauncherMap.put(fragment, launcher);
    }

    @Nullable
    public OtplessLauncher getLauncher(final Fragment fragment) {
        if (mLauncherMap.containsKey(fragment)) {
            return mLauncherMap.get(fragment);
        }
        return null;
    }

    public void removeLauncher(final Fragment fragment) {
        mLauncherMap.remove(fragment);
    }
}
