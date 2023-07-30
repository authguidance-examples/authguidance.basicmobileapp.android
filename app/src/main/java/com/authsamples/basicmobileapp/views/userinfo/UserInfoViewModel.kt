package com.authsamples.basicmobileapp.views.userinfo

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel
import com.authsamples.basicmobileapp.api.client.ApiClient
import com.authsamples.basicmobileapp.api.client.ApiRequestOptions
import com.authsamples.basicmobileapp.api.entities.ApiUserInfo
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.plumbing.oauth.Authenticator
import com.authsamples.basicmobileapp.plumbing.oauth.OAuthUserInfo
import com.authsamples.basicmobileapp.views.utilities.ApiViewEvents
import com.authsamples.basicmobileapp.views.utilities.Constants.VIEW_USERINFO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * A simple view model class for the user info view
 */
class UserInfoViewModel(
    val authenticator: Authenticator,
    val apiClient: ApiClient,
    val apiViewEvents: ApiViewEvents
) : ViewModel(), Observable {

    // Observable data for which the UI must be notified upon change
    private var oauthUserInfo: OAuthUserInfo? = null
    private var apiUserInfo: ApiUserInfo? = null
    private val callbacks = PropertyChangeRegistry()

    /*
     * A method to do the work of calling the API
     */
    fun callApi(
        options: UserInfoLoadOptions,
        onError: (UIError) -> Unit
    ) {

        // Return if we already have user info, unless we are doing a reload
        if (this.isLoaded() && !options.reload) {
            this.apiViewEvents.onViewLoaded(VIEW_USERINFO)
            return
        }

        // Indicate a loading state
        this.apiViewEvents.onViewLoading(VIEW_USERINFO)

        // Make the remote call on a background thread
        val that = this@UserInfoViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Make the API call
                val apiClient = that.apiClient
                val requestOptions = ApiRequestOptions(options.causeError)

                // The UI gets OAuth user info from the authorization server
                val oauthUserInfo = authenticator.getUserInfo()

                // The UI gets domain specific user attributes from its API
                val apiUserInfo = apiClient.getUserInfo(requestOptions)

                // Indicate success
                withContext(Dispatchers.Main) {
                    that.setUserInfo(oauthUserInfo, apiUserInfo)
                    that.apiViewEvents.onViewLoaded(VIEW_USERINFO)
                }
            } catch (uiError: UIError) {

                // Inform the view so that the error can be reported
                withContext(Dispatchers.Main) {
                    that.apiViewEvents.onViewLoadFailed(VIEW_USERINFO, uiError)
                    onError(uiError)
                    that.clearUserInfo()
                }
            }
        }
    }

    /*
     * Markup calls this method to get the logged in user's display name
     */
    fun getLoggedInUser(): String {

        if (this.oauthUserInfo == null) {
            return ""
        }

        var name = "${this.oauthUserInfo!!.given_name} ${this.oauthUserInfo!!.family_name}"
        if (this.apiUserInfo?.role == "admin") {
            name += " (ADMIN)"
        }

        return name
    }

    /*
     * Clear user info when we log out and inform the binding system
     */
    fun clearUserInfo() {
        this.oauthUserInfo = null
        this.apiUserInfo = null
        callbacks.notifyCallbacks(this, 0, null)
    }

    /*
     * Observable plumbing to allow XML views to register
     */
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    /*
     * Observable plumbing to allow XML views to unregister
     */
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }

    /*
     * Set user info and inform the binding system
     */
    private fun setUserInfo(oauthUserInfo: OAuthUserInfo, apiUserInfo: ApiUserInfo) {
        this.oauthUserInfo = oauthUserInfo
        this.apiUserInfo = apiUserInfo
        callbacks.notifyCallbacks(this, 0, null)
    }

    /*
     * Determine whether we need to load data
     */
    private fun isLoaded(): Boolean {
        return this.oauthUserInfo != null && this.apiUserInfo != null
    }
}
