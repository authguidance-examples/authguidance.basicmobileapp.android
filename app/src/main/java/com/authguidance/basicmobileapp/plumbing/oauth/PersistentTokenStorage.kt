package com.authguidance.basicmobileapp.plumbing.oauth

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson

/*
 * In Android there is no suitable operating system secure storage so we use shared preferences with encryption
 * https://github.com/okta/okta-oidc-android/blob/master/library/src/main/java/com/okta/oidc/storage/security/DefaultEncryptionManager.java
 */
class PersistentTokenStorage(val context: Context, val encryptionManager: EncryptionManager) {

    private val applicationName = "com.authguidance.basicmobileapp"
    private val key = "AUTH_STATE"
    private val tokenStorage = context.getSharedPreferences(applicationName, MODE_PRIVATE)
    private val lock = Object()

    /*
     * Load tokens from storage and note that the shared preferences API stores them in memory afterwards
     */
    fun loadTokens(): TokenData? {

        synchronized(lock) {
            return this.loadTokenData()
        }
    }

    /*
     * Save tokens to stored preferences
     */
    fun saveTokens(data: TokenData) {

        synchronized(lock) {
            this.saveTokenData(data)
        }
    }

    /*
     * Remove tokens from storage
     */
    fun removeTokens() {

        synchronized(lock) {
            this.tokenStorage.edit().remove(this.key).apply()
        }
    }

    /*
     * Remove just the access token from storage
     */
    fun clearAccessToken() {

        synchronized(lock) {
            val tokenData = loadTokenData()
            if (tokenData != null) {
                tokenData.accessToken = null
                this.saveTokenData(tokenData)
            }
        }
    }

    /*
     * A hacky method for testing, to update token storage to make the access token act like it is expired
     */
    fun expireAccessToken() {

        synchronized(lock) {
            val tokenData = loadTokenData()
            if (tokenData != null) {
                tokenData.accessToken = "x${tokenData.accessToken}x"
                this.saveTokenData(tokenData)
            }
        }
    }

    /*
     * A hacky method for testing, to update token storage to make the refresh token act like it is expired
     */
    fun expireRefreshToken() {

        synchronized(lock) {
            val tokenData = loadTokenData()
            if (tokenData != null) {
                tokenData.accessToken = null
                tokenData.refreshToken = "x${tokenData.refreshToken}x"
                this.saveTokenData(tokenData)
            }
        }
    }

    /*
     * Encrypt tokens and save to stored preferences
     */
    private fun saveTokenData(data: TokenData) {
        val gson = Gson()
        val json = gson.toJson(data)
        this.tokenStorage.edit().putString(this.key, encryptionManager.encrypt(json)).apply()
    }

    /*
     * Load token data from storage and decrypt
     */
    private fun loadTokenData(): TokenData? {

        val data = this.tokenStorage.getString(this.key, "")
        if (data.isNullOrBlank()) {
            return null
        }

        val gson = Gson()
        return gson.fromJson(encryptionManager.decrypt(data), TokenData::class.java)
    }
}