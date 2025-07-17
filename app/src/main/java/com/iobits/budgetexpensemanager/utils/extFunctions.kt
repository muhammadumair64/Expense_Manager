package com.iobits.budgetexpensemanager.utils

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.iobits.budgetexpensemanager.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


fun Fragment.getCurrentDateTime(): String {
    val dateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())
    val currentDateTime = Date()
    return dateFormat.format(currentDateTime)
}
fun Fragment.getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
    val currentDateTime = Date()
    return dateFormat.format(currentDateTime)
}
fun Fragment.getCurrentTime(): String {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val currentTime = Date()
    return timeFormat.format(currentTime)
}
fun Fragment.getCurrentMonthNumber(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.MONTH) // Adding 1 because months are zero-based
}

fun Activity.changeStatusBarColor(activity : Activity , colorId : Int){
    val window: Window = activity.window
// Set the system UI visibility flags to indicate light status bar icons
    activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    window.statusBarColor = ContextCompat.getColor(activity,colorId)
}
fun Fragment.changeStatusBarColor(activity : Activity , colorId : Int){
    val window: Window = activity.window
// Set the system UI visibility flags to indicate light status bar icons
    activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    window.statusBarColor = ContextCompat.getColor(activity,colorId)
}


fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}
fun ByteArray.toBitmap(): Bitmap? {
    return BitmapFactory.decodeByteArray(this, 0, size)
}
fun View.gone() {
    visibility = View.GONE
}

fun EditText.onDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

fun Fragment.disableMultipleClicking(view: View, delay: Long = 750) {
    view.isEnabled = false
    this.lifecycleScope.launch {
        delay(delay)
        view.isEnabled = true
    }
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            try {
                if(value != null){
                    observer.onChanged(value)
                    removeObserver(this)
                }
            }catch (e:Exception){
                Log.d("observeOnce", "onChanged: ${e.localizedMessage}")}

        }
    })
}

fun Fragment.popBackStack() {
    try {
        findNavController().navigateUp()
    } catch (e: IllegalArgumentException) {
        Log.e("CLEAR_BACKSTACK_ERROR", "Error clearing back stack: ${e.localizedMessage}")
    }
}

fun Fragment.handleBackPress(onBackPressed: () -> Unit) {
    var lastBackPressedTime = 0L  // Variable to store the last back button press time

    requireView().isFocusableInTouchMode = true
    requireView().requestFocus()
    requireView().setOnKeyListener { _, keyCode, event ->
        if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressedTime > 1000) {  // Check if more than 2 seconds have passed
                lastBackPressedTime = currentTime
                onBackPressed() // Call the provided callback function
            }
            true
        } else false
    }
}
fun Fragment.navigateTo(actionId: Int, destinationName: Int) {
    findNavController().navigate(
        actionId, null, NavOptions.Builder().setPopUpTo(destinationName, true).build()
    )
}
fun Fragment.clearBackStack(destinationId: Int, inclusive: Boolean = false) {
    try {
        findNavController().popBackStack(destinationId, inclusive)
    } catch (e: IllegalArgumentException) {
        Log.e("CLEAR_BACKSTACK_ERROR", "Error clearing back stack: ${e.localizedMessage}")
    }
}
fun Fragment.safeNavigate(actionId: Int, currentFragmentId: Int) {
    try{
        if (findNavController().currentDestination?.id == currentFragmentId) {
            findNavController().navigate(
                actionId
            )
        } else {
            Log.d("TAG", "navigateSafe: ")
        }
    }catch (e:Exception){
        Log.d("SAFE_NAV_ERROR", "safeNavigateError:${e.localizedMessage} ")
    }
}
fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
fun Fragment.showToast(string: String) {
    Toast.makeText(this.requireContext(), string, Toast.LENGTH_SHORT).show()
}
fun Fragment.showLongToast(string: String) {
    Toast.makeText(this.requireContext(), string, Toast.LENGTH_LONG).apply {
        cancel() // Cancel any previous toast
        show()
    }
}
fun Fragment.showKeyboard(view: View?) {
    view?.let {
        val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(it, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}
fun Fragment.hideKeyboard(view: View?): Boolean {
    val inputMethodManager =
        view?.context?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as? InputMethodManager
    return inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0) ?: false
}

fun Context.showEmailChooser(supportEmail: String, subject: String,body: String?=null){
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        val chooser = Intent.createChooser(intent, "Send Email")
        if (chooser.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        } else {
            Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show()
        }
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show()
    }
}


fun Fragment.showSettingsDialog() {
    val dialog = Dialog(requireContext())
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.setting_dialogue)

    val width = (requireContext().resources.displayMetrics.widthPixels * 0.90).toInt()
    dialog.setCancelable(false)
    dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

    dialog.findViewById<TextView>(R.id.yes).setOnClickListener {
        openAppSettingsStorage()
        dialog.dismiss()
    }

    dialog.findViewById<ImageView>(R.id.closeBtn).setOnClickListener {
        dialog.dismiss()
    }

    try {
        dialog.show()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

private fun Fragment.openAppSettingsStorage() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", requireContext().packageName, null)
    intent.data = uri
    requireContext().startActivity(intent)
}

fun ViewModel.isInternetAvailable(context: Context): Boolean {
    var result = false
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    connectivityManager?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
        } else {
            return true
        }
    }
    return result
}
