package com.codingwithmitch.openapi.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.RequestManager
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.*
import com.codingwithmitch.openapi.ui.auth.AuthActivity
import com.codingwithmitch.openapi.ui.main.account.*
import com.codingwithmitch.openapi.ui.main.blog.BaseBlogFragment
import com.codingwithmitch.openapi.ui.main.create_blog.BaseCreateFragment
import com.codingwithmitch.openapi.util.*
import com.codingwithmitch.openapi.util.Constants.Companion.PERMISSIONS_REQUEST_READ_STORAGE
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity(),
    BottomNavController.NavGraphProvider,
    BottomNavController.OnNavigationGraphChanged
{

    private val TAG: String = "AppDebug"

    private lateinit var bottomNavigationView: BottomNavigationView

    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(this, R.id.main_nav_host_fragment, R.id.nav_blog)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        setupActionBar()
        Log.d(TAG, "MainActivity: onCreate: called.")

        bottomNavController.apply {
            setNavGraphProvider(this@MainActivity)
            setNavGraphChangeListener(this@MainActivity)
        }
        bottomNavigationView.setUpNavigation(bottomNavController)
        if (savedInstanceState == null) {
            bottomNavController.onNavigationItemSelected()
        }
        subscribeObservers()
    }


    override fun isStoragePermissionGranted(): Boolean{
        if (
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED  ) {


            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSIONS_REQUEST_READ_STORAGE
            )

            return false
        } else {
            // Permission has already been granted
            return true
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(tool_bar)
    }

    override fun getNavGraphId(itemId: Int) = when (itemId) {
        R.id.nav_blog -> {
            R.navigation.nav_blog
        }
        R.id.nav_create_blog -> {
            R.navigation.nav_create_blog
        }
        R.id.nav_account -> {
            R.navigation.nav_account
        }
        else -> {
            R.navigation.nav_blog
        }
    }

    // Cancel previous jobs when navigating to a new graph
    override fun onGraphChange() {
        cancelActiveJobs()
        expandAppBar()
    }

    private fun cancelActiveJobs(){
        val fragments = bottomNavController.fragmentManager.findFragmentById(bottomNavController.containerId)?.childFragmentManager?.fragments
        if(fragments != null){
            for(fragment in fragments){
                if(fragment is BaseAccountFragment){
                    fragment.cancelPreviousJobs()
                }
                if(fragment is BaseBlogFragment){
                    fragment.cancelPreviousJobs()
                }
                if(fragment is BaseCreateFragment){
                    fragment.cancelPreviousJobs()
                }
            }
        }
        displayProgressBar(false)
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    // This was shown to cause problems in testing due to our custom navigation system with the nav bar
    // Instead I'll be using 'onOptionsItemSelected'
//    override fun onSupportNavigateUp(): Boolean = navController
//        .navigateUp()

    fun subscribeObservers(){
        sessionManager.cachedToken.observe(this, Observer{ authToken ->
            Log.d(TAG, "MainActivity, subscribeObservers: ViewState: ${authToken}")
            if(authToken == null || authToken.account_pk == -1 || authToken.token == null){
                navAuthActivity()
                finish()
            }
        })
    }

    private fun navAuthActivity(){
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun displayProgressBar(bool: Boolean){
        if(bool){
            lottie_progress_bar.visibility = View.VISIBLE
        }
        else{
            lottie_progress_bar.visibility = View.GONE
        }
    }

    override fun setActionBarTitle(title: String) {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setTitle(title)
    }

    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }
}






















