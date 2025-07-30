package com.example.auth.views

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.auth.R
import com.example.auth.viewmodel.AuthState
import com.example.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

// VerifyEmailActivity.kt
class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var otp1: EditText
    private lateinit var otp2: EditText
    private lateinit var otp3: EditText
    private lateinit var otp4: EditText
    private lateinit var btnCek: ImageButton
    private lateinit var btnResend: ImageButton
    private lateinit var timerTextView: TextView
    private lateinit var progressBar: ProgressBar

    private var countDownTimer: CountDownTimer? = null
    private var email: String = ""
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_email)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get email from intent
        email = intent.getStringExtra("email") ?: ""
        if (email.isEmpty()) {
            showToast("Email tidak ditemukan")
            finish()
            return
        }

        initViews()
        setupViewModel()
        setupOtpInput()
        setupClickListeners()
        startTimer()
        observeAuthState()
    }

    private fun initViews() {
        otp1 = findViewById(R.id.otp1)
        otp2 = findViewById(R.id.otp2)
        otp3 = findViewById(R.id.otp3)
        otp4 = findViewById(R.id.otp4)
        btnCek = findViewById(R.id.btnCek)
        btnResend = findViewById(R.id.btnResend)
        timerTextView = findViewById(R.id.timerTextView)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
    }

    private fun setupOtpInput() {
        val otpFields = listOf(otp1, otp2, otp3, otp4)

        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        // Move to next field
                        otpFields[index + 1].requestFocus()
                    } else if (s?.isEmpty() == true && index > 0) {
                        // Move to previous field on backspace
                        otpFields[index - 1].requestFocus()
                    }

                    // Check if all fields are filled
                    if (isOtpComplete()) {
                        btnCek.isEnabled = true
                        btnCek.alpha = 1.0f
                    } else {
                        btnCek.isEnabled = false
                        btnCek.alpha = 0.5f
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && index > 0) {
                        otpFields[index - 1].requestFocus()
                        otpFields[index - 1].setText("")
                    }
                }
                false
            }
        }

        // Initially disable check button
        btnCek.isEnabled = false
        btnCek.alpha = 0.5f
    }

    private fun setupClickListeners() {
        btnCek.setOnClickListener {
            if (isOtpComplete()) {
                cekOtp()
            } else {
                showToast("Masukkan kode OTP lengkap")
            }
        }

        btnResend.setOnClickListener {
            resendOtp()
        }
    }

    private fun cekOtp() {
        val otp = getOtpString()
        if (otp.length == 4) {
            // Pass context to verifyOtp method
            authViewModel.verifyOtp(this, email, otp)
        } else {
            showToast("Masukkan kode OTP 4 digit")
        }
    }

    private fun resendOtp() {
        authViewModel.resendOtp(email)
        clearOtpFields()
        startTimer()
    }

    private fun getOtpString(): String {
        return "${otp1.text}${otp2.text}${otp3.text}${otp4.text}"
    }

    private fun isOtpComplete(): Boolean {
        return otp1.text.length == 1 &&
                otp2.text.length == 1 &&
                otp3.text.length == 1 &&
                otp4.text.length == 1
    }

    private fun clearOtpFields() {
        otp1.setText("")
        otp2.setText("")
        otp3.setText("")
        otp4.setText("")
        otp1.requestFocus()
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        isTimerRunning = true
        btnResend.isEnabled = false
        btnResend.alpha = 0.5f

        countDownTimer = object : CountDownTimer(300000, 1000) { // 5 minutes
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "00:00"
                isTimerRunning = false
                btnResend.isEnabled = true
                btnResend.alpha = 1.0f
                showToast("Kode OTP telah kedaluwarsa")
            }
        }
        countDownTimer?.start()
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Error -> {
                        progressBar.visibility = View.GONE
                        showToast(state.message)
                        btnCek.isEnabled = isOtpComplete()
                        if (!isTimerRunning) {
                            btnResend.isEnabled = true
                        }

                        // Clear OTP fields on error
                        clearOtpFields()
                    }
                    AuthState.Idle -> {
                        progressBar.visibility = View.GONE
                        btnCek.isEnabled = isOtpComplete()
                        if (!isTimerRunning) {
                            btnResend.isEnabled = true
                        }
                    }
                    AuthState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        btnCek.isEnabled = false
                        btnResend.isEnabled = false
                    }
                    is AuthState.RequiresVerification -> {
                        progressBar.visibility = View.GONE
                        showToast(state.message)
                    }
                    is AuthState.Success -> {
                        progressBar.visibility = View.GONE
                        showToast(state.message)

                        // Navigate to MainActivity after successful verification
                        startActivity(Intent(this@VerifyEmailActivity, LoginActivity::class.java))
                        finishAffinity() // Clear all previous activities
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}