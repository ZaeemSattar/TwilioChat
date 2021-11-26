package com.zaeem.myapplication.manager

import com.zaeem.myapplication.common.extensions.setFriendlyName
import com.zaeem.myapplication.data.ConversationsClientWrapper

interface UserManager {
    suspend fun setFriendlyName(friendlyName:String)
}

class UserManagerImpl(private val conversationsClient: ConversationsClientWrapper) : UserManager {

    override suspend fun setFriendlyName(friendlyName: String)
            = conversationsClient.getConversationsClient().myUser.setFriendlyName(friendlyName)

}
