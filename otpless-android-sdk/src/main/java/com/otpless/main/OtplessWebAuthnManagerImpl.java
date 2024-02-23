package com.otpless.main;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Base64;

import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.common.Transport;
import com.google.android.gms.fido.fido2.Fido2ApiClient;
import com.google.android.gms.fido.fido2.api.common.Attachment;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorSelectionCriteria;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;
import com.otpless.network.ApiCallback;
import com.otpless.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class OtplessWebAuthnManagerImpl implements OtplessWebAuthnManager {

    private static final int WEBAUTHN_REGISTER_REQUEST_CODE = 9767355;
    private static final int WEBAUTHN_SIGNIN_REQUEST_CODE = 9767356;
    private static final int BASE64_FLAG = Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE;

    private final Fido2ApiClient fidoApiClient;
    private final Activity activity;
    private ApiCallback<String> callback;

    public OtplessWebAuthnManagerImpl(final Activity activity) {
        fidoApiClient = Fido.getFido2ApiClient(activity);
        this.activity = activity;
    }

    public void initRegistration(final JSONObject request, ApiCallback<String> callback) throws JSONException, Attachment.UnsupportedAttachmentException {
        this.callback = callback;
        PublicKeyCredentialCreationOptions publicKey = makePublicKeyCreationOption(request);
        fidoApiClient.getRegisterPendingIntent(publicKey).addOnSuccessListener(pendingIntent -> {
            try {
                activity.startIntentSenderForResult(pendingIntent.getIntentSender(), WEBAUTHN_REGISTER_REQUEST_CODE, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Utility.debugLog(e);
                callback.onError(e);
            }
        }).addOnFailureListener(e -> {
            Utility.debugLog(e);
            callback.onError(e);
        });
    }

    public void initLogin(final JSONObject request, ApiCallback<String> callback) throws JSONException, Transport.UnsupportedTransportException {
        this.callback = callback;
        final PublicKeyCredentialRequestOptions publicKey = makePublicKeyRequestOption(request);
        fidoApiClient.getSignPendingIntent(publicKey).addOnSuccessListener(pendingIntent -> {
            try {
                activity.startIntentSenderForResult(pendingIntent.getIntentSender(), WEBAUTHN_SIGNIN_REQUEST_CODE, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Utility.debugLog(e);
                callback.onError(e);
            }
        }).addOnFailureListener(e -> {
            Utility.debugLog(e);
            callback.onError(e);
        });
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        switch (requestCode) {
            case WEBAUTHN_REGISTER_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (intent == null) {
                        callback.onError(new Exception("error intent data"));
                        return;
                    }
                    byte[] data = intent.getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA);
                    if (data == null) {
                        callback.onError(new Exception("error byte array data"));
                        return;
                    }
                    final PublicKeyCredential credential = PublicKeyCredential.deserializeFromBytes(data);
                    final String error = credential.getResponse().toString();
                    Utility.debugLog("public key credential register: " + error);
                    if (credential.getResponse() instanceof AuthenticatorErrorResponse) {
                        this.callback.onError(new Exception(error));
                    } else {
                        this.callback.onSuccess(encodeBase64(data));
                    }
                    return;
                }
                callback.onError(new Exception("User cancelled"));
                break;
            case WEBAUTHN_SIGNIN_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (intent == null) {
                        callback.onError(new Exception("error intent data"));
                        return;
                    }
                    byte[] data = intent.getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA);
                    if (data == null) {
                        callback.onError(new Exception("error byte array data"));
                        return;
                    }
                    final PublicKeyCredential credential = PublicKeyCredential.deserializeFromBytes(data);
                    final String error = credential.getResponse().toString();
                    Utility.debugLog("public key credential login: " + error);
                    if (credential.getResponse() instanceof AuthenticatorErrorResponse) {
                        this.callback.onError(new Exception(error));
                    } else {
                        this.callback.onSuccess(encodeBase64(data));
                    }
                    return;
                }
                callback.onError(new Exception("User cancelled"));
                break;
        }
    }

    private PublicKeyCredentialCreationOptions makePublicKeyCreationOption(final JSONObject registrationInitData) throws JSONException, Attachment.UnsupportedAttachmentException {
        final PublicKeyCredentialCreationOptions.Builder builder = new PublicKeyCredentialCreationOptions.Builder();
        //region setting authn user and RP
        final JSONObject authUser = registrationInitData.getJSONObject("user");
        final PublicKeyCredentialUserEntity user = new PublicKeyCredentialUserEntity(
                decodeBase64(authUser.getString("id")), authUser.getString("name"), "", authUser.getString("displayName")
        );
        builder.setUser(user);
        final JSONObject rpData = registrationInitData.getJSONObject("rp");
        final PublicKeyCredentialRpEntity rpEntity = new PublicKeyCredentialRpEntity(
                rpData.getString("id"), rpData.getString("name"), null
        );
        builder.setRp(rpEntity);
        //endregion
        // setting challenge
        builder.setChallenge(decodeBase64(registrationInitData.getString("challenge")));
        // setting timeout
        builder.setTimeoutSeconds((double) registrationInitData.getLong("timeout"));
        // setting public credentials
        final ArrayList<PublicKeyCredentialParameters> arrayList = new ArrayList<>();
        final JSONArray pubKeyCreds = registrationInitData.getJSONArray("pubKeyCredParams");
        final int len = pubKeyCreds.length();
        for (int i = 0; i < len; i++) {
            JSONObject cred = pubKeyCreds.getJSONObject(i);
            arrayList.add(new PublicKeyCredentialParameters(cred.getString("type"), cred.getInt("alg")));
        }
        builder.setParameters(arrayList);
        // setting the authentication selection
        final JSONObject authSelection = registrationInitData.optJSONObject("authenticatorSelection");
        if (authSelection != null) {
            final AuthenticatorSelectionCriteria.Builder authenticationBuilder = new AuthenticatorSelectionCriteria.Builder();
            authenticationBuilder.setAttachment(Attachment.fromString(authSelection.getString("authenticatorAttachment")));
        }

        return builder.build();
    }

    private PublicKeyCredentialRequestOptions makePublicKeyRequestOption(final JSONObject initData) throws JSONException, Transport.UnsupportedTransportException {
        PublicKeyCredentialRequestOptions.Builder builder = new PublicKeyCredentialRequestOptions.Builder();
        // setting challenge
        builder.setChallenge(decodeBase64(initData.getString("challenge")));
        // setting allowed credentials
        final JSONArray allowedCredential = initData.optJSONArray("allowCredentials");
        if (allowedCredential != null) {
            final ArrayList<PublicKeyCredentialDescriptor> descriptors = new ArrayList<>();
            final int len = allowedCredential.length();
            for (int i = 0; i < len; i++) {
                JSONObject credential = allowedCredential.getJSONObject(i);
                final ArrayList<Transport> transports = new ArrayList<>();
                final JSONArray jsonArrayTransport = credential.optJSONArray("transports");
                if (jsonArrayTransport != null) {
                    final int tLen = jsonArrayTransport.length();
                    for (int j = 0; j < tLen; j++) {
                        String transport = jsonArrayTransport.getString(j);
                        transports.add(Transport.fromString(transport));
                    }
                }
                final PublicKeyCredentialDescriptor descriptor = new PublicKeyCredentialDescriptor(
                        credential.getString("type"), decodeBase64(credential.getString("id")), transports
                );
                descriptors.add(descriptor);
            }
            builder.setAllowList(descriptors);
        }
        // setting rpId
        builder.setRpId(initData.getString("rpId"));
        final long timeout = initData.getLong("timeout");
        // setting timeout
        builder.setTimeoutSeconds((double) timeout);
        return builder.build();
    }

    private byte[] decodeBase64(final String base64) {
        return Base64.decode(base64, BASE64_FLAG);
    }

    private String encodeBase64(final byte[] data) {
        return Base64.encodeToString(data, BASE64_FLAG);
    }
}
