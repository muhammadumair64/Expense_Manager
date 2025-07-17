package com.iobits.budgetexpensemanager.ui.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.ActivityMainBinding
import com.iobits.budgetexpensemanager.managers.PreferenceManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.viewModels.AuthViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.DataShareViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.GoogleSignInHelper
import com.iobits.budgetexpensemanager.utils.changeStatusBarColor
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
val TAG = "MainActivityTAG"
    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val shareViewModel: DataShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initListeners()
//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
//            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            view.setPadding(
//                systemBarsInsets.left,
//                systemBarsInsets.top,
//                systemBarsInsets.right,
//                systemBarsInsets.bottom
//            )
//            WindowInsetsCompat.CONSUMED
//        }
      changeStatusBarColor(this, R.color.white)
        initLoading()
        fetchRemoteConfig()
        val googleSignInHelper = GoogleSignInHelper(this)
        googleSignInHelper.initialize()
        GoogleSignInHelper.onStart = {
            googleSignInHelper.signIn()
        }
    }

    private fun initListeners() {
//        shareViewModel.addTopSpacing = {
//            if (it) {
//                ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
//                    val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//                    view.setPadding(
//                        systemBarsInsets.left,
//                        0,
//                        systemBarsInsets.right,
//                        systemBarsInsets.bottom
//                    )
//                    WindowInsetsCompat.CONSUMED
//                }
//            } else {
//                ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
//                    val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//                    view.setPadding(
//                        systemBarsInsets.left,
//                        systemBarsInsets.top,
//                        systemBarsInsets.right,
//                        systemBarsInsets.bottom
//                    )
//                    WindowInsetsCompat.CONSUMED
//                }
//            }
//        }

    }

    private fun initLoading() {
        authViewModel.startLoading = {
            runOnUiThread {
                if (it) {
                    binding.apply {
                        window?.setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        )
                        loading.visible()
                    }
                } else {
                    binding.apply {
                        window?.clearFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        )
                        loading.gone()
                    }
                }
            }
        }
        mainViewModel.startLoading = {
            runOnUiThread {
                if (it) {
                    binding.loading.visible()
                } else {
                    binding.loading.gone()
                }
            }
        }
    }
    private fun fetchRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(7000)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        Handler(Looper.getMainLooper()).postDelayed({

            remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this
                ) { task ->
                    if (task.isSuccessful) {
                        val updated = task.result
                        Log.d(TAG, "Config params updated: $updated")

                    } else {
                        Log.d(TAG, "Fetch Failed")
                    }
                    Log.d(
                        TAG,
                        "fetchRemoteConfig: success and value is: " + remoteConfig.getBoolean("ads_enable")
                    )
                    MyApplication.mInstance.preferenceManager.put(
                        PreferenceManager.Key.REMOTE_CONFIG_IS_GL_ENABLE,
                        remoteConfig.getBoolean("is_enable_google_login")
                    )
                }
        }, 1500)
    }
}
