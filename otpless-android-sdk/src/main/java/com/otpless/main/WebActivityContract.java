package com.otpless.main;

import android.view.ViewGroup;

import org.json.JSONObject;

public interface WebActivityContract {
    ViewGroup getParentView();
    JSONObject getExtraParams();
}
