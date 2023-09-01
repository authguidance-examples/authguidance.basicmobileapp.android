package com.authsamples.basicmobileapp.views.userinfo

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.api.client.FetchClient
import com.authsamples.basicmobileapp.views.utilities.ViewModelCoordinator

/*
 * Android plumbing needed to avoid recreating the view model if the view is recreated
 */
class UserInfoViewModelFactory(
    private val authenticator: com.authsamples.basicmobileapp.plumbing.oauth.Authenticator,
    private val fetchClient: FetchClient,
    private val viewModelCoordinator: ViewModelCoordinator,
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return UserInfoViewModel(authenticator, fetchClient, viewModelCoordinator, app) as T
    }
}
