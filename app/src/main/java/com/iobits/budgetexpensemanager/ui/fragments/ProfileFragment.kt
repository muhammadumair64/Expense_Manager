package com.iobits.budgetexpensemanager.ui.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentProfileBinding
import com.iobits.budgetexpensemanager.managers.AnalyticsManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.viewModels.AuthViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.PopupMenuCustomLayout
import com.iobits.budgetexpensemanager.utils.TinyDB
import com.iobits.budgetexpensemanager.utils.clearBackStack
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.safeNavigate
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private val binding by lazy {
        FragmentProfileBinding.inflate(layoutInflater)
    }
    private val mainViewModel: MainViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private var  tinyDB :TinyDB?=null
//    private val launcher = registerImagePicker { images ->
//        // selected images
//        if(images.isNotEmpty()){
//            val image = images[0]
//            uploadImage(image.uri)
//        }
//    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        tinyDB = TinyDB(requireContext())
        authViewModel.getProfileFromLocalDb()
        initViews()
        return binding.root
    }

    fun initViews() {
        AnalyticsManager.logEvent("User_Is_In_Profile",null)
        mainViewModel.getAccount().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.totalBalance.text = it.totalAmount.toString()
            }
        })
        authViewModel.profileData.observe(viewLifecycleOwner, Observer {
            binding.apply {
                accountName.text = it.name
                currency.text = it.currency
                if(it.pic !=  ""){
                    Glide.with(requireContext())
                       .load(Uri.parse(it.pic))
                       .into(binding.profileImage)
                }
            }
        })
        binding.apply {
        confirm.setOnClickListener {
            MyApplication.mInstance.adsManager.loadInterstitialAd(requireActivity()){
             safeNavigate(R.id.action_profileFragment_to_dashboardFragment, R.id.profileFragment)
            }
        }
        backBtn.setOnClickListener {
            safeNavigate(R.id.action_profileFragment_to_dashboardFragment, R.id.profileFragment)
        }
            edit.gone()
            edit.setOnClickListener {
                if(tinyDB?.getBoolean(K.IsLoginSkipped)==false){
//                openImagePicker()
                }else{
                    Toast.makeText(requireContext(), "User Not Login", Toast.LENGTH_SHORT).show()
                }
            }
        menu.setOnClickListener {
            val popupMenu = PopupMenuCustomLayout(
                requireContext(), R.layout.custome_menu
            ) { item ->
                when (item) {
                    R.id.log_out -> {
                        if(tinyDB?.getBoolean(K.IsLoginSkipped)==false){
                            logout()
                            authViewModel.startLoading?.invoke(true)
                        }else{
                           // Toast.makeText(requireContext(), "User Not Login", Toast.LENGTH_SHORT).show()
                            mainViewModel.deleteDataBase()
                            tinyDB?.putBoolean(K.IsLoginSkipped,false)
                            safeNavigate(R.id.action_profileFragment_to_loginFragment,R.id.profileFragment)
                            clearBackStack(R.id.loginFragment,false)
                        }
                    }

                    R.id.delete_account -> {
                        if(tinyDB?.getBoolean(K.IsLoginSkipped)==false){
                            deleteAccount()
                            authViewModel.startLoading?.invoke(true)
                        }else{
                          //  Toast.makeText(requireContext(), "User Not Login", Toast.LENGTH_SHORT).show()
                            mainViewModel.deleteDataBase()
                            tinyDB?.putBoolean(K.IsLoginSkipped,false)
                            safeNavigate(R.id.action_profileFragment_to_loginFragment,R.id.profileFragment)
                            clearBackStack(R.id.loginFragment,false)
                        }
                    }
                }
            }
            popupMenu.showSettingDialog(it)
        }
        }

    }

//    private fun openImagePicker() {
//        val config = ImagePickerConfig(
//            isFolderMode = true,
//            isShowCamera = true,
//            limitSize = 1,
//            selectedIndicatorType = IndicatorType.NUMBER,
//            rootDirectory = RootDirectory.DCIM,
//            subDirectory = "Image Picker",
//            folderGridCount = GridCount(2, 4),
//            imageGridCount = GridCount(3, 5),
//            customColor = CustomColor(
//                background = "#FFFFFF",
//                statusBar = "#000000",
//                toolbar = "#212121",
//                toolbarTitle = "#FFFFFF",
//                toolbarIcon = "#FFFFFF",
//            ),
//            customMessage = CustomMessage(
//                reachLimitSize = "You can only select up to 10 images.",
//                noImage = "No image found.",
//                noPhotoAccessPermission = "Please allow permission to access photos and media.",
//                noCameraPermission = "Please allow permission to access camera."
//            )
//            // see more options below
//        )
//
//        launcher.launch(config)
//    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        mainViewModel.deleteDataBase()
        safeNavigate(R.id.action_profileFragment_to_loginFragment,R.id.profileFragment)
        clearBackStack(R.id.loginFragment,false)
        Toast.makeText(requireContext(), "Successfully Log Out", Toast.LENGTH_SHORT)
            .show()
        authViewModel.startLoading?.invoke(false)
        // You can also navigate the user to the login screen or perform any other action here
    }

    private fun deleteAccount() {

        mainViewModel.deleteFirebaseAccount = {
            if (it) {
                removeUser()
            } else {
                Toast.makeText(requireContext(), "Error Delete Account failed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        lifecycleScope.launch {
            mainViewModel.deleteDataBase()
            if(tinyDB?.getBoolean(K.IsLoginSkipped)== false){
            mainViewModel.deleteUserFromFirebase()
            }
        }
    }

    private fun uploadImage(image: Uri) {
        authViewModel.startLoading?.invoke(true)
        lifecycleScope.launch {
            authViewModel.uploadImageAndStoreUrl(image,binding.totalBalance.text.toString())
            Glide.with(requireContext())
                .load(image)
                .into(binding.profileImage)
            authViewModel.startLoading?.invoke(false)
        }
    }

    private fun removeUser()
    {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        currentUser?.delete()
            ?.addOnSuccessListener {
                // Handle success
                safeNavigate(R.id.action_profileFragment_to_loginFragment,R.id.profileFragment)
                clearBackStack(R.id.loginFragment,false)
                Toast.makeText(requireContext(), "Successfully Delete Account", Toast.LENGTH_SHORT)
                    .show()
                authViewModel.startLoading?.invoke(false)
            }
            ?.addOnFailureListener { e ->
                // Handle failure
                Toast.makeText(requireContext(), "Error Delete Account failed", Toast.LENGTH_SHORT)
                    .show()
                authViewModel.startLoading?.invoke(false)
            }
    }
}
