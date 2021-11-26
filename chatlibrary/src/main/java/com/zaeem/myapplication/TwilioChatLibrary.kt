package com.zaeem.myapplication

import android.content.Context
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.zaeem.myapplication.data.ConversationsClientWrapper
import com.zaeem.myapplication.data.localCache.LocalCacheProvider

object TwilioChatLibrary {

    fun init(context: Context)
    {
        FirebaseAnalytics.getInstance(context)
        FirebaseApp.initializeApp(context)
        EmojiCompat.init(BundledEmojiCompatConfig(context))
        ConversationsClientWrapper.createInstance(context)
        LocalCacheProvider.createInstance(context)

    }

}