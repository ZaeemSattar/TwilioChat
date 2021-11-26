package com.zaeem.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.zaeem.myapplication.common.SingleLiveEvent
import com.zaeem.myapplication.common.asUserViewItem
import com.zaeem.myapplication.common.enums.ConversationsError
import com.zaeem.myapplication.common.extensions.ConversationsException
import com.zaeem.myapplication.manager.LoginManager
import com.zaeem.myapplication.manager.UserManager
import com.zaeem.myapplication.repository.ConversationsRepository

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileViewModel(
    private val conversationsRepository: ConversationsRepository,
    private val userManager: UserManager,
    private val loginManager: LoginManager,
) : ViewModel() {

    val selfUser = conversationsRepository.getSelfUser()
        .map { it.asUserViewItem() }
        .asLiveData(viewModelScope.coroutineContext)

    val onUserUpdated = SingleLiveEvent<Unit>()
    val onSignedOut = SingleLiveEvent<Unit>()
    val onError = SingleLiveEvent<ConversationsError>()

    fun setFriendlyName(friendlyName: String) = viewModelScope.launch {
        Timber.d("Updating self user: $friendlyName")
        try {
            userManager.setFriendlyName(friendlyName)
            Timber.d("Self user updated: $friendlyName")
            onUserUpdated.call()
        } catch (e: ConversationsException) {
            Timber.d("Failed to update self user")
            onError.value = ConversationsError.USER_UPDATE_FAILED
        }
    }

    fun signOut() = viewModelScope.launch {
        Timber.d("signOut")
        loginManager.signOut()
        onSignedOut.call()
    }
}
