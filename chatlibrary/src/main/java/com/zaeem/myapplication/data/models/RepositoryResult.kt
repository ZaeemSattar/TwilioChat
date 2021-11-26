package com.zaeem.myapplication.data.models

data class RepositoryResult<T>(
    val data: T,
    val requestStatus: RepositoryRequestStatus
)
