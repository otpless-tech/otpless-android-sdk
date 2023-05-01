package com.otpless.main;

import androidx.cardview.widget.CardView;

import org.json.JSONObject;

public interface WebActivityContract {
    CardView getParentView();
    JSONObject getExtraParams();
}
