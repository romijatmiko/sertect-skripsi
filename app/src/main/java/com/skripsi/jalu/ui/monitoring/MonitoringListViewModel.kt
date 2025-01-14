package com.skripsi.jalu.ui.monitoring

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skripsi.jalu.data.DataRepository
import com.skripsi.jalu.data.MonitoringData
import kotlinx.coroutines.launch
import java.io.File

class MonitoringListViewModel(private val dataRepository: DataRepository) : ViewModel() {
    private val _monitoringList = MutableLiveData<List<MonitoringData>>()
    val monitoringList: LiveData<List<MonitoringData>> = _monitoringList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchMonitoringList(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = dataRepository.getMonitoringByUserId(userId)
                // Sort monitoring list by timestamp in descending order
                val sortedMonitoring = response.monitoring.sortedByDescending { it.tanggalMonitoring }
                _monitoringList.value = sortedMonitoring
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun transcribeAudio(id: String, audioFile: File) {
        try {
            dataRepository.transcribeAudio(id, audioFile)
        } catch (e: Exception) {
            _error.value = e.message
            throw e
        }
    }
}