package com.skripsi.jalu.ui.laporan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skripsi.jalu.data.DataRepository

class LaporanDetailViewModelFactory(
    private val repository: DataRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d("LaporanDetailViewModelFactory", "Creating ViewModel of type: ${modelClass.simpleName}")
        if (modelClass.isAssignableFrom(LaporanDetailViewModel::class.java)) {
            return LaporanDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
