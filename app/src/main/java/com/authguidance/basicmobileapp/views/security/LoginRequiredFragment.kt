package com.authguidance.basicmobileapp.views.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.databinding.FragmentLoginRequiredBinding
import com.authguidance.basicmobileapp.plumbing.events.NavigatedEvent
import org.greenrobot.eventbus.EventBus

/*
 * The fragment to indicate that a login is required
 */
class LoginRequiredFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentLoginRequiredBinding

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout
        this.binding = FragmentLoginRequiredBinding.inflate(inflater, container, false)

        // Notify that the main view has changed
        println("GJA: Login required navigated")
        EventBus.getDefault().post(NavigatedEvent(false));
        return binding.root
    }
}
