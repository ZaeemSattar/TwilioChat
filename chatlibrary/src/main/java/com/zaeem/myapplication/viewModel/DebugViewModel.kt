package com.zaeem.myapplication.viewModel

import androidx.lifecycle.ViewModel
import com.zaeem.myapplication.common.enums.CrashIn
import com.zaeem.myapplication.repository.ConversationsRepository

class DebugViewModel(private val conversationsRepository: ConversationsRepository) : ViewModel() {

    fun simulateCrash(where: CrashIn) = conversationsRepository.simulateCrash(where)
}
