package com.skripsi.jalu.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skripsi.jalu.MainActivity
import com.skripsi.jalu.data.DataRepository
import com.skripsi.jalu.databinding.ActivityEntryBinding
import kotlinx.coroutines.launch

class EntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEntryBinding
    private lateinit var dataRepository: DataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataRepository = DataRepository(this)

        checkUserLoggedIn()

        binding.button.setOnClickListener {
            // Handle button click to navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.button2.setOnClickListener {
            // Handle button2 click to navigate to RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkUserLoggedIn() {
        val userTokenFlow = dataRepository.getUserToken()

        lifecycleScope.launch {
            userTokenFlow.collect { token ->
                if (!token.isNullOrBlank()) {
                    startMainActivity()
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
