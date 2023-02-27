package com.otpless.network;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ApiManager {

    private static ApiManager sInstance;

    private final HandlerThread mNetworkThread;
    private final Handler mHandler;
    private final Handler mUiHandler;

    public String baseUrl = "";

    private final  String META_VERSE = "/metaverse";

    private ApiManager() {
        mNetworkThread = new HandlerThread("OtplessNetworkThread");
        mNetworkThread.start();
        mHandler = new Handler(mNetworkThread.getLooper());
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public static ApiManager getInstance() {
        if (sInstance == null) {
            synchronized (ApiManager.class) {
                if (sInstance != null) {
                    return sInstance;
                }
                sInstance = new ApiManager();
            }
        }
        return sInstance;
    }

    public void verifyWaId(final String waid, final ApiCallback<JSONObject> callback) {
        mHandler.post(() -> {
            try {
                URL url = new URL(baseUrl + META_VERSE);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userId", waid);
                jsonParam.put("api", "getUserDetail");

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // success
                    final InputStream is = conn.getInputStream();
                    final BufferedReader bis = new BufferedReader(new InputStreamReader(is));
                    final StringBuilder writer = new StringBuilder();
                    while (true) {
                        String line = bis.readLine();
                        if (line == null) {
                            break;
                        }
                        writer.append(line);
                    }
                    final String responseStr = writer.toString();
                    JSONObject jsonObject = new JSONObject(responseStr);
                    if (isValidStatus(jsonObject)) {
                        mUiHandler.post(() -> callback.onSuccess(jsonObject));
                        return;
                    }
                    // read the response
                }
                final Exception ex = new Exception("Not 200 response code");
                mUiHandler.post(() -> callback.onError(ex));

            } catch (Exception e) {
                mUiHandler.post(() -> callback.onError(e));
            }
        });
    }

    private boolean isValidStatus(final JSONObject jsonObject) {
        return jsonObject.optBoolean("success");
    }
}
