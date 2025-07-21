package com.example.votchain

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.votchain.databinding.ActivityMainBinding
import com.google.android.play.core.splitcompat.SplitCompat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: Toolbar

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        SplitCompat.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek status login
        if (!AuthManager.isLoggedIn(this)) {
            redirectToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI(savedInstanceState)
    }

    private fun setupUI(savedInstanceState: Bundle?) {
        val bottomNavigationView = binding.bottomNavigation

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_beranda
            loadDynamicFragment("com.example.vote.beranda.BerandaFragment")
        }

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val className = when (menuItem.itemId) {
                R.id.nav_beranda -> "com.example.vote.beranda.BerandaFragment"
                R.id.nav_profil -> "com.example.vote.profile.ProfileFragment"
                else -> null
            }

            className?.let {
                loadDynamicFragment(it)
                true
            } ?: false
        }
    }

    private fun redirectToLogin() {
        val intent = Intent().setClassName(
            this,
            "com.example.auth.views.LoginActivity"
        )
        startActivity(intent)
        finish()
    }

    private fun loadDynamicFragment(className: String): Boolean {
        return try {
            val clazz = Class.forName(className)
            val fragment = clazz.newInstance() as Fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

            toolbar.visibility = if (fragment.javaClass.simpleName.contains("Beranda")) View.VISIBLE else View.GONE
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
