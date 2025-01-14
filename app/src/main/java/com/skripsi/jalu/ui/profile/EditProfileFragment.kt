package com.skripsi.jalu.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.skripsi.jalu.R
import com.skripsi.jalu.data.DataRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var dataRepository: DataRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        firstNameEditText = view.findViewById(R.id.firstNameEditText)
        lastNameEditText = view.findViewById(R.id.LastNameEditText)
        passwordEditText = view.findViewById(R.id.PasswordEditText)
        saveButton = view.findViewById(R.id.btn_save)

        dataRepository = DataRepository(requireContext())

        // Load user data from repository
        loadUserProfile()

        saveButton.setOnClickListener {
            updateUserProfile()
        }

        return view
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                val userResponse = dataRepository.getUserById()
                val user = userResponse.user
                firstNameEditText.setText(user.firstName)
                lastNameEditText.setText(user.lastName)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserProfile() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val userId = dataRepository.getUserId().firstOrNull() ?: return@launch
                val response = dataRepository.editUserProfile(userId, firstName, lastName, password)
                if (response.user != null) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()

                    // Pindah ke ProfileFragment setelah update berhasil
                    findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
