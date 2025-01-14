package com.skripsi.jalu.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


class DataRepository(private val context: Context) {

    private val apiService = ApiConfig.getApiService()
    private val userPreference = UserPreference(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun login(email: String, password: String): LoginResponse {
        return try {
            val response = apiService.login(email, password)
            if (response.message == "success" && response.loginResult != null) {
                val user = firebaseAuth.currentUser
                val idToken = user?.getIdToken(true)?.await()?.token
                val userId = user?.uid // Get the user ID from Firebase
                if (idToken != null && userId != null) {
                    saveUserToken(idToken) // Save ID token
                    saveUserId(userId) // Save user ID
                    Log.d("DataRepository", "User ID Token and ID saved: $idToken, $userId")
                } else {
                    Log.e("DataRepository", "Failed to retrieve ID Token or User ID")
                }
            }
            response
        } catch (e: Exception) {
            Log.e("DataRepository", "Error during login: ${e.message}", e)
            throw e
        }
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): RegisterResponse {
        return try {
            apiService.register(firstName, lastName, email, password)
        } catch (e: Exception) {
            Log.e("DataRepository", "Error during registration: ${e.message}", e)
            throw e
        }
    }

    suspend fun saveUserToken(token: String) {
        try {
            userPreference.saveUserToken(token)
            Log.d("DataRepository", "User token saved: $token")
        } catch (e: Exception) {
            Log.e("DataRepository", "Error saving user token: ${e.message}", e)
            throw e
        }
    }

    // Function to save the user ID
    suspend fun saveUserId(id: String) {
        try {
            userPreference.saveUserId(id)
            Log.d("DataRepository", "User ID saved: $id")
        } catch (e: Exception) {
            Log.e("DataRepository", "Error saving user ID: ${e.message}", e)
            throw e
        }
    }

    suspend fun refreshAndGetToken(): String? {
        return try {
            val user = firebaseAuth.currentUser
            val freshToken = user?.getIdToken(true)?.await()?.token
            if (freshToken != null) {
                saveUserToken(freshToken)
                Log.d("DataRepository", "Token refreshed and saved: $freshToken")
            } else {
                Log.e("DataRepository", "Failed to refresh token")
            }
            freshToken
        } catch (e: Exception) {
            Log.e("DataRepository", "Error refreshing token: ${e.message}", e)
            null
        }
    }

    // Updated getUserToken() function
    fun getUserToken(): Flow<String?> {
        return userPreference.userToken.flatMapConcat { token ->
            if (token == null) {
                // If token is null, try to refresh it
                refreshAndGetToken()?.let { flow { emit(it) } } ?: flow { emit(null) }
            } else {
                // If token exists, check if it's expired and refresh if necessary
                checkAndRefreshTokenIfNeeded(token)
            }
        }.catch { e ->
            Log.e("DataRepository", "Error retrieving or refreshing user token: ${e.message}", e)
        }
    }

    private suspend fun checkAndRefreshTokenIfNeeded(token: String): Flow<String?> {
        return flow {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val metadata = user.metadata
                if (metadata != null) {
                    val lastSignInTimestamp = metadata.lastSignInTimestamp
                    val currentTime = System.currentTimeMillis()
                    // Check if token is older than 1 hour (3600000 milliseconds)
                    if (currentTime - lastSignInTimestamp > 3600000) {
                        val freshToken = refreshAndGetToken()
                        emit(freshToken)
                    } else {
                        emit(token)
                    }
                } else {
                    emit(token)
                }
            } else {
                emit(null)
            }
        }
    }

    // Function to get the user ID
    fun getUserId(): Flow<String?> {
        return userPreference.userId.onEach { id ->
            Log.d("DataRepository", "User ID retrieved: $id")
        }.catch { e ->
            Log.e("DataRepository", "Error retrieving user ID: ${e.message}", e)
        }
    }

    suspend fun getUserById(): UserResponse {
        return try {
            // Retrieve user token and user ID from UserPreference
            val userToken = userPreference.userToken.firstOrNull()
            val userId = userPreference.userId.firstOrNull()

            if (userToken != null && userId != null) {
                // Make the API call using the retrieved user ID and token
                apiService.getUserById("Bearer $userToken", userId)
            } else {
                throw Exception("User token or ID is null")
            }
        } catch (e: Exception) {
            Log.e("DataRepository", "Error fetching user by ID: ${e.message}", e)
            throw e
        }
    }
    suspend fun editUserProfile(
        userId: String,
        firstName: String,
        lastName: String,
        password: String
    ): UserResponse {
        val token = userPreference.userToken.firstOrNull() ?: throw Exception("Token not found")
        val displayName = "$firstName $lastName" // Combine firstName and lastName
        return try {
            apiService.editUserProfile("Bearer $token", userId, displayName, password)
        } catch (e: Exception) {
            Log.e("DataRepository", "Error editing user profile: ${e.message}", e)
            throw e
        }
    }


    suspend fun transcribeAudio(id: String, audioFile: File): TranscribeResponse {
        val token = userPreference.userToken.firstOrNull() ?: throw Exception("Token not found")

        val audioPart = MultipartBody.Part.createFormData(
            "audio", audioFile.name, audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
        )

        return try {
            apiService.transcribeAudio("Bearer $token", id, audioPart)
        } catch (e: Exception) {
            Log.e("DataRepository", "Error transcribing audio: ${e.message}", e)
            throw e
        }
    }


    suspend fun getNotifications(userId: String): NotificationListResponse {
        val token = userPreference.userToken.firstOrNull() ?: throw Exception("Token not found")
        return try {
            apiService.getNotifications("Bearer $token", userId)
        } catch (e: Exception) {
            Log.e("DataRepository", "Error fetching notifications: ${e.message}", e)
            throw e
        }
    }

    suspend fun getLaporanById(laporanId: String): LaporanApiResponse {
        return try {
            val token = userPreference.userToken.firstOrNull() ?: throw Exception("Token not found")
            apiService.getLaporanById("Bearer $token", laporanId)
        } catch (e: Exception) {
            Log.e("DataRepository", "Error fetching laporan by ID: ${e.message}", e)
            throw e
        }
    }

    suspend fun getLaporanByUserId(userId: String): LaporanListResponse {
        return try {
            val token = userPreference.userToken.firstOrNull() ?: throw Exception("Token not found")
            apiService.getLaporan("Bearer $token", userId)
        } catch (e: Exception) {
            Log.e("DataRepository", "Error fetching laporan list: ${e.message}", e)
            throw e
        }
    }



    // Function to handle logout
    suspend fun logout() {
        try {
            userPreference.clearAll()
            firebaseAuth.signOut()
            Log.d("DataRepository", "User logged out and token cleared")
        } catch (e: Exception) {
            Log.e("DataRepository", "Error during logout: ${e.message}", e)
            throw e
        }
    }

    suspend fun addMonitoring(id: String, userId: String, judulMonitoring: String, tanggalMonitoring: String, namaFileMonitoring: String): MonitoringResponse {
        val token = userPreference.userToken.firstOrNull() ?: throw Exception("Token not found")
        return apiService.addMonitoring("Bearer $token", id, userId, judulMonitoring, tanggalMonitoring, namaFileMonitoring)
    }

    suspend fun getMonitoringByUserId(userId: String): MonitoringListResponse {
        val token = userPreference.userToken.firstOrNull() ?: throw Exception("Token not found")
        return apiService.getMonitoringByUserId("Bearer $token", userId)
    }


}



