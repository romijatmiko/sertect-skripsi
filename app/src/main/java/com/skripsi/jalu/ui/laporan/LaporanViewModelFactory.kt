package com.skripsi.jalu.ui.laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skripsi.jalu.data.DataRepository

class LaporanViewModelFactory(private val repository: DataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LaporanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LaporanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}