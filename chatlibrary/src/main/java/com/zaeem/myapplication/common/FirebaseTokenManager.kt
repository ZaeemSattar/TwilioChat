package com.zaeem.myapplication.common

import com.google.firebase.messaging.FirebaseMessaging
import com.zaeem.myapplication.common.enums.ConversationsError
import com.zaeem.myapplication.common.extensions.ConversationsException
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseTokenManager {

    suspend fun retrieveToken(): String {
        deleteToken().await()

        return suspendCoroutine { continuation ->
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener { task ->
                try {
                    task.result?.let { continuation.resume(it) }
                        ?: continuation.resumeWithException(ConversationsException(
                            ConversationsError.TOKEN_ERROR))
                } catch (e: Exception) {
                    // TOO_MANY_REGISTRATIONS thrown on devices with too many Firebase instances
                    continuation.resumeWithException(ConversationsException(ConversationsError.TOKEN_ERROR))
                }
            }
        }
    }

    fun deleteToken() = CompletableDeferred<Boolean>().apply {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
            Timber.d("delete FCM token completed: ${task.isSuccessful}")
            complete(task.isSuccessful)
        }
    }
}
