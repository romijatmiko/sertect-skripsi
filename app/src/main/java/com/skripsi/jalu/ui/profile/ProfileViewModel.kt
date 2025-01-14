package com.skripsi.jalu.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skripsi.jalu.data.DataRepository
import com.skripsi.jalu.data.NotificationItem
import com.skripsi.jalu.data.UserResponse
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProfileViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private val _user = MutableLiveData<UserResponse>()
    val user: LiveData<UserResponse> = _user

    private val _notifications = MutableLiveData<List<NotificationItem>>()
    val notifications: LiveData<List<NotificationItem>> = _notifications

    init {
        loadUserData()
        loadNotifications()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val userData = dataRepository.getUserById()
                _user.value = userData
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            try {
                val userId = dataRepository.getUserId().firstOrNull() ?: throw Exception("User ID not found")
                val notificationResponse = dataRepository.getNotifications(userId)
                _notifications.value = notificationResponse.notifications
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    suspend fun logout() {
        dataRepository.logout()
    }

    fun editUserProfile(firstName: String, lastName: String, password: String) {
        viewModelScope.launch {
            try {
                val userId = dataRepository.getUserId().firstOrNull() ?: throw Exception("User ID not found")
                val updatedUser = dataRepository.editUserProfile(userId, firstName, lastName, password)
                _user.value = updatedUser
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getUserToken() = dataRepository.getUserToken()

    fun getUserId() = dataRepository.getUserId()
}
