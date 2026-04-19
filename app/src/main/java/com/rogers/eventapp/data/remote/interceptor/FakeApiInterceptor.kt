package com.rogers.eventapp.data.remote.interceptor

import android.content.Context
import com.rogers.eventapp.R
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class FakeApiInterceptor @Inject constructor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return if (request.url.encodedPath.contains("events")) {
            val json = context.resources
                .openRawResource(R.raw.event)
                .bufferedReader()
                .use { it.readText() }

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(json.toResponseBody("application/json".toMediaType()))
                .build()
        } else {
            chain.proceed(request)
        }
    }
}