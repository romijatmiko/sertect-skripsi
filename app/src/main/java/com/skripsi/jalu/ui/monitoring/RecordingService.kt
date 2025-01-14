package com.skripsi.jalu.monitoring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.skripsi.jalu.MainActivity
import com.skripsi.jalu.R
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RecordingService : Service() {

    private var recorder: AudioRecord? = null
    private var isRecording = false
    private lateinit var recordingJob: Job
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var fileName: String? = null

    companion object {
        const val ACTION_START_RECORDING = "START_RECORDING"
        const val ACTION_STOP_RECORDING = "STOP_RECORDING"
        const val ACTION_BACK_TO_APP = "BACK_TO_APP"
        const val EXTRA_FILE_NAME = "file_name"
        private const val CHANNEL_ID = "recording_service_channel"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "RecordingService"
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "onCreate: Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Received action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                fileName = intent.getStringExtra(EXTRA_FILE_NAME)
                if (fileName != null) {
                    startRecording()
                } else {
                    Log.e(TAG, "Error: No filename provided for recording")
                    stopSelf()
                }
            }
            ACTION_STOP_RECORDING -> stopRecording()
            ACTION_BACK_TO_APP -> backToApp()
            else -> Log.w(TAG, "Unknown action received: ${intent?.action}")
        }
        return START_STICKY
    }

    private fun startRecording() {
        Log.d(TAG, "startRecording: Attempting to start recording")
        if (!isRecording) {
            if (fileName == null) {
                Log.e(TAG, "Error: No filename available for recording")
                stopSelf()
                return
            }
            val outputFile = File(getExternalFilesDir(null), fileName!!)
            Log.d(TAG, "Output file: ${outputFile.absolutePath}")

            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            recorder = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize)

            if (recorder?.state == AudioRecord.STATE_INITIALIZED) {
                isRecording = true
                showNotification("Recording in progress")

                recordingJob = coroutineScope.launch {
                    recorder?.startRecording()
                    writeAudioDataToFile(outputFile, bufferSize)
                }

                Log.d(TAG, "Recording started successfully")
            } else {
                Log.e(TAG, "Failed to initialize AudioRecord")
            }
        } else {
            Log.w(TAG, "startRecording: Already recording")
        }
    }

    private suspend fun writeAudioDataToFile(outputFile: File, bufferSize: Int) {
        withContext(Dispatchers.IO) {
            try {
                FileOutputStream(outputFile).use { fos ->
                    writeWavHeader(fos, SAMPLE_RATE, CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_STEREO)

                    val buffer = ByteArray(bufferSize)
                    while (isRecording) {
                        val readSize = recorder?.read(buffer, 0, bufferSize) ?: -1
                        if (readSize > 0) {
                            fos.write(buffer, 0, readSize)
                        }
                    }

                    updateWavHeader(outputFile)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error writing audio data to file", e)
            }
        }
    }

    private fun writeWavHeader(fos: FileOutputStream, sampleRate: Int, isStereo: Boolean) {
        val channels = if (isStereo) 2 else 1
        val headerSize = 44
        val bytesPerSample = 2

        fos.write("RIFF".toByteArray())
        fos.write(intToByteArray(0))
        fos.write("WAVE".toByteArray())
        fos.write("fmt ".toByteArray())
        fos.write(intToByteArray(16))
        fos.write(shortToByteArray(1))
        fos.write(shortToByteArray(channels.toShort()))
        fos.write(intToByteArray(sampleRate))
        fos.write(intToByteArray(sampleRate * channels * bytesPerSample))
        fos.write(shortToByteArray((channels * bytesPerSample).toShort()))
        fos.write(shortToByteArray((bytesPerSample * 8).toShort()))
        fos.write("data".toByteArray())
        fos.write(intToByteArray(0))
    }

    private fun updateWavHeader(file: File) {
        val fileSize = file.length()
        val dataSize = fileSize - 44

        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(4)
            raf.write(intToByteArray((fileSize - 8).toInt()))
            raf.seek(40)
            raf.write(intToByteArray(dataSize.toInt()))
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()
    }

    private fun stopRecording() {
        Log.d(TAG, "stopRecording: Attempting to stop recording")
        if (isRecording) {
            isRecording = false
            recordingJob.cancel()
            recorder?.stop()
            recorder?.release()
            recorder = null
            showNotification("Recording stopped")
            Log.d(TAG, "Recording stopped successfully")
        } else {
            Log.w(TAG, "stopRecording: Not currently recording")
        }
        stopForeground(true)
        stopSelf()
    }

    private fun backToApp() {
        Log.d(TAG, "backToApp: Back to app action triggered")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recording Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for recording service"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(text: String) {
        Log.d(TAG, "showNotification: Attempting to show notification with text: $text")
        try {
            val stopIntent = Intent(this, RecordingService::class.java).apply {
                action = ACTION_STOP_RECORDING
            }
            val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

            val backIntent = Intent(this, RecordingService::class.java).apply {
                action = ACTION_BACK_TO_APP
            }
            val backPendingIntent = PendingIntent.getService(this, 0, backIntent, PendingIntent.FLAG_IMMUTABLE)

            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audio Recording")
                .setContentText(text)
                .setSmallIcon(R.drawable.fire1)
                .addAction(R.drawable.baseline_stop_circle_24, "Stop Recording", stopPendingIntent)
                .addAction(R.drawable.ic_home_black_24dp, "Back to App", backPendingIntent)
                .build()

            Log.d(TAG, "showNotification: Notification built successfully")

            startForeground(NOTIFICATION_ID, notification)
            Log.d(TAG, "showNotification: startForeground called with NOTIFICATION_ID: $NOTIFICATION_ID")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: Service is being destroyed")
        stopRecording()
        coroutineScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}