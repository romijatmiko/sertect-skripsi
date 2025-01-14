package com.skripsi.jalu.ui.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skripsi.jalu.R
import com.skripsi.jalu.data.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var dataRepository: DataRepository
    private lateinit var emailEditText: EmailValidationEditText
    private lateinit var passwordEditText: PasswordValidationEditText
    private lateinit var nameEditText: EditText
    private lateinit var lastEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var errorTextViewEmail: TextView
    private lateinit var errorPasswordTextView: TextView
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dataRepository = DataRepository(this)

        emailEditText = findViewById(R.id.edtEmail)
        passwordEditText = findViewById(R.id.edtPassword)
        nameEditText = findViewById(R.id.firstNameEditText)
        lastEditText = findViewById(R.id.LastNameEditText)
        registerButton = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            performRegister()
        }

        val loginNowTextView = findViewById<TextView>(R.id.textView8)
        loginNowTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateRegisterButton() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val name = nameEditText.text.toString()

        registerButton.isEnabled = email.contains('@') && password.length >= 8 && name.isNotEmpty()

        if (!email.contains('@')) {
            errorTextViewEmail.text = "Masukan Email yang Valid"
            errorTextViewEmail.visibility = View.VISIBLE
        } else {
            errorTextViewEmail.text = ""
            errorTextViewEmail.visibility = View.GONE
        }

        if (password.length < 8) {
            errorPasswordTextView.text = "Password harus lebih dari 8 karakter"
            errorPasswordTextView.visibility = View.VISIBLE
        } else {
            errorPasswordTextView.text = ""
            errorPasswordTextView.visibility = View.GONE
        }
    }

    private fun performRegister() {
        val firstName = nameEditText.text.toString()
        val lastName = lastEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty()|| email.isEmpty() || password.isEmpty() || !email.contains('@')) {
            showToastOnMainThread("Isi semua kolom dengan benar")
            return
        }

        progressDialog = ProgressDialog.show(this, "Registering", "Please wait...", true, false)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = dataRepository.register(firstName,lastName, email, password)

                runOnUiThread {
                    progressDialog?.dismiss()
                }

                if (response.message.isNullOrBlank()) {
                    showToastOnMainThread("Registrasi Berhasil")

                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showToastOnMainThread(response.message ?: "Registrasi Gagal")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressDialog?.dismiss()
                    showToastOnMainThread("Registrasi Berhasil")
                }
            }
        }
    }

    private fun showToastOnMainThread(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
