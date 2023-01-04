package com.example.exomine.ui

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.example.exomine.R
import com.example.exomine.databinding.ActivityMainBinding
import com.example.exomine.util.PermissionUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private  val mainViewModel: MainViewModel by viewModels()

    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding  = DataBindingUtil.setContentView(this, R.layout.activity_main)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        PermissionUtil.checkPermissions(
            this,
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}, PermissionUtil.NOTIFICATION_REQUIRED_PERMISSIONS
        )



    }







}