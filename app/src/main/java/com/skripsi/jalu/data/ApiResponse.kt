package com.skripsi.jalu.data

import android.app.Notification
import com.google.api.Monitoring
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class RegisterResponse(
    var message: String?
)

data class LoginResponse(
    val message: String,
    val loginResult: LoginResult
)

data class LoginResult(
    val token: String,
    val userId: String
)

data class UserResponse(
    @SerializedName("user") val user: UserItem
)

data class UserItem(
    @SerializedName("userId") val userId: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("email") val email: String
) {
    val firstName: String
        get() = displayName.split(" ").firstOrNull() ?: ""

    val lastName: String
        get() = displayName.split(" ").drop(1).joinToString(" ")
}

data class MonitoringResponse(
    val message: String
)

data class MonitoringListResponse(
    val monitoring: List<MonitoringData>
)

data class MonitoringData(
    val id: String,
    val userId: String,
    val judulMonitoring: String,
    val tanggalMonitoring: String,
    val namaFileMonitoring: String
)

data class TranscribeResponse(
    val is_offensive: Boolean,
    val offensive_words: Map<String, Int>,
    val transcription: String
)

data class ProfileRequest(
    val firstName: String,
    val lastName: String,
    val password: String
)


data class NotificationResponse(
    @SerializedName("notification") val notification: NotificationItem
)

data class NotificationListResponse(
    @SerializedName("notifications") val notifications: List<NotificationItem>
)

data class NotificationItem(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("judulNotification") val judulNotification: String,
    @SerializedName("tanggalNotification") val tanggalNotification: String
)
data class SerapanWordInfo(
    val kata: String,
    val arti: String,
    val asal_kata: String,
    val bentuk_kata: String,
    val sumber: String
)

data class LaporanResponse(
    val id: String,
    val isKataSerapan: Boolean,
    val judul_laporan: String,
    val transcription: String,
    val serapan_words_info: List<SerapanWordInfo>,
    val tanggal: String,
    val timestamp: String,
    val user_id: String
)

data class LaporanApiResponse(
    val laporan: LaporanResponse
)

data class LaporanListResponse(
    val laporan: List<LaporanResponse>
)

