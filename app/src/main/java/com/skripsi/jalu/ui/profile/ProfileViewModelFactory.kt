package com.skripsi.jalu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skripsi.jalu.data.DataRepository

class ProfileViewModelFactory(private val dataRepository: DataRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(dataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
