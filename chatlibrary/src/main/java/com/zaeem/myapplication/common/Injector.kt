package com.zaeem.myapplication.common

import android.app.Application
import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope
import com.zaeem.myapplication.data.ConversationsClientWrapper
import com.zaeem.myapplication.data.CredentialStorage
import com.zaeem.myapplication.manager.*
import com.zaeem.myapplication.repository.ConversationsRepositoryImpl
import com.zaeem.myapplication.viewModel.*

var injector = Injector()
    private set

@RestrictTo(Scope.TESTS)
fun setupTestInjector(testInjector: Injector) {
    injector = testInjector
}

open class Injector {

    private var fcmManagerImpl: FCMManagerImpl? = null

    open fun createCredentialStorage(applicationContext: Context) = CredentialStorage(applicationContext)

    open fun createLoginManager(applicationContext: Context): LoginManager = LoginManagerImpl(
        ConversationsClientWrapper.INSTANCE,
        ConversationsRepositoryImpl.INSTANCE,
        CredentialStorage(applicationContext),
        FirebaseTokenManager(),
    )

    open fun createLoginViewModel(application: Application): LoginViewModel {
        val loginManager = createLoginManager(application)
        val connectivityMonitor = ConnectivityMonitor(application)

        return LoginViewModel(loginManager, connectivityMonitor)
    }

    open fun createSplashViewModel(application: Application): SplashViewModel {
        val loginManager = createLoginManager(application)

        val viewModel = SplashViewModel(loginManager)
        viewModel.initialize()

        return viewModel
    }

    open fun createConversationListViewModel(applicationContext: Context): ConversationListViewModel {
        val conversationListManager = ConversationListManagerImpl(ConversationsClientWrapper.INSTANCE)
        val connectivityMonitor = ConnectivityMonitor(applicationContext)

        return ConversationListViewModel(
            applicationContext,
            ConversationsRepositoryImpl.INSTANCE,
            conversationListManager,
            connectivityMonitor,
        )
    }

    open fun createDebugViewModel() = DebugViewModel(ConversationsRepositoryImpl.INSTANCE)

    open fun createProfileViewModel(applicationContext: Context) = ProfileViewModel(
        ConversationsRepositoryImpl.INSTANCE,
        UserManagerImpl(ConversationsClientWrapper.INSTANCE),
        createLoginManager(applicationContext)
    )

    open fun createMessageListViewModel(appContext: Context, conversationSid: String): MessageListViewModel {
        val messageListManager = MessageListManagerImpl(
            conversationSid,
            ConversationsClientWrapper.INSTANCE,
            ConversationsRepositoryImpl.INSTANCE
        )
        return MessageListViewModel(
            appContext,
            conversationSid,
            ConversationsRepositoryImpl.INSTANCE,
            messageListManager
        )
    }

    open fun createConversationDetailsViewModel(conversationSid: String): ConversationDetailsViewModel {
        val conversationListManager = ConversationListManagerImpl(ConversationsClientWrapper.INSTANCE)
        val participantListManager = ParticipantListManagerImpl(conversationSid, ConversationsClientWrapper.INSTANCE)
        return ConversationDetailsViewModel(
            conversationSid,
            ConversationsRepositoryImpl.INSTANCE,
            conversationListManager,
            participantListManager
        )
    }

    open fun createParticipantListViewModel(conversationSid: String): ParticipantListViewModel {
        val participantListManager = ParticipantListManagerImpl(conversationSid, ConversationsClientWrapper.INSTANCE)
        return ParticipantListViewModel(conversationSid, ConversationsRepositoryImpl.INSTANCE, participantListManager)
    }

    open fun createFCMManager(context: Context): FCMManager {
        val credentialStorage = createCredentialStorage(context.applicationContext)
        if (fcmManagerImpl == null) {
            fcmManagerImpl = FCMManagerImpl(context, ConversationsClientWrapper.INSTANCE, credentialStorage)
        }
        return fcmManagerImpl!!
    }
}
