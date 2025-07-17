package com.iobits.budgetexpensemanager.utils

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.iobits.budgetexpensemanager.R

class GoogleSignInHelper(private val activity: AppCompatActivity) {



    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    init {
        configureGoogleSignIn()
        mAuth = FirebaseAuth.getInstance()
    }

    fun initialize() {
        configureActivityResultLauncher()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        googleSignInClient.revokeAccess()
    }

    private fun configureActivityResultLauncher() {
        signInLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    Toast.makeText(
                        activity,
                        "Google sign in failed: ${e.statusCode}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = mAuth.currentUser

                    result?.invoke(true, user?.email.toString())
                    // Proceed to your main activity or any other activity
                } else {
                    // Sign in failed
                    result?.invoke(false, "")
                }
            }
    }

    companion object {
        const val RC_SIGN_IN = 9001
        var result : ((Boolean,String)->Unit)? = null
        var onStart : (()->Unit)? = null
    }
}
