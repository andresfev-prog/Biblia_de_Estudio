package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.BibleRepository
import com.example.data.repository.CloudSyncRepository

class BibleViewModelFactory(
    private val application: Application,
    private val repository: BibleRepository,
    private val cloudSyncRepository: CloudSyncRepository = CloudSyncRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BibleViewModel::class.java)) {
            return BibleViewModel(application, repository, cloudSyncRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
