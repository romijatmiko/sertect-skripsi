package com.skripsi.jalu.ui.laporan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skripsi.jalu.data.DataRepository
import com.skripsi.jalu.data.LaporanResponse
import com.skripsi.jalu.data.UserPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LaporanViewModel(private val repository: DataRepository) : ViewModel() {
    private val _laporanList = MutableLiveData<List<LaporanResponse>>()
    val laporanList: LiveData<List<LaporanResponse>> = _laporanList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchLaporanList() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = repository.getUserId().first() ?: throw Exception("User ID not found")
                val response = repository.getLaporanByUserId(userId)

                // Sort laporan list by timestamp in descending order (newest first)
                val sortedLaporan = response.laporan.sortedByDescending { it.timestamp }

                _laporanList.value = sortedLaporan
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}