package com.skripsi.jalu.ui.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.skripsi.jalu.MainActivity
import com.skripsi.jalu.R
import com.skripsi.jalu.data.DataRepository
import com.skripsi.jalu.data.UserPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private lateinit var dataRepository: DataRepository
    private lateinit var userPreference: UserPreference

    private lateinit var emailEditText: EmailValidationEditText
    private lateinit var passwordEditText: PasswordValidationEditText
    private lateinit var loginButton: Button
    private var progressDialog: ProgressDialog? = null
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dataRepository = DataRepository(this)
        userPreference = UserPreference(this)

        emailEditText = findViewById(R.id.edtEmail)
        passwordEditText = findViewById(R.id.edtPassword)
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            performLogin()
        }

        val registerNowTextView = findViewById<TextView>(R.id.textViewDaftar)
        registerNowTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty() || !email.contains('@')) {
            showToastOnMainThread("Email and Password must be filled correctly")
            return
        }

        progressDialog = ProgressDialog.show(this, "Logging in", "Please wait...", true, false)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Perform login using Firebase Authentication
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = firebaseAuth.currentUser

                if (user != null) {
                    val idToken = user.getIdToken(true).await().token
                    val userId = user.uid

                    if (idToken != null) {
                        userPreference.saveUserToken(idToken)
                        userPreference.saveUserId(userId)
                        showToastOnMainThread("Login successful")
                        navigateToMainActivity()
                    } else {
                        showToastOnMainThread("Login failed: Could not retrieve ID Token")
                    }
                } else {
                    showToastOnMainThread("Login failed: User is null")
                }
            } catch (e: Exception) {
                progressDialog?.dismiss()
                showToastOnMainThread("Login failed: ${e.message}")
            } finally {
                progressDialog?.dismiss()
            }
        }
    }


    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToastOnMainThread(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
