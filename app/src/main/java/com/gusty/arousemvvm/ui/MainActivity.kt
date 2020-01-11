package com.gusty.arousemvvm.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gusty.arousemvvm.MusicPlayerService
import com.gusty.arousemvvm.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            val musicPlayerFragment = MusicPlayerFragment()
            addFragmentToFront(musicPlayerFragment)
        }
    }

    private fun addFragmentToFront(fragment: Fragment) {
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
    }

    fun setStatusBarColor(color: Int) {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
        window.navigationBarColor = color
    }


    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(this, MusicPlayerService::class.java)
        stopService(serviceIntent)
    }
}
