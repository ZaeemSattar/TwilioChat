package com.zaeem.myapplication.data

import android.content.Context
import android.net.Uri
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope
import com.twilio.conversations.ConversationsClient
import com.zaeem.myapplication.BuildConfig
import com.zaeem.myapplication.common.enums.ConversationsError
import com.zaeem.myapplication.common.extensions.ConversationsException
import com.zaeem.myapplication.common.extensions.addListener
import com.zaeem.myapplication.common.extensions.createAndSyncClient
import com.zaeem.myapplication.common.extensions.updateToken

import kotlinx.coroutines.*
import timber.log.Timber
import java.io.FileNotFoundException
import java.net.URL

class ConversationsClientWrapper(private val applicationContext: Context) {

    private var deferredClient = CompletableDeferred<ConversationsClient>()

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val isClientCreated get() = deferredClient.isCompleted && !deferredClient.isCancelled

    val onUpdateTokenFailure = mutableListOf<() -> Unit>()

    private fun notifyUpdateTokenFailure() = onUpdateTokenFailure.forEach { it() }

    suspend fun getConversationsClient() = deferredClient.await() // Business logic will wait until conversationsClient created

    /**
     * Get token and call createClient if token is not null
     */
    suspend fun create(identity: String, password: String) {
        Timber.d("create")

        val token = getToken(identity)
//        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTS2FiMmMzMWNiZjI0NjQyNTIxNmQxYWZjMDA1NmJiOTQwLTE2MzYwMjY4MDUiLCJncmFudHMiOnsiaWRlbnRpdHkiOiJVc2VyIDEiLCJjaGF0Ijp7InNlcnZpY2Vfc2lkIjoiSVM3Y2ViYjk0ODlmOTU0NjIwOGZhMDY2YTY3ODRlMjE1NSJ9LCJkYXRhX3N5bmMiOnsic2VydmljZV9zaWQiOiJkZWZhdWx0In19LCJpYXQiOjE2MzYwMjY4MDUsImV4cCI6MTYzNjAzMDQwNSwiaXNzIjoiU0thYjJjMzFjYmYyNDY0MjUyMTZkMWFmYzAwNTZiYjk0MCIsInN1YiI6IkFDYTgyZTMwOTZhYTBlYjczZGFiOGVhMWUzYjZjNDkzYzIifQ.Dee0RWREXc347dfZuyudcZpCoVEr65-MsUTyPUbQYLA"
        Timber.d("token: $token")

        val client = createAndSyncClient(applicationContext, token)
        this.deferredClient.complete(client)

        client.addListener(
            onTokenAboutToExpire = { updateToken(identity, password, notifyOnFailure = false) },
            onTokenExpired = { updateToken(identity, password, notifyOnFailure = true) },
        )
    }

    suspend fun shutdown() {
        Timber.d("shutdown")
        getConversationsClient().shutdown()
        deferredClient = CompletableDeferred()
    }

    /**
     * Fetch Twilio access token and return it, if token is non-null, otherwise return error
     */
    private suspend fun getToken(username: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(TOKEN_URL)
                .buildUpon()
                .appendQueryParameter(QUERY_IDENTITY, username)
                .appendQueryParameter(QUERY_PASSWORD, password)
                .build()
                .toString()

            return@withContext URL(uri).readText()
        } catch (e: FileNotFoundException) {
            throw ConversationsException(ConversationsError.TOKEN_ACCESS_DENIED)
        } catch (e: Exception) {
            throw ConversationsException(ConversationsError.TOKEN_ERROR)
        }
    }

    private suspend fun getToken(username: String) = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(TOKEN_URL)
                .buildUpon()
                .appendQueryParameter(QUERY_IDENTITY, username)
                .build()
                .toString()

            return@withContext URL(uri).readText()
        } catch (e: FileNotFoundException) {
            throw ConversationsException(ConversationsError.TOKEN_ACCESS_DENIED)
        } catch (e: Exception) {
            throw ConversationsException(ConversationsError.TOKEN_ERROR)
        }
    }

    private fun updateToken(identity: String, password: String, notifyOnFailure: Boolean) = coroutineScope.launch {
        Timber.d("updateToken notifyOnFailure: $notifyOnFailure")

        val result = runCatching {
            val twilioToken = getToken(identity)
//            val twilioToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTS2FiMmMzMWNiZjI0NjQyNTIxNmQxYWZjMDA1NmJiOTQwLTE2MzYwMjY4MDUiLCJncmFudHMiOnsiaWRlbnRpdHkiOiJVc2VyIDEiLCJjaGF0Ijp7InNlcnZpY2Vfc2lkIjoiSVM3Y2ViYjk0ODlmOTU0NjIwOGZhMDY2YTY3ODRlMjE1NSJ9LCJkYXRhX3N5bmMiOnsic2VydmljZV9zaWQiOiJkZWZhdWx0In19LCJpYXQiOjE2MzYwMjY4MDUsImV4cCI6MTYzNjAzMDQwNSwiaXNzIjoiU0thYjJjMzFjYmYyNDY0MjUyMTZkMWFmYzAwNTZiYjk0MCIsInN1YiI6IkFDYTgyZTMwOTZhYTBlYjczZGFiOGVhMWUzYjZjNDkzYzIifQ.Dee0RWREXc347dfZuyudcZpCoVEr65-MsUTyPUbQYLA"
            getConversationsClient().updateToken(twilioToken)
        }

        if (result.isFailure && notifyOnFailure) {
            Timber.e(result.exceptionOrNull())
            notifyUpdateTokenFailure()
        }
    }

    companion object {
        private const val TOKEN_URL = BuildConfig.ACCESS_TOKEN_SERVICE_URL
        private const val QUERY_IDENTITY = "identity"
        private const val QUERY_PASSWORD = "password"

        val INSTANCE get() = _instance ?: error("call ConversationsClientWrapper.createInstance() first")

        private var _instance: ConversationsClientWrapper? = null

        fun createInstance(applicationContext: Context) {
            check(_instance == null) { "ConversationsClientWrapper singleton instance has been already created" }
            _instance = ConversationsClientWrapper(applicationContext)
        }

        @DelicateCoroutinesApi
        @RestrictTo(Scope.TESTS)
        fun recreateInstance(applicationContext: Context) {
            _instance?.let { instance ->
                // Shutdown old client if it will ever be created
                GlobalScope.launch { instance.getConversationsClient().shutdown() }
            }

            _instance = null
            createInstance(applicationContext)
        }
    }
}
