package com.otpless.network;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class ApiManager {

    private static ApiManager sInstance;

    private HandlerThread mNetworkThread;
    private Handler mHandler;
    private Handler mUiHandler;

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

    private ApiManager() {
        initNetworkThread();
    }

    private void initNetworkThread() {
        mNetworkThread = new HandlerThread("OtplessNetworkThread");
        mNetworkThread.start();
        mHandler = new Handler(mNetworkThread.getLooper());
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    private void executeCall(final Runnable runnable) {
        if (mNetworkThread.isInterrupted()) {
            initNetworkThread();
        }
        mHandler.post(runnable);
    }

    void post(@NonNull final String mainUrl, @NonNull final JSONObject postData, @NonNull final ApiCallback<JSONObject> callback) {
        executeCall(() -> {
            try {
                final URL url = new URL(mainUrl);
                final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                OutputStream os = conn.getOutputStream();
                os.write(postData.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED ||
                        responseCode == HttpURLConnection.HTTP_ACCEPTED) {
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
                    mUiHandler.post(() -> callback.onSuccess(jsonObject));
                    return;
                    // read the response
                }
                final Exception ex = new Exception("Not 200 response code");
                mUiHandler.post(() -> callback.onError(ex));

            } catch (Exception e) {
                mUiHandler.post(() -> callback.onError(e));
            }
        });
    }

    void get(@NonNull final String mainUrl, @Nullable final JSONObject queryData, @NonNull final ApiCallback<JSONObject> callback) {
        executeCall(() -> {
            try {
                final Uri.Builder builder = Uri.parse(mainUrl).buildUpon();
                if (queryData != null) {
                    for (final Iterator<String> iter = queryData.keys(); iter.hasNext(); ) {
                        final String key = iter.next();
                        final String value = queryData.optString(key);
                        if (key.length() == 0) continue;
                        builder.appendQueryParameter(key, value);
                    }
                }
                final URL url = new URL(builder.build().toString());
                final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED ||
                        responseCode == HttpURLConnection.HTTP_ACCEPTED) {
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
                    final JSONObject jsonObject;
                    if (responseStr.isEmpty()) {
                        jsonObject = new JSONObject();
                    } else {
                        jsonObject = new JSONObject(responseStr);
                    }
                    mUiHandler.post(() -> callback.onSuccess(jsonObject));
                    return;
                    // read the response
                }
                final Exception ex = new Exception("" + responseCode + " response code");
                mUiHandler.post(() -> callback.onError(ex));
            } catch (Exception e) {
                mUiHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void pushEvents(final JSONObject eventParam, final ApiCallback<JSONObject> callback) {
        final String eventUrl = "https://mtkikwb8yc.execute-api.ap-south-1.amazonaws.com/prod/appevent";
        get(eventUrl, eventParam, callback);
    }

    public void apiConfig(final ApiCallback<JSONObject> callback) {
        final String apiConfigUrl = "https://d1j61bbz9a40n6.cloudfront.net/sdk/config/prod/config.json";
        get(apiConfigUrl, null, callback);
    }
}
