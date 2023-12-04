package com.aubrey.recepku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.aubrey.recepku.databinding.ActivityMainBinding
import com.aubrey.recepku.view.fragments.AddFragment
import com.aubrey.recepku.view.fragments.FavoriteFragment
import com.aubrey.recepku.view.fragments.HomeFragment
import com.aubrey.recepku.view.fragments.ProfileFragment
import com.aubrey.recepku.view.fragments.SettingFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HomeFragment())

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.favorite -> {
                    replaceFragment(FavoriteFragment())
                    true
                }
                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                R.id.setting -> {
                    replaceFragment(SettingFragment())
                    true
                }
                R.id.add -> {
                    replaceFragment(AddFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment){
        if (fragment != null){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_container, fragment)
            transaction.commit()
        }
    }
}