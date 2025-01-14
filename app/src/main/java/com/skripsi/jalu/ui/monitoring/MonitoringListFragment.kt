package com.skripsi.jalu.ui.monitoring

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.skripsi.jalu.R
import com.skripsi.jalu.data.DataRepository
import com.skripsi.jalu.data.MonitoringData
import com.skripsi.jalu.databinding.FragmentMonitoringListBinding
import kotlinx.coroutines.launch
import java.io.File

class MonitoringListFragment : Fragment() {

    private var _binding: FragmentMonitoringListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MonitoringListViewModel
    private lateinit var adapter: MonitoringListAdapter
    private lateinit var selectedMonitoring: MonitoringData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitoringListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MonitoringListAdapter { monitoringData ->
            selectedMonitoring = monitoringData
            showConfirmationDialog() // Tampilkan konfirmasi sebelum memproses
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MonitoringListFragment.adapter
        }
    }

    private fun observeViewModel() {
        val factory = MonitoringListViewModelFactory(DataRepository(requireContext()))
        viewModel = ViewModelProvider(this, factory)[MonitoringListViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monitoringList.observe(viewLifecycleOwner) { list ->
                Log.d("MonitoringListFragment", "Monitoring list updated: ${list.size} items")
                adapter.updateData(list)
                showContent()
            }

            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    showSkeleton()
                } else {
                    Log.d("MonitoringListFragment", "Data loaded successfully")
                    showContent()
                }
            }

            viewModel.error.observe(viewLifecycleOwner) { error ->
                error?.let {
                    Log.e("MonitoringListFragment", "Error occurred: $it")
                    Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_LONG).show()
                    showContent()
                }
            }

            DataRepository(requireContext()).getUserId().collect { userId ->
                userId?.let { viewModel.fetchMonitoringList(it) }
            }
        }
    }

    private fun showSkeleton() {
        binding.skeletonLayout.root?.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        (binding.skeletonLayout.root.findViewById<View>(R.id.shimmerLayout) as? com.facebook.shimmer.ShimmerFrameLayout)?.startShimmer()
    }

    private fun showContent() {
        // Fade out skeleton
        binding.skeletonLayout.root?.animate()
            ?.alpha(0f)
            ?.setDuration(350)
            ?.withEndAction {
                (binding.skeletonLayout.root.findViewById<View>(R.id.shimmerLayout) as? com.facebook.shimmer.ShimmerFrameLayout)?.stopShimmer()
                binding.skeletonLayout.root?.visibility = View.GONE
                binding.skeletonLayout.root?.alpha = 1f // Reset alpha untuk penggunaan berikutnya

                // Fade in recyclerView
                binding.recyclerView.alpha = 0f
                binding.recyclerView.visibility = View.VISIBLE
                binding.recyclerView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            }
            ?.start()
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin memproses file ini?")
            .setPositiveButton("Yes") { _, _ ->
                handlePredictClick() // Lanjutkan ke proses jika "Yes"
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss() // Tutup dialog jika "Tidak"
            }
            .create()
            .show()
    }

    private fun handlePredictClick() {
        val namaFileMonitoring = selectedMonitoring.namaFileMonitoring
        val filePath = "/storage/emulated/0/Android/data/com.skripsi.jalu/files/$namaFileMonitoring"
        val audioFile = File(filePath)

        if (audioFile.exists()) {
            val monitoringId = selectedMonitoring.userId
            showLoading() // Show loading animation
            lifecycleScope.launch {
                try {
                    viewModel.transcribeAudio(monitoringId, audioFile)
                    Toast.makeText(requireContext(), "Transkripsi berhasil dikirim", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("MonitoringListFragment", "Error during transcription: ${e.message}")
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    hideLoading() // Hide loading animation after transcription is complete
                }
            }
        } else {
            Toast.makeText(requireContext(), "File audio tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    // Show loading animation
    private fun showLoading() {
        // Fade out recyclerView
        binding.recyclerView.animate()
            .alpha(0f)
            .setDuration(350)
            .withEndAction {
                binding.recyclerView.visibility = View.GONE
                binding.recyclerView.alpha = 1f // Reset alpha untuk penggunaan berikutnya
            }
            .start()

        binding.skeletonLayout.root.visibility = View.GONE

        // Fade in loading animation
        binding.loadingLayout.root.alpha = 0f
        binding.loadingLayout.root.visibility = View.VISIBLE
        binding.loadingLayout.root.animate()
            .alpha(1f)
            .setDuration(300)
            .withEndAction {
                binding.loadingLayout.lottieLoading.apply {
                    visibility = View.VISIBLE
                    playAnimation()
                }
            }
            .start()
    }

    // Hide loading animation
    private fun hideLoading() {
        // Fade out loading animation
        binding.loadingLayout.root.animate()
            .alpha(0f)
            .setDuration(350)
            .withEndAction {
                binding.loadingLayout.root.visibility = View.GONE
                binding.loadingLayout.root.alpha = 1f // Reset alpha untuk penggunaan berikutnya
                binding.loadingLayout.lottieLoading.pauseAnimation()

                // Fade in recyclerView
                binding.recyclerView.alpha = 0f
                binding.recyclerView.visibility = View.VISIBLE
                binding.recyclerView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
