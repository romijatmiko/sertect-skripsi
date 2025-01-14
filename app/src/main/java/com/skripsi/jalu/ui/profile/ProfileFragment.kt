package com.skripsi.jalu.ui.profile

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.skripsi.jalu.R
import com.skripsi.jalu.data.DataRepository
import com.skripsi.jalu.databinding.FragmentProfileBinding
import com.skripsi.jalu.databinding.FragmentProfileSkeletonBinding
import com.skripsi.jalu.ui.login.EntryActivity
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var _skeletonBinding: FragmentProfileSkeletonBinding? = null
    private val skeletonBinding get() = _skeletonBinding!!

    private lateinit var containerView: FrameLayout
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(DataRepository(requireContext()))
    }

    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        containerView = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        _binding = FragmentProfileBinding.inflate(inflater, null, false)
        _skeletonBinding = FragmentProfileSkeletonBinding.inflate(inflater, null, false)

        shimmerFrameLayout = ShimmerFrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(skeletonBinding.root)
        }

        containerView.addView(binding.root)
        containerView.addView(shimmerFrameLayout)

        binding.root.alpha = 0f

        showSkeleton()
        loadContent()

        return containerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun showSkeleton() {
        shimmerFrameLayout.visibility = View.VISIBLE
        shimmerFrameLayout.startShimmer()
        binding.root.visibility = View.GONE
    }

    private fun hideSkeleton() {
        if (!isAdded || _binding == null) return // Safety check

        // Delay hiding skeleton for 4 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdded || _binding == null) return@postDelayed

            val fadeOutSkeleton = AlphaAnimation(1f, 0f).apply {
                duration = 800
                fillAfter = true
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        shimmerFrameLayout.stopShimmer()
                    }
                    override fun onAnimationEnd(animation: Animation?) {
                        if (isAdded && shimmerFrameLayout.isAttachedToWindow) {
                            shimmerFrameLayout.visibility = View.GONE
                        }
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }

            binding.root.visibility = View.VISIBLE
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 300
                addUpdateListener { animator ->
                    binding.root.alpha = animator.animatedValue as Float
                }
                start()
            }

            shimmerFrameLayout.startAnimation(fadeOutSkeleton)
        }, 4000) // 4 second delay
    }

    private fun loadContent() {
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(emptyList())
        binding.recyclerViewNotif.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }

    private fun setupObservers() {
        profileViewModel.user.observe(viewLifecycleOwner) { userResponse ->
            userResponse?.user?.let { user ->
                if (_binding != null) {
                    binding.username.text = "${user.firstName} ${user.lastName}"
                    binding.email.text = user.email
                    hideSkeleton()
                }
            }
        }

        profileViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            notifications?.let {
                notificationAdapter.updateNotifications(it)
            }
        }
    }

    private fun setupClickListeners() {
        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding.btnKeluarAkun.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Ya") { dialog, _ ->
                dialog.dismiss()
                logoutUser()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            try {
                profileViewModel.logout()
                navigateToEntryActivity()
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(context, "Gagal keluar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToEntryActivity() {
        val intent = Intent(activity, EntryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        if (isAdded) {
            Toast.makeText(activity, "Anda telah keluar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shimmerFrameLayout.stopShimmer()
        _binding = null
        _skeletonBinding = null
    }
}