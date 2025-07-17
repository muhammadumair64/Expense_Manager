package com.iobits.budgetexpensemanager.ui.viewModels

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.iobits.budgetexpensemanager.localDatabase.entities.Account
import com.iobits.budgetexpensemanager.localDatabase.entities.Categories
import com.iobits.budgetexpensemanager.localDatabase.entities.Profile
import com.iobits.budgetexpensemanager.repos.MainRepo
import com.iobits.budgetexpensemanager.localDatabase.entities.Budget
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.utils.K
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.Url
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: MainRepo) : ViewModel() {
    val TAG = "AuthViewModelTag"

    var createAccount: ((Boolean) -> Unit)? = null
    var result: ((Boolean) -> Unit)? = null
    val profileData = MutableLiveData<Profile>()
    var email = ""

    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    var startLoading:((Boolean)-> Unit)? = null

    //------------------------------------------ Sign in -----------------------------------------//
    fun signUp(
        mAuth: FirebaseAuth,
        email: String,
        password: String,
        requireContext: Context,
        requireActivity: FragmentActivity
    ) {
        mAuth.createUserWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener(requireActivity,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        // Sign up success
                        Toast.makeText(
                            requireContext,
                            "Sign up successful!",
                            Toast.LENGTH_SHORT
                        ).show()
                        this.email = email
                        result?.invoke(true)
                        // Proceed to your main activity or any other activity
                    } else {
                        // Sign up failed
                        Toast.makeText(
                            requireContext,
                            "Sign up failed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        result?.invoke(false)
                    }
                })
    }

    fun signIn(
        mAuth: FirebaseAuth,
        email: String,
        password: String,
        requireContext: Context,
        requireActivity: FragmentActivity
    ) {
        mAuth.signInWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener(requireActivity,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        // Sign up success
                        Toast.makeText(
                            requireContext,
                            "Sign In successful!",
                            Toast.LENGTH_SHORT
                        ).show()
                        mAuth.uid?.let { getProfileFromCloud(it) }
                        Log.d(TAG, "signIn: Auth UID is ${mAuth.uid}")
                        // Proceed to your main activity or any other activity
                    } else {
                        // Sign up failed
                        Toast.makeText(
                            requireContext,
                            "Sign In failed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        this.email = email
                        result?.invoke(false)
                    }
                })
    }
    fun forgotPassword(
        mAuth: FirebaseAuth,
        email: String,
        requireContext: Context,
        requireActivity: FragmentActivity,
    ) {
        mAuth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener(requireActivity,
                OnCompleteListener<Void?> { task ->
                    if (task.isSuccessful) {
                        // Password reset email sent successfully
                        Toast.makeText(
                            requireContext,
                            "Password reset email sent. Check your email.",
                            Toast.LENGTH_SHORT
                        ).show()
                        result?.invoke(true)
                    } else {
                        // Failed to send password reset email
                        Toast.makeText(
                            requireContext,
                            "Failed to send password reset email. Check your email address.",
                            Toast.LENGTH_SHORT
                        ).show()
                        result?.invoke(false)
                    }
                })
    }
    //------------------------------------------ Profile -----------------------------------------//
  fun insertProfile(profile: Profile) {
        profileData.postValue(profile)
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertProfile(profile)
            withContext(Dispatchers.Main){
                result?.invoke(true)
            }
        }
    }

    fun uploadProfile(profile: Profile, totalAmount: String) {
        try {
            val profileData = hashMapOf(
                "id" to profile.id,
                "name" to profile.name,
                "pic" to profile.pic,
                "email" to profile.email,
                "currency" to profile.currency
            )
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            val uid = currentUser?.uid

            // FireStore collection name where you want to store profiles
            val profilesCollection = fireStore.collection(K.FIRE_STORE_NAME)
            if (uid != null) {
                profilesCollection.document("Users")
                    .collection(uid)
                    .document("Profile")
                    .set(profileData)
                    .addOnSuccessListener {
                        Log.d(TAG, "uploadProfile: Successful ")
                        val emptyList = ArrayList<Transaction>()
                        val emptyBudgetList = ArrayList<Budget>()

                            createAccount(
                                Account(
                                    0,
                                    totalAmount.toFloat(),
                                    totalAmount.toFloat(),
                                    emptyList,
                                    emptyBudgetList,
                                    0f,
                                    0f,
                                    getCurrentDateTime()
                                )
                            )

                        insertProfile(profile)

                    }
                    .addOnFailureListener { e ->
                        // Handle any errors
                        Log.d(TAG, "uploadProfile: ERROR ${e.localizedMessage} ")
                        result?.invoke(false)
                        startLoading?.invoke(true)
                    }
            }
        }catch (e:Exception){
            Log.d(TAG, "uploadProfile: Error ${e.localizedMessage}")}
    }

    fun getProfileFromLocalDb() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getProfile()
            if(profile != null){
                profileData.postValue(profile)
                K.SYMBOL = profile.currency
            }
          // Handle the profile data accordingly, for example, update LiveData
        }
    }

    fun getProfileFromCloud(uid: String) {
        val profilesCollection = fireStore.collection(K.FIRE_STORE_NAME)
        profilesCollection.document("Users")
            .collection(uid)
            .document("Profile")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Convert document data to Profile object
                    val profileDoc = documentSnapshot.data
                    val id = (profileDoc?.get("id") as Long).toInt()
                    val profile = Profile(
                        id = id,
                        name = profileDoc["name"] as String,
                        pic = profileDoc["pic"] as String,
                        email = profileDoc["email"] as String,
                        currency = profileDoc["currency"] as String
                    )
                    Log.d(TAG, "getProfileFromCloud: my Profile $profile")
                    insertProfile(profile)
                    fetchUserAccount(uid)
                    fetchCategories()
                }else{
                    createAccount?.invoke(true)
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors
                Log.d(TAG, "fetchUserProfile: ERROR ${e.localizedMessage}")
                createAccount?.invoke(true)
            }
    }

    fun uploadImageAndStoreUrl(imageUri: Uri, amount : String) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        // Accessing Firebase Storage
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference

        // Defining reference for the image in Firebase Storage
        val imagesRef: StorageReference = storageRef.child("images/$uid")

        // Uploading image to Firebase Storage
        val uploadTask = imagesRef.putFile(imageUri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Image uploaded successfully
            // Getting download URL of the uploaded image
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                val profile = profileData.value
                profile?.pic =  imageUrl
                Log.d(TAG, "uploadImageAndStoreUrl: Profile is $profile")
                if (profile != null) {
                    uploadProfile(profile,amount)
                }
            }
        }.addOnFailureListener { e ->
            // Error uploading image to Firebase Storage
        startLoading?.invoke(false)
        }
    }

    //------------------------------------------ Account -----------------------------------------//
    private fun createAccount(account: Account) {
        val accountData = hashMapOf(
            "id" to account.id,
            "totalAmount" to account.totalAmount,
            "currentBalance" to account.currentBalance,
            "transactions" to account.transactions,
            "budgets" to account.budgets,
            "income" to account.income,
            "expense" to account.expense,
            "date" to account.date
        )
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        // FireStore collection name where you want to store profiles
        val profilesCollection = fireStore.collection(K.FIRE_STORE_NAME)
        if (uid != null) {
            profilesCollection.document("Users")
                .collection(uid)
                .document("Account")
                .set(accountData)
                .addOnSuccessListener {
                    Log.d(TAG, "uploadAccount: Successful ")
                    insertAccount(account.totalAmount,false)
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                    Log.d(TAG, "uploadAccount: ERROR ${e.localizedMessage} ")
                }
        }
    }
   fun insertAccount(totalAmount:Float, isFromSignIn:Boolean) {
        val emptyList = ArrayList<Transaction>()
        val emptyBudgetList = ArrayList<Budget>()
       val account =  Account(
            0,
            totalAmount.toFloat(),
            totalAmount.toFloat(),
            emptyList,
            emptyBudgetList,
            0f,
            0f,
            getCurrentDateTime()
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAccount(account)
            if(isFromSignIn){
                result?.invoke(true)
            }
        }
    }

    private fun updateAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAccount(account)
        }
    }

    private fun fetchUserAccount(uid: String) {
        val profilesCollection = fireStore.collection(K.FIRE_STORE_NAME)
        profilesCollection.document("Users")
            .collection(uid)
            .document("Account")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Convert document data to Account object
                    val accountData = documentSnapshot.data
                    val account = Account(
                        id = 0,
                        totalAmount = (accountData?.get("totalAmount") as Double).toFloat(),
                        currentBalance = (accountData["currentBalance"] as Double).toFloat(),
                        transactions = accountData["transactions"] as ArrayList<Transaction>,
                        budgets = accountData["budgets"] as ArrayList<Budget>,
                        income = (accountData["income"] as Double).toFloat(),
                        expense = (accountData["expense"] as Double).toFloat(),
                        date = accountData["date"] as String
                    )
                    // Do something with the account data, such as updating UI
                    Log.d(TAG, "fetchUserAccount: Fetch account successfully done")
                    updateAccount(account)
                } else {
                    // Document doesn't exist
                    result?.invoke(false)
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors
                Log.d(TAG, "fetchUserAccount: ERROR ${e.localizedMessage}")
            }
    }
    //---------------------------------------  Categories ----------------------------------------//

    private fun fetchCategories() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            val categoriesCollection = fireStore.collection(K.FIRE_STORE_NAME)
                .document("Users")
                .collection(uid)
                .document("Categories")

            categoriesCollection.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val categoryData = documentSnapshot.data
                        if (categoryData != null) {
                            val id = categoryData["id"] as Long
                            val expenseList = categoryData["expenseList"] as ArrayList<String>
                            val incomeList = categoryData["incomeList"] as ArrayList<String>
                            val categories = Categories(id.toInt(), expenseList, incomeList)
                            // Do something with the categories, such as updating UI
                            Log.d(TAG, "fetchCategories: Fetch categories successfully done")
                            insertCategories(categories)
                        }
                    } else {
                        // Document doesn't exist
                        Log.d(TAG, "fetchCategories: Categories document doesn't exist")
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                    Log.d(TAG, "fetchCategories: ERROR ${e.localizedMessage}")
                }
        }
    }
    private fun insertCategories(categories: Categories) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCategories(categories)
        }
    }

    //------------------------------------------ validators --------------------------------------//

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        if (password.firstOrNull { it.isDigit() } == null) return false
        if (password.filter { it.isLetter() }.firstOrNull { it.isUpperCase() } == null) return false
        if (password.filter { it.isLetter() }.firstOrNull { it.isLowerCase() } == null) return false
        return password.firstOrNull { !it.isLetterOrDigit() } != null
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val currentDateTime = Date()
        return dateFormat.format(currentDateTime)
    }
}
