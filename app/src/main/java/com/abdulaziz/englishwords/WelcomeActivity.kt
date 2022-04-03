package com.abdulaziz.englishwords

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.abdulaziz.englishwords.adapter.WelcomePagerAdapter
import com.abdulaziz.englishwords.database.WordDatabase
import com.abdulaziz.englishwords.databinding.ActivityWelcomeBinding
import com.google.android.material.tabs.TabLayout

class WelcomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityWelcomeBinding
    lateinit var adapter: WelcomePagerAdapter
    lateinit var database: WordDatabase

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)
        database = WordDatabase.getInstance(binding.root.context)

        if (database.wordDao().isExist()) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
        }

        adapter = WelcomePagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = adapter
        binding.tablayout.setupWithViewPager(binding.viewPager)

        binding.skipTv.setOnClickListener {
            binding.tablayout.selectTab(binding.tablayout.getTabAt(3))
        }
        binding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position==3){
                    binding.tablayout.visibility = View.GONE
                    binding.viewPager.setOnTouchListener { _, _ -> true }
                    binding.skipTv.visibility = View.GONE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })


    }



}