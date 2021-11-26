package com.zaeem.myapplication.data.models

import com.zaeem.myapplication.common.enums.ConversationsError


sealed class RepositoryRequestStatus {
    object FETCHING : RepositoryRequestStatus()
    object SUBSCRIBING : RepositoryRequestStatus()
    object COMPLETE : RepositoryRequestStatus()
    class Error(val error: ConversationsError) : RepositoryRequestStatus()
}
