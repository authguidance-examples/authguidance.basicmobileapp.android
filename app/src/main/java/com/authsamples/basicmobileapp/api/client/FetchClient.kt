package com.authsamples.basicmobileapp.api.client

import com.authsamples.basicmobileapp.api.entities.ApiUserInfo
import com.authsamples.basicmobileapp.api.entities.Company
import com.authsamples.basicmobileapp.api.entities.CompanyTransactions
import com.authsamples.basicmobileapp.api.entities.OAuthUserInfo
import com.authsamples.basicmobileapp.configuration.Configuration
import com.authsamples.basicmobileapp.plumbing.errors.ErrorFactory
import com.authsamples.basicmobileapp.plumbing.oauth.Authenticator
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * Plumbing related to making HTTP calls
 */
class FetchClient(
    private val configuration: Configuration,
    private val authenticator: Authenticator
) {

    // Create a session id for API logs
    val sessionId = UUID.randomUUID().toString()

    /*
     * Get the list of companies
     */
    suspend fun getCompanyList(options: FetchOptions? = null): Array<Company> {

        return this.callApi(
            "${this.configuration.app.apiBaseUrl}/companies",
            "GET",
            null,
            Array<Company>::class.java,
            options
        )
    }

    /*
     * Get the list of transactions for a company
     */
    suspend fun getCompanyTransactions(companyId: String, options: FetchOptions? = null): CompanyTransactions {

        return this.callApi(
            "${this.configuration.app.apiBaseUrl}/companies/$companyId/transactions",
            "GET",
            null,
            CompanyTransactions::class.java,
            options
        )
    }

    /*
     * Download user attributes from the authorization server
     */
    suspend fun getOAuthUserInfo(options: FetchOptions? = null): OAuthUserInfo {

        return this.callApi(
            this.configuration.oauth.userInfoEndpoint,
            "GET",
            null,
            OAuthUserInfo::class.java,
            options
        )
    }

    /*
     * Download user attributes stored in the API's own data
     */
    suspend fun getApiUserInfo(options: FetchOptions? = null): ApiUserInfo {

        return this.callApi(
            "${this.configuration.app.apiBaseUrl}/userinfo",
            "GET",
            null,
            ApiUserInfo::class.java,
            options
        )
    }

    /*
     * The entry point for calling an API in a parameterised manner
     */
    private suspend fun <T> callApi(
        url: String,
        method: String,
        requestData: Any?,
        responseType: Class<T>,
        options: FetchOptions? = null
    ): T {

        // First get an access token
        var accessToken = this.authenticator.getAccessToken()

        // Make the request
        var response = this.callApiWithToken(method, url, requestData, accessToken, options)
        if (response.isSuccessful) {

            // Handle successful responses
            return Gson().fromJson(response.body?.string(), responseType)

        } else {

            // Retry once with a new token if there is a 401 error
            if (response.code == 401) {

                // Try to refresh the access token
                accessToken = this.authenticator.refreshAccessToken()

                // Retry the API call
                response = this.callApiWithToken(method, url, requestData, accessToken, options)
                if (response.isSuccessful) {

                    // Handle successful responses
                    return Gson().fromJson(response.body?.string(), responseType)

                } else {

                    // Handle failed responses on the retry
                    throw ErrorFactory().fromHttpResponseError(response, url, "web API")
                }
            } else {

                // Handle failed responses on the original call
                throw ErrorFactory().fromHttpResponseError(response, url, "web API")
            }
        }
    }

    /*
     * Use the okhttp library to make an async request for data
     */
    private suspend fun callApiWithToken(
        method: String,
        url: String,
        data: Any?,
        accessToken: String,
        options: FetchOptions? = null
    ): Response {

        // Configure the request body
        var body: RequestBody? = null
        if (data != null) {
            val gson = Gson()
            body = gson.toJson(data).toRequestBody("application/json".toMediaType())
        }

        // Build the full request
        val builder = Request.Builder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .method(method, body)
            .url(url)
        this.addCustomHeaders(builder, options)
        val request = builder.build()

        // Create an HTTP client
        val client = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .build()

        return suspendCoroutine { continuation ->

            client.newCall(request).enqueue(object : Callback {

                override fun onResponse(call: Call, response: Response) {
                    continuation.resumeWith(Result.success(response))
                }

                override fun onFailure(call: Call, e: IOException) {
                    val exception = ErrorFactory().fromHttpRequestError(e, url, "web API")
                    continuation.resumeWithException(exception)
                }
            })
        }
    }

    /*
     * Send custom headers to the API for logging purposes
     */
    private fun addCustomHeaders(builder: Request.Builder, options: FetchOptions? = null) {

        builder.header("x-mycompany-api-client", "BasicAndroidApp")
        builder.header("x-mycompany-session-id", this.sessionId)
        builder.header("x-mycompany-correlation-id", UUID.randomUUID().toString())

        // A special header can be sent to thr API to cause a simulated exception
        if (options != null && options.causeError) {
            builder.header("x-mycompany-test-exception", "SampleApi")
        }
    }
}
