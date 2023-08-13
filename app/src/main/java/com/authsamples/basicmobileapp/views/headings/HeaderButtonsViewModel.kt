package com.authsamples.basicmobileapp.views.headings

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel

/*
 * A simple view model class for the header buttons fragment
 */
class HeaderButtonsViewModel : ViewModel(), Observable {

    // Observable data for which the UI must be notified upon change
    private var hasData = false
    private val callbacks = PropertyChangeRegistry()

    /*
     * The binding system calls this and only enables buttons when we have data
     */
    fun hasData(): Boolean {
        return this.hasData
    }

    /*
     * The enabled state is updated during API calls and when logging out
     */
    fun updateDataStatus(hasData: Boolean) {
        this.hasData = hasData
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
}
