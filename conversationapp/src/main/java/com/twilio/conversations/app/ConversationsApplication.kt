package com.twilio.conversations.app

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.zaeem.myapplication.TwilioChatLibrary
import com.zaeem.myapplication.common.injector
import com.zaeem.myapplication.data.ConversationsClientWrapper
import com.zaeem.myapplication.data.localCache.LocalCacheProvider
import com.zaeem.myapplication.repository.ConversationsRepositoryImpl
import com.zaeem.myapplication.ui.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class ConversationsApplication : Application(), LifecycleObserver {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var isForegrounded = false

    private var runOnForeground = {}

    override fun onCreate() {
        super.onCreate()

        TwilioChatLibrary.init(this)
        ConversationsRepositoryImpl.createInstance(ConversationsClientWrapper.INSTANCE, LocalCacheProvider.INSTANCE)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        ConversationsClientWrapper.INSTANCE.onUpdateTokenFailure += { signOut() }
    }

    private fun signOut() = applicationScope.launch {
        val loginManager = injector.createLoginManager(applicationContext)
        loginManager.signOut()

        startLoginActivityWhenInForeground()
    }

    private fun startLoginActivityWhenInForeground() {
        if (isForegrounded) {
            LoginActivity.start(this)
        } else {
            runOnForeground = { LoginActivity.start(this) }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isForegrounded = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        isForegrounded = true

        runOnForeground()
        runOnForeground = {}
    }
}
