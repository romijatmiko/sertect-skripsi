package com.skripsi.jalu.ui.dashboard

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.skripsi.jalu.databinding.FragmentDashboardBinding
import com.skripsi.jalu.databinding.FragmentDashboardSkeletonBinding

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var _skeletonBinding: FragmentDashboardSkeletonBinding? = null
    private val skeletonBinding get() = _skeletonBinding!!

    private lateinit var containerView: FrameLayout
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        containerView = FrameLayout(requireContext())
        containerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        _binding = FragmentDashboardBinding.inflate(inflater, null, false)
        _skeletonBinding = FragmentDashboardSkeletonBinding.inflate(inflater, null, false)

        containerView.addView(binding.root)
        containerView.addView(skeletonBinding.root)

        // Set initial alpha to 0 for fade in effect
        binding.root.alpha = 0f

        showSkeleton()
        loadContent()

        return containerView
    }

    private fun showSkeleton() {
        skeletonBinding.shimmerTopSection.startShimmer()
        skeletonBinding.shimmerLayout.startShimmer()
        skeletonBinding.shimmerLayoutBawah.startShimmer()

        skeletonBinding.root.visibility = View.VISIBLE
        binding.root.visibility = View.GONE
    }

    private fun hideSkeleton() {
        // Create fade out animation for skeleton
        val fadeOutSkeleton = AlphaAnimation(1f, 0f)
        fadeOutSkeleton.duration = 300 // 800 milliseconds
        fadeOutSkeleton.fillAfter = true

        fadeOutSkeleton.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                skeletonBinding.root.visibility = View.GONE
                skeletonBinding.shimmerTopSection.stopShimmer()
                skeletonBinding.shimmerLayout.stopShimmer()
                skeletonBinding.shimmerLayoutBawah.stopShimmer()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        // Create fade in animation for content using ValueAnimator
        binding.root.visibility = View.VISIBLE
        val fadeInContent = ValueAnimator.ofFloat(0f, 1f)
        fadeInContent.duration = 800 // 800 milliseconds
        fadeInContent.addUpdateListener { animator ->
            binding.root.alpha = animator.animatedValue as Float
        }

        // Start both animations
        skeletonBinding.root.startAnimation(fadeOutSkeleton)
        fadeInContent.start()
    }

    private fun loadContent() {
        handler.postDelayed({
            setupViews()
            hideSkeleton()
        }, 2000)
    }

    private fun setupViews() {
        val dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        dashboardViewModel.text.observe(viewLifecycleOwner) { text ->
            binding.hotFeature.text = text
        }

        binding.apply {
            iconEvents.setOnClickListener {
                // Handle events icon click
            }

            scan.setOnClickListener {
                // Handle scan icon click
            }

            community.setOnClickListener {
                // Handle community icon click
            }

            iconDislike.setOnClickListener {
                // Handle dislike click
            }

            iconDislikePertama.setOnClickListener {
                // Handle first dislike click
            }

            iconDislikeKedua.setOnClickListener {
                // Handle second dislike click
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
        _skeletonBinding = null
    }
}