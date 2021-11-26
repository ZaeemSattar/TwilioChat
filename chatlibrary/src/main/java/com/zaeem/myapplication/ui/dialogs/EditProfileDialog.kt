package com.zaeem.myapplication.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.zaeem.myapplication.R
import com.zaeem.myapplication.common.extensions.applicationContext
import com.zaeem.myapplication.common.extensions.enableErrorResettingOnTextChanged
import com.zaeem.myapplication.common.extensions.lazyActivityViewModel
import com.zaeem.myapplication.common.extensions.onSubmit
import com.zaeem.myapplication.common.injector
import com.zaeem.myapplication.databinding.DialogEditProfileBinding

class EditProfileDialog : BaseBottomSheetDialogFragment() {

    lateinit var binding: DialogEditProfileBinding

    val profileViewModel by lazyActivityViewModel { injector.createProfileViewModel(applicationContext) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileFriendlyNameInputHolder.enableErrorResettingOnTextChanged()
        binding.profileFriendlyNameInput.onSubmit { saveChanges() }
        binding.saveChanges.setOnClickListener { saveChanges() }
        binding.cancelButton.setOnClickListener { dismiss() }
    }

    private fun saveChanges() {
        val friendlyName = binding.profileFriendlyNameInput.text.toString()
        if (friendlyName.isBlank()) {
            binding.profileFriendlyNameInputHolder.error = getString(R.string.profile_friendly_name_error_text)
            return
        }

        profileViewModel.setFriendlyName(friendlyName)
        dismiss()
    }
}
