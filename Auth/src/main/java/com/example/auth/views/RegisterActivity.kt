package com.example.auth.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.auth.databinding.ActivityRegisterBinding
import com.example.auth.viewmodel.AuthState
import com.example.auth.viewmodel.AuthViewModel
import com.example.votchain.MainActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup dropdown untuk Prodi
        val prodiList = listOf(
            "Teknik Informatika",
            "Sistem Informasi",
            "Teknik Sipil",
            "Manajemen",
            "Akuntansi",
            "Psikologi",
            "Ilmu Komunikasi",
            "Desain Produk",
            "Desain Komunikasi Visual",
            "Arsitektur"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, prodiList)
        binding.prodiEditText.setAdapter(adapter)

        // Set click listener untuk dropdown
        binding.prodiEditText.setOnClickListener {
            binding.prodiEditText.showDropDown()
        }

        // Button listeners
        binding.btnDaftar.setOnClickListener {
            registerUser()
        }

        binding.tvMasuk.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser() {
        val email = binding.registerEmailEditText.text.toString().trim()
        val username = binding.userEditText.text.toString().trim()
        val prodi = binding.prodiEditText.text.toString().trim()
        val nim = binding.nimEditText.text.toString().trim()
        val password = binding.pwEditText.text.toString()
        val confirmPassword = binding.confirmpwEditText.text.toString()

        // Validasi input
        when {
            email.isEmpty() -> {
                binding.registerEmailEditText.error = "Email tidak boleh kosong"
                return
            }
            username.isEmpty() -> {
                binding.userEditText.error = "Nama pengguna tidak boleh kosong"
                return
            }
            prodi.isEmpty() -> {
                Toast.makeText(this, "Silakan pilih prodi", Toast.LENGTH_SHORT).show()
                return
            }
            nim.isEmpty() -> {
                binding.nimEditText.error = "NIM tidak boleh kosong"
                return
            }
            password.isEmpty() -> {
                binding.pwEditText.error = "Kata sandi tidak boleh kosong"
                return
            }
            confirmPassword.isEmpty() -> {
                binding.confirmpwEditText.error = "Konfirmasi kata sandi tidak boleh kosong"
                return
            }
            password != confirmPassword -> {
                binding.confirmpwEditText.error = "Kata sandi tidak sama"
                return
            }
        }

        viewModel.register(this, email, username, prodi, nim, password, confirmPassword)
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnDaftar.isEnabled = false
                    }
                    is AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    }
                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnDaftar.isEnabled = true
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
}