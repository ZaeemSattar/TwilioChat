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
import com.zaeem.myapplication.databinding.DialogNewConversationBinding

class NewConversationDialog : BaseBottomSheetDialogFragment() {

    lateinit var binding: DialogNewConversationBinding

    val conversationListViewModel by lazyActivityViewModel { injector.createConversationListViewModel(applicationContext) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogNewConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.newConversationNameInputHolder.enableErrorResettingOnTextChanged()
        binding.newConversationNameInput.onSubmit { createConversation() }
        binding.createConversation.setOnClickListener { createConversation() }
        binding.cancelButton.setOnClickListener { dismiss() }
    }

    private fun createConversation() {
        val friendlyName = binding.newConversationNameInput.text.toString()
        if (friendlyName.isBlank()) {
            binding.newConversationNameInputHolder.error = getString(R.string.profile_friendly_name_error_text)
            return
        }

        conversationListViewModel.createConversation(friendlyName)
        dismiss()
    }
}
