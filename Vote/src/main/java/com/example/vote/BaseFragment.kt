package com.example.vote

import android.content.Intent
import androidx.fragment.app.Fragment
import com.example.auth.views.LoginActivity
import com.example.votchain.AuthManager

open class BaseFragment : Fragment() {
    protected fun checkAuthAndRedirect() {
        if (!AuthManager.isLoggedIn(requireContext())) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAuthAndRedirect()
    }
}