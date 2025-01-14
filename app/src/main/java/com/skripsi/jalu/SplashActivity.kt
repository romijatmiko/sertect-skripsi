package com.skripsi.jalu

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.skripsi.jalu.ui.login.EntryActivity // Sesuaikan dengan nama package dan kelas EntryActivity yang sesuai

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val intent = Intent(
                this@SplashActivity,
                EntryActivity::class.java
            )
            startActivity(intent)
            finish()
        }, 3000)
    }
}
