package com.skripsi.jalu.ui.monitoring

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.facebook.shimmer.ShimmerFrameLayout
import com.skripsi.jalu.R
import com.skripsi.jalu.data.DataRepository
import com.skripsi.jalu.databinding.FragmentMonitoringBinding
import com.skripsi.jalu.databinding.FragmentProfileSkeletonBinding
import com.skripsi.jalu.monitoring.RecordingService
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MonitoringFragment : Fragment() {

    private var _binding: FragmentMonitoringBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private var isRecording = false
    private lateinit var dataRepository: DataRepository

    private var _skeletonBinding: FragmentProfileSkeletonBinding? = null
    private val skeletonBinding get() = _skeletonBinding!!
    private lateinit var containerView: FrameLayout
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    companion object {
        private var generatedFileName: String? = null
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val REQUEST_NOTIFICATION_PERMISSION = 201
        private const val REQUEST_READ_MEDIA_AUDIO_PERMISSION = 202
        private const val TAG = "MonitoringFragment"
        private const val PREF_IS_RECORDING = "is_recording"
        private const val AUDIO_FOLDER_NAME = "jalu"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)

        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        isRecording = sharedPreferences.getBoolean(PREF_IS_RECORDING, false)
        dataRepository = DataRepository(requireContext())

        // Update UI based on current recording state
        updateUI(isRecording)

        // Set click listener for 'Go' button
        binding.goButton.setOnClickListener {
            handleButtonClick()
        }
        binding.storageIcon.setOnClickListener {
            findNavController().navigate(R.id.action_monitoringFragment_to_monitoringListFragment)
        }

        return binding.root
    }

    private fun handleButtonClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
                return
            }
        }

        if (checkPermissions()) {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
            isRecording = !isRecording
            updateUI(isRecording)
            saveRecordingState(isRecording)
        } else {
            requestPermissions()
        }
    }

    private fun updateUI(isRecording: Boolean) {
        with(binding) {
            goButton.text = if (isRecording) getString(R.string.stop_button_text) else getString(R.string.go_button_text)
            imageView.visibility = View.VISIBLE
        }
    }

    private fun saveRecordingState(isRecording: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_IS_RECORDING, isRecording).apply()
    }

    private fun checkPermissions(): Boolean {
        val hasRecordAudioPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val hasReadMediaAudioPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_MEDIA_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        return hasRecordAudioPermission && hasReadMediaAudioPermission
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray(),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    private fun incrementAndSaveSharedCount(): Int {
        val currentCount = sharedPreferences.getInt("shared_count", 0)
        val newCount = currentCount + 1
        sharedPreferences.edit().putInt("shared_count", newCount).apply()
        return newCount
    }

    private fun saveMonitoringData() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())

        // Increment and retrieve the shared count once
        val sharedCount = incrementAndSaveSharedCount()

        // Retrieve the user ID from DataStore using the DataRepository
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = dataRepository.getUserId().firstOrNull() ?: "default_user" // Fallback if user ID is not found

            // Gunakan nama file yang sudah dihasilkan atau buat baru jika belum ada
            val fileName = generatedFileName ?: generateFileName(sharedCount, userId).also {
                generatedFileName = it
            }

            // Save the file name to SharedPreferences
            sharedPreferences.edit().putString("current_recording_filename", fileName).apply()

            // Prepare the intent for starting the recording service
            val serviceIntent = Intent(requireContext(), RecordingService::class.java).apply {
                action = RecordingService.ACTION_START_RECORDING
                putExtra("file_name", fileName)
            }

            // Start the recording service with the consistent file name
            requireContext().startForegroundService(serviceIntent)

            // Create a unique monitoring ID and title using the same shared count
            val monitoringId = UUID.randomUUID().toString()
            val monitoringTitle = "Monitoring #$sharedCount - $currentDate"

            // Save the monitoring data with the same file name and shared count
            addMonitoring(monitoringId, monitoringTitle, currentDate, fileName)
        }
    }

    private fun startRecording() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())

        // Increment and retrieve the shared count using the new function
        val sharedCount = incrementAndSaveSharedCount()

        // Retrieve the user ID from DataStore using the DataRepository
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = dataRepository.getUserId().firstOrNull() ?: "default_user" // Fallback if user ID is not found

            // Gunakan nama file yang sudah dihasilkan atau buat baru jika belum ada
            val fileName = generatedFileName ?: generateFileName(sharedCount, userId).also {
                generatedFileName = it
            }

            // Save the file name to SharedPreferences
            sharedPreferences.edit().putString("current_recording_filename", fileName).apply()

            // Prepare and start the recording service
            val intent = Intent(requireContext(), RecordingService::class.java).apply {
                action = RecordingService.ACTION_START_RECORDING
                putExtra(RecordingService.EXTRA_FILE_NAME, fileName)
            }
            try {
                sharedPreferences.edit().putLong("recording_start_time", System.currentTimeMillis()).apply()
                requireContext().startForegroundService(intent)
                Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting RecordingService", e)
                Toast.makeText(requireContext(), "Failed to start recording", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording() {
        val intent = Intent(requireContext(), RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP_RECORDING
        }
        try {
            requireContext().startService(intent)
            Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_SHORT).show()
            saveMonitoringData()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping RecordingService", e)
            Toast.makeText(requireContext(), "Failed to stop recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMonitoring(id: String, judul: String, tanggal: String, namaFile: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userId = dataRepository.getUserId().firstOrNull() ?: throw Exception("User ID not found")
                dataRepository.addMonitoring(id, userId, judul, tanggal, namaFile)
                Toast.makeText(requireContext(), "Monitoring added successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error adding monitoring", e)
                Toast.makeText(requireContext(), "Failed to add monitoring", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Audio permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Audio permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_NOTIFICATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_READ_MEDIA_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Media audio permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Media audio permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getAudioFolder(): File {
        val folder = requireContext().getExternalFilesDir(null)?.let { File(it, AUDIO_FOLDER_NAME) }
            ?: File(requireContext().filesDir, AUDIO_FOLDER_NAME)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    private fun getAudioFile(fileName: String): File {
        val folder = getAudioFolder()
        return File(folder, fileName)
    }

    private fun generateFileName(sharedCount: Int, userId: String): String {
        return "recording_${userId}_$sharedCount.wav"
    }
}
