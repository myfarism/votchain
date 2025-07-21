package com.example.auth.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.auth.R
import com.example.auth.databinding.ActivityLoginBinding
import com.example.auth.viewmodel.AuthState
import com.example.auth.viewmodel.AuthViewModel
import com.example.votchain.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.buttonMasuk.setOnClickListener {
            loginUser()
        }

        binding.tvSandiMasuk.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            // Implementasi lupa password
            Toast.makeText(this, "Fitur lupa password belum tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        viewModel.login(email, password)
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.buttonMasuk.isEnabled = false
                    }
                    is AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.buttonMasuk.isEnabled = true
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
}