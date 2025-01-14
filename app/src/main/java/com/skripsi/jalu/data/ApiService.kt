package com.skripsi.jalu.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    @GET("users/{userId}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): UserResponse

    @FormUrlEncoded
    @PUT("users/{userId}")
    suspend fun editUserProfile(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Field("displayName") displayName: String,
        @Field("password") password: String
    ): UserResponse


    @FormUrlEncoded
    @POST("/auth/register")
    suspend fun register(
        @Field("firstName") firstName: String,
        @Field("lastName") lastName: String,
        @Field("email") email: String,
        @Field("password") password: String,
    ): RegisterResponse

    @FormUrlEncoded
    @POST("/auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): LoginResponse


    @Multipart
    @POST("transcribe/{id}")
    suspend fun transcribeAudio(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part audio: MultipartBody.Part
    ): TranscribeResponse


    @FormUrlEncoded
    @POST("monitoring/{id}")
    suspend fun addMonitoring(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Field("userId") userId: String,
        @Field("judulMonitoring") judulMonitoring: String,
        @Field("tanggalMonitoring") tanggalMonitoring: String,
        @Field("namaFileMonitoring") namaFileMonitoring: String
    ): MonitoringResponse


    @FormUrlEncoded
    @PUT("monitoring/{id}")
    suspend fun editMonitoring(
        @Path("id") id: String,
        @Field("userId") userId: String,
        @Field("judulMonitoring") judulMonitoring: String,
        @Field("tanggalMonitoring") tanggalMonitoring: String,
        @Field("namaFileMonitoring") namaFileMonitoring: String
    ): MonitoringResponse

    @DELETE("monitoring/{id}")
    suspend fun deleteMonitoring(@Path("id") id: String): MonitoringResponse

    @GET("monitoring/{userId}")
    suspend fun getMonitoringByUserId(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): MonitoringListResponse

    @GET("notification/{userId}")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): NotificationListResponse

    @GET("laporan/{userId}")
    suspend fun getLaporan(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): LaporanListResponse

    @GET("laporan/detail/{id}")
    suspend fun getLaporanById(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): LaporanApiResponse

}



