package com.otpless.network;


import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.otpless.BuildConfig;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;


import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {

    private static ApiManager sInstance;
    private final Retrofit.Builder mRetrofitBuilder;
    private final EventApi eventApi;

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
        final GsonConverterFactory factory = GsonConverterFactory.create(new Gson());
        mRetrofitBuilder = new Retrofit.Builder();
        mRetrofitBuilder.addConverterFactory(new NullOnEmptyConverterFactory());
        mRetrofitBuilder.addConverterFactory(factory);

        // setting push event service
        eventApi = build("https://mtkikwb8yc.execute-api.ap-south-1.amazonaws.com/")
                .create(EventApi.class);
    }

    private Retrofit build(final String baseUrl) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)
                .connectTimeout(40, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        return mRetrofitBuilder.baseUrl(baseUrl)
                .client(builder.build())
                .build();
    }

    public void pushEvents(final JSONObject eventParam, final ApiCallback<JSONObject> callback) {
        final HashMap<String, String> map = new HashMap<>();
        if (eventParam != null) {
            for (final Iterator<String> iter = eventParam.keys(); iter.hasNext(); ) {
                final String key = iter.next();
                final String value = eventParam.optString(key);
                if (key.length() == 0) continue;
                map.put(key, value);
            }
        }
        eventApi.pushEvents(map).enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(@NonNull Call<JSONObject> call, @NonNull Response<JSONObject> response) {
                callback.onSuccess(new JSONObject());
            }

            @Override
            public void onFailure(@NonNull Call<JSONObject> call, @NonNull Throwable t) {
                callback.onError(t);
            }
        });
    }
}

class NullOnEmptyConverterFactory extends Converter.Factory {
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
        return (Converter<ResponseBody, Object>) body -> {
            if (body.contentLength() == 0) return null;
            return delegate.convert(body);
        };
    }
}
