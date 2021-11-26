package com.zaeem.myapplication.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE
import androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.zaeem.myapplication.R
import com.zaeem.myapplication.common.enums.ConversationsError
import com.zaeem.myapplication.common.extensions.applicationContext
import com.zaeem.myapplication.common.extensions.getErrorMessage
import com.zaeem.myapplication.common.extensions.lazyActivityViewModel
import com.zaeem.myapplication.common.injector
import com.zaeem.myapplication.databinding.FragmentProfileBinding
import com.zaeem.myapplication.ui.LoginActivity
import com.zaeem.myapplication.ui.dialogs.EditProfileDialog
import timber.log.Timber

class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding

    val profileViewModel by lazyActivityViewModel { injector.createProfileViewModel(applicationContext) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.title_profile)

        profileViewModel.selfUser.observe(viewLifecycleOwner) { user ->
            binding.profileName.text = user.friendlyName
            binding.profileIdentity.text = user.identity
        }

        profileViewModel.onUserUpdated.observe(viewLifecycleOwner) {
            Snackbar.make(view, R.string.profile_updated, Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.bottom_navigation)
                .show()
        }

        profileViewModel.onSignedOut.observe(viewLifecycleOwner) {
            LoginActivity.start(requireContext(), ConversationsError.SIGN_OUT_SUCCEEDED)
        }

        profileViewModel.onError.observe(viewLifecycleOwner) { error ->
            val message = requireContext().getErrorMessage(error)
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.bottom_navigation)
                .setAction(R.string.retry) { showEditProfileDialog() }
                .show()
        }

        binding.editProfile.setOnClickListener { showEditProfileDialog() }
        binding.signOut.setOnClickListener { showSignOutDialog() }
    }

    fun showEditProfileDialog() = EditProfileDialog().showNow(childFragmentManager, null)

    private fun showSignOutDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.sign_out_dialog_title)
            .setMessage(R.string.sign_out_dialog_message)
            .setPositiveButton(R.string.sign_out) { _, _ -> profileViewModel.signOut() }
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            val color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
            dialog.getButton(BUTTON_POSITIVE).setTextColor(color)

            dialog.getButton(BUTTON_NEGATIVE).isAllCaps = false
            dialog.getButton(BUTTON_POSITIVE).isAllCaps = false
        }

        dialog.show()
    }
}
