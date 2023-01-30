# android-sdk-otpless

[![](https://jitpack.io/v/otpless-tech/otpless-android-sdk.svg)](https://jitpack.io/#otpless-tech/otpless-android-sdk)


## Download
Use Gradle:
```
repository {
    maven { url "https://jitpack.io" }
}

dependency {
    implementation 'com.otplesslabs:otpless-android-sdk:1.0.0'
}

```


## How to implement OTPless

**AndroidManifest.xml**
```
<activity
    android:name="com.otpless.main.OtplessLoginActivity"
    android:exported="true"
    android:launchMode="singleTop">
        <intent-filter>
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
            <data
                android:host="otpless-provided-host"
                android:scheme="otpless" />
        </intent-filter>
</activity>
```

To add OTPless authentication in Activity named as **LoginActivity.java** with xml file **activity_login.xml**.

*  Add button in your **activity_login.xml**
```
<com.otpless.views.WhatsappLoginButton
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:otpless_link="https://otpless-provided-host.authlink.me?redirectUri=otpless://otpless-provided-host" />
```
Other margin constrainst of buttons can be done in standard way.

* To receive callback in your **LoginActivity.java** add
```
Button button = (Button) findViewById(R.id.logoutBtn);
button.setOnClickListener((v) -> {
    final Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
    finish();
});
```


## License
