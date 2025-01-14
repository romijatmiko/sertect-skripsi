package com.skripsi.jalu.ui.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skripsi.jalu.data.DataRepository

class MonitoringListViewModelFactory(private val repository: DataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonitoringListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MonitoringListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

