package com.otpless.main

import android.content.Context
import id.tru.sdk.TruSDK
import org.json.JSONObject
import java.net.URL

internal object OtplessTruIdManager {

    @JvmStatic
    fun openWithDataCellular(appContext: Context, urlDest: String, isDebug: Boolean = false): JSONObject {
        TruSDK.initializeSdk(appContext)
        val url = URL(urlDest)
        return TruSDK.getInstance().openWithDataCellular(url, isDebug)
    }

    @JvmStatic
    fun openWithDataCellularAndAccessToken(
        appContext: Context, urlDest: String, accessToken: String, isDebug: Boolean = false
    ): JSONObject {
        TruSDK.initializeSdk(appContext)
        val url = URL(urlDest)
        return TruSDK.getInstance().openWithDataCellularAndAccessToken(
            url, accessToken, isDebug
        )
    }
}