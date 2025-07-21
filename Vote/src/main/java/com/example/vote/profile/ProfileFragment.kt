package com.example.vote.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.example.auth.viewmodel.AuthViewModel
import com.example.auth.views.LoginActivity
import com.example.vote.R

class ProfileFragment : androidx.fragment.app.Fragment() {

    private lateinit var logoutButton: ConstraintLayout
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        logoutButton = view.findViewById(R.id.logoutContainer)

        setupLogout(view)

        return view
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
        viewModel.logout(requireContext()) // Pastikan ViewModel punya fungsi ini
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}