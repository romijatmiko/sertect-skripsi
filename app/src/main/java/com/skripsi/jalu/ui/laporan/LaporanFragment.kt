package com.skripsi.jalu.ui.laporan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.skripsi.jalu.R
import com.skripsi.jalu.data.DataRepository
import com.skripsi.jalu.databinding.FragmentLaporanBinding
import kotlinx.coroutines.launch

class LaporanFragment : Fragment() {
    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LaporanViewModel
    private lateinit var adapter: LaporanAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupViewModel()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = LaporanAdapter { laporanId ->
            findNavController().navigate(
                LaporanFragmentDirections.actionLaporanFragmentToLaporanDetailFragment(laporanId)
            )
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@LaporanFragment.adapter
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            LaporanViewModelFactory(DataRepository(requireContext()))
        )[LaporanViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.laporanList.observe(viewLifecycleOwner) { laporanList ->
            adapter.submitList(laporanList)
            showContent()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showSkeleton()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                showContent()
            }
        }

        viewModel.fetchLaporanList()
    }

    private fun showSkeleton() {
        // Fade out recyclerView if visible
        if (binding.recyclerView.visibility == View.VISIBLE) {
            binding.recyclerView.animate()
                .alpha(0f)
                .setDuration(350)
                .withEndAction {
                    binding.recyclerView.visibility = View.GONE
                    binding.recyclerView.alpha = 1f // Reset alpha for next use
                }
                .start()
        }

        // Fade in skeleton
        binding.skeletonLayout.root.alpha = 0f
        binding.skeletonLayout.root.visibility = View.VISIBLE
        binding.skeletonLayout.root.animate()
            .alpha(1f)
            .setDuration(300)
            .withEndAction {
                (binding.skeletonLayout.root.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout))?.startShimmer()
            }
            .start()
    }

    private fun showContent() {
        // Fade out skeleton
        binding.skeletonLayout.root.animate()
            ?.alpha(0f)
            ?.setDuration(350)
            ?.withEndAction {
                (binding.skeletonLayout.root.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout))?.stopShimmer()
                binding.skeletonLayout.root.visibility = View.GONE
                binding.skeletonLayout.root.alpha = 1f // Reset alpha for next use

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}