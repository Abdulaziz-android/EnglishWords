package com.abdulaziz.englishwords

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.abdulaziz.englishwords.databinding.ActivityMainBinding
import com.abdulaziz.englishwords.databinding.ItemDialogBinding
import com.abdulaziz.englishwords.fragments.HomeFragment
import com.abdulaziz.englishwords.fragments.SavedFragment
import com.abdulaziz.englishwords.fragments.SearchFragment
import com.abdulaziz.englishwords.fragments.SeenFragment
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding:ActivityMainBinding

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.toolbar.title = "Home"
        setSupportActionBar(binding.toolbar)
        setUpNavigationDrawer()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()


        binding.bottomNavigation.setOnItemSelectedListener {
            var selectedFragment : Fragment?=null
            when(it.itemId){
                R.id.homeFragment -> {
                    selectedFragment = HomeFragment()
                    binding.toolbar.title = "Home"
                }
                R.id.searchFragment -> {
                    selectedFragment = SearchFragment()
                    binding.toolbar.title = "Searched"
                }
                R.id.saveFragment -> {
                    selectedFragment = SavedFragment()
                    binding.toolbar.title = "Saved"
                }
                R.id.seenFragment -> {
                    selectedFragment = SeenFragment()
                    binding.toolbar.title = "Recent"
                }

                else -> HomeFragment()
            }

            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                selectedFragment!!).commit()
            true
        }

        binding.bottomNavigation.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
    }

    private fun setUpNavigationDrawer() {
        binding.navView.setNavigationItemSelectedListener(this)

        val toogle = ActionBarDrawerToggle(this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close)

        toogle.isDrawerIndicatorEnabled = false
        toogle.setHomeAsUpIndicator(R.drawable.ic_drawer)
        binding.drawerLayout.addDrawerListener(toogle)
        toogle.setToolbarNavigationClickListener {
            if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
            hideKeyboard()
        }
        toogle.syncState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.home_menu -> {
                binding.bottomNavigation.selectedItemId = R.id.homeFragment
                binding.toolbar.title = "Home"
            }
            R.id.search_menu -> {
                binding.bottomNavigation.selectedItemId = R.id.searchFragment
                binding.toolbar.title = "Searched"
            }
            R.id.save_menu -> {
                binding.bottomNavigation.selectedItemId = R.id.saveFragment
                binding.toolbar.title = "Saved"
            }
            R.id.seen_menu -> {
                binding.bottomNavigation.selectedItemId = R.id.seenFragment
                binding.toolbar.title = "Recent"
            }

            R.id.share_menu -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            }
            R.id.info_menu -> {
                val view = findViewById<View>(android.R.id.content) as ViewGroup
                val alertDialog = AlertDialog.Builder(binding.root.context, R.style.SheetDialog)
                val itemDialog = DataBindingUtil.inflate<ItemDialogBinding>(layoutInflater,
                    R.layout.item_dialog,
                    view,
                    false)
                alertDialog.setView(itemDialog.root)
                alertDialog.show()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return false
    }

    private fun hideKeyboard(){
        this.currentFocus?.let { view ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}