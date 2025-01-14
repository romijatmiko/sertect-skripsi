package com.skripsi.jalu.ui.laporan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skripsi.jalu.R
import com.skripsi.jalu.data.DataRepository
import com.skripsi.jalu.data.LaporanResponse
import com.skripsi.jalu.databinding.FragmentDetailLaporanBinding

class LaporanDetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailLaporanBinding
    private lateinit var viewModel: LaporanDetailViewModel
    private lateinit var adapter: SerapanWordsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDetailLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val laporanId = LaporanDetailFragmentArgs.fromBundle(requireArguments()).laporanId
        viewModel = ViewModelProvider(this, LaporanDetailViewModelFactory(DataRepository(requireContext())))[LaporanDetailViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        showLoadingState()

        binding.downloadButton.setOnClickListener {
            viewModel.downloadTranscription()
        }

        viewModel.fetchLaporanDetail(laporanId)
    }

    private fun setupRecyclerView() {
        adapter = SerapanWordsAdapter()
        binding.contentScrollView.findViewById<RecyclerView>(R.id.recyclerView).apply {
            this.adapter = this@LaporanDetailFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewModel.laporan.observe(viewLifecycleOwner) { laporan ->
            // Langsung transisi ke layout yang sesuai setelah data tersedia
            hideLoadingStateWithAnimation {
                when {
                    laporan.isKataSerapan && laporan.serapan_words_info.isNullOrEmpty() -> {
                        showBelumAdaDataLayout()
                    }
                    !laporan.isKataSerapan -> {
                        showTidakPrediksiLayout()
                    }
                    else -> {
                        showNormalContent(laporan)
                    }
                }
            }
        }

        // Tambahkan observer untuk loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showLoadingState()
            }
        }
    }

    private fun showBelumAdaDataLayout() {
        binding.apply {
            // Reset view terlebih dahulu
            (root as ViewGroup).removeView(root.findViewWithTag("belum_ada_data"))
            (root as ViewGroup).removeView(root.findViewWithTag("tidak_prediksi"))

            contentScrollView.visibility = View.GONE

            // Inflate dan tambahkan layout belum ada data
            val belumAdaDataView = layoutInflater.inflate(R.layout.item_belum_ada_data, binding.root as ViewGroup, false).apply {
                tag = "belum_ada_data"
            }
            (binding.root as ViewGroup).addView(belumAdaDataView)
        }
    }

    private fun showTidakPrediksiLayout() {
        binding.apply {
            // Reset view terlebih dahulu
            (root as ViewGroup).removeView(root.findViewWithTag("belum_ada_data"))
            (root as ViewGroup).removeView(root.findViewWithTag("tidak_prediksi"))

            contentScrollView.visibility = View.GONE

            // Inflate dan tambahkan layout tidak prediksi
            val tidakPrediksiView = layoutInflater.inflate(R.layout.item_tidak_prediksi, binding.root as ViewGroup, false).apply {
                tag = "tidak_prediksi"
            }
            (binding.root as ViewGroup).addView(tidakPrediksiView)
        }
    }

    private fun showNormalContent(laporan: LaporanResponse) {
        binding.apply {
            // Reset view terlebih dahulu
            (root as ViewGroup).removeView(root.findViewWithTag("belum_ada_data"))
            (root as ViewGroup).removeView(root.findViewWithTag("tidak_prediksi"))

            contentScrollView.visibility = View.VISIBLE
            contentScrollView.findViewById<TextView>(R.id.title).text =
                "Wih Ada ${laporan.serapan_words_info.size} Kata Serapan Yang Muncul Nih"
        }
        adapter.submitList(laporan.serapan_words_info)
    }

    private fun showLoadingState() {
        binding.skeletonLayout.root.visibility = View.VISIBLE
        binding.contentScrollView.visibility = View.GONE

        // Reset semua view tambahan
        (binding.root as ViewGroup).removeView(binding.root.findViewWithTag("belum_ada_data"))
        (binding.root as ViewGroup).removeView(binding.root.findViewWithTag("tidak_prediksi"))
    }

    private fun hideLoadingStateWithAnimation(onAnimationComplete: () -> Unit) {
        val fadeOutSkeleton = AlphaAnimation(1f, 0f).apply {
            duration = 300 // Dipercepat menjadi 300ms
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    binding.skeletonLayout.root.visibility = View.GONE
                    onAnimationComplete()
                }

                override fun onAnimationRepeat(p0: Animation?) {}
            })
        }
        binding.skeletonLayout.root.startAnimation(fadeOutSkeleton)
    }
}