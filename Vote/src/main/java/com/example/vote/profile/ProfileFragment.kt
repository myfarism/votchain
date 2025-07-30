package com.example.vote.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.example.auth.viewmodel.AuthViewModel
import com.example.auth.views.LoginActivity
import com.example.votchain.AuthManager
import com.example.vote.R

class ProfileFragment : androidx.fragment.app.Fragment() {
    private lateinit var logoutButton: ConstraintLayout
    private lateinit var userNameTextView: TextView
    private lateinit var prodiTextView: TextView
    private lateinit var addressTextView: TextView
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        logoutButton = view.findViewById(R.id.logoutContainer)
        userNameTextView = view.findViewById(R.id.yourName)
        prodiTextView = view.findViewById(R.id.yourProdi)
        addressTextView = view.findViewById(R.id.yourAlamat)

        setupLogout(view)
        loadUserProfile()

        return view
    }

    private fun loadUserProfile() {
        try {
            val username = AuthManager.getUserName(requireContext())
            val prodi = AuthManager.getProdi(requireContext())
            val address = AuthManager.getUserAddress(requireContext())

            if (!username.isNullOrEmpty()) {
                userNameTextView.text = username
                Log.d("ProfileFragment", "Username loaded: $username")
            } else {
                userNameTextView.text = "User"
            }

            if (!prodi.isNullOrEmpty()) {
                prodiTextView.text = prodi
                Log.d("ProfileFragment", "Prodi loaded: $prodi")
            }

            if (!address.isNullOrEmpty()) {
                addressTextView.text = shortenAddress(address)
                Log.d("ProfileFragment", "Address loaded: $address")
            }

        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error loading user profile: ${e.message}")
            userNameTextView.text = "User"
        }
    }


    override fun onResume() {
        super.onResume()
        // Refresh user name when fragment resumes
        loadUserProfile()
    }

    private fun setupLogout(view: View) {
        val logoutContainer: View = view.findViewById(R.id.logoutContainer)
        logoutContainer.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Apakah Anda ingin logout?")
            .setPositiveButton("Ya") { dialog, _ ->
                logout()
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun logout() {
        viewModel.logout(requireContext())
        AuthManager.logout(requireContext())
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun shortenAddress(address: String?): String {
        if (address.isNullOrEmpty()) return "-"
        return if (address.length > 10) {
            "${address.substring(0, 6)}...${address.takeLast(5)}"
        } else {
            address
        }
    }
}