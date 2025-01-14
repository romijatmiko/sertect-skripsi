package com.skripsi.jalu.ui.laporan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skripsi.jalu.R
import com.skripsi.jalu.data.DataRepository
import com.skripsi.jalu.data.LaporanResponse
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.Exception

class LaporanDetailViewModel(private val repository: DataRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _laporan = MutableLiveData<LaporanResponse>()
    val laporan: LiveData<LaporanResponse> = _laporan

    fun fetchLaporanDetail(laporanId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.getLaporanById(laporanId)
                _laporan.value = response.laporan
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadTranscription() {
        viewModelScope.launch {
            // Implement download logic here
        }
    }
}
