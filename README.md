
<p align="center">
  <img src="https://github.com/otpless-tech/otpless-android-sdk/blob/Images-and-readme-changes/otpless.svg" height="220"/>
</p>
<p align="center">
  <img src="https://github.com/otpless-tech/otpless-android-sdk/blob/Images-and-readme-changes/android-icon.svg" height="50" width "50"/>
</p>


[![](https://jitpack.io/v/otpless-tech/otpless-android-sdk.svg)](https://jitpack.io/#otpless-tech/otpless-android-sdk)

# android-sdk-otpless
Otpless authentication sdk for android


## Download
Use Gradle:
```
repository {
    maven { url "https://jitpack.io" }
}

dependency {
    implementation 'com.otplesslabs:otpless-android-sdk:LATEST-TAG'
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
