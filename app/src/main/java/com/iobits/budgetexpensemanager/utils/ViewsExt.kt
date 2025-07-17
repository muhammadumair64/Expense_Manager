package com.iobits.budgetexpensemanager.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs


fun Fragment.onBackPressDispatcherOverride(func: () -> Unit) {
    // This callback will only be called when MyFragment is at least Started.
    val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            func.invoke()
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
}
fun TextView.applyTextShader(colors: List<Int> =listOf(Color.parseColor("#3363F2"), Color.parseColor("#FF48E0"))) {
    val width = paint?.measureText(text.toString())
    val textShader: Shader = LinearGradient(
        0f, 0f, width ?: 0f, textSize, colors.toIntArray(), null, Shader.TileMode.REPEAT
    )

    paint.setShader(textShader)
}
@RequiresApi(Build.VERSION_CODES.O)
fun String.getDayOfWeekOrToday(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val date = LocalDate.parse(this, formatter)
    val today = LocalDate.now()

    return when {
        date == today -> "Today"
        else -> {
            when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "Monday"
                DayOfWeek.TUESDAY -> "Tuesday"
                DayOfWeek.WEDNESDAY -> "Wednesday"
                DayOfWeek.THURSDAY -> "Thursday"
                DayOfWeek.FRIDAY -> "Friday"
                DayOfWeek.SATURDAY -> "Saturday"
                DayOfWeek.SUNDAY -> "Sunday"
                else -> "-----"
            }
        }
    }
}
fun String.getHoursFromTimeString(): Int? {
    return try {
        val hourIndex = this.indexOf(' ')
        val timeSubstring = if (hourIndex != -1) {
            this.substring(hourIndex + 1).trim()
        } else {
            this.trim()
        }
        val hour = timeSubstring.substringBefore(':').toInt()
        if (hour in 0..23) {
            hour
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.getHoursAndMinutesFromDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val dateTime = LocalDateTime.parse(this, formatter)
    return "${dateTime.hour}:${dateTime.minute}"
}
@RequiresApi(Build.VERSION_CODES.O)
fun String.getDayOfWeekFromMMDDFormat(): String {
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val outputFormatter = DateTimeFormatter.ofPattern("MM/dd")
    val date = LocalDate.parse(this, inputFormatter)
    return date.format(outputFormatter)
}

fun String.formatDateTimeMain(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val outputFormat = SimpleDateFormat("HH:mm EEE, dd MMM", Locale.getDefault())

    val date = inputFormat.parse(this)
    return outputFormat.format(date ?: Date())
}
fun String.textToBitmap(
    textSize: Float = 50f, textColor: Int = Color.BLACK, textTypeface: Typeface? = Typeface.DEFAULT
): Bitmap {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = textSize
        color = textColor
        style = Paint.Style.FILL
        textTypeface?.let { typeface = it }
    }

    val textBounds = Rect()
    paint.getTextBounds(this, 0, this.length, textBounds)
    val width = textBounds.width() + 2 * abs(textBounds.left)
    val height = textBounds.height() + 2 * abs(textBounds.top)

    val bitmap = Bitmap.createBitmap(
        width, height, Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.TRANSPARENT)
    canvas.drawText(
        this,
        -textBounds.left.toFloat(),
        -textBounds.top.toFloat() + textSize, // Adjust for baseline
        paint
    )

    return bitmap
}



fun Long.formatToCustomString(): String {
    val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
    return sdf.format(Date(this))
}

fun Long.formatToCustomStringInCharts(): String {
    val sdf = SimpleDateFormat("dd MMM", Locale.US)
    return sdf.format(Date(this))
}

fun Any.logd(message: String, tag: String? = "RoomTemperatureApp") {
    Log.d(tag, message)
}

fun Long.formatFileSizeToMB(): String {
    val fileSizeInMB = this.toDouble() / (1024 * 1024)
    return String.format("%.2f MB", fileSizeInMB)
}
fun Date.toFormattedString(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    return dateFormat.format(this)
}

fun EditText.onSearch(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

//fun Fragment.disableMultipleClicking(view: View, delay: Long = 750) {
//    view.isEnabled = false
//    this.lifecycleScope.launch {
//        delay(delay)
//        view.isEnabled = true
//    }
//}

fun Resources.decodeSampledBitmapFromResource(
    resId: Int, reqWidth: Int, reqHeight: Int
): Bitmap {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        BitmapFactory.decodeResource(this@decodeSampledBitmapFromResource, resId, this)
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        inJustDecodeBounds = false
    }
    return BitmapFactory.decodeResource(this@decodeSampledBitmapFromResource, resId, options)
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun View.setSampledBitmapAsBackground(resources: Resources, resId: Int) {
    val scaledBitmap = resources.decodeSampledBitmapFromResource(resId, 100, 100)
    this.background = BitmapDrawable(resources, scaledBitmap)
}


/**
 * for sending bundle along with navigation
 * */
fun Fragment.navigateSafe(
    actionId: Int, currentDestinationFragmentId: Int, bundle: Bundle? = null
) {
    if (findNavController().currentDestination?.id == currentDestinationFragmentId) {
        findNavController().navigate(
            actionId, bundle
        )
    } else {
        Log.d("TAG", "navigateSafe: ")
    }
}

fun Fragment.printLogs(msg: Any, tag: String? = null) {
    val fragmentName = tag ?: this.javaClass.simpleName
    Log.d(fragmentName, "$fragmentName: $msg")
}

fun Any.printLogs(msg: Any, tag: String? = null) {
    Log.d("SignatureMakerApp", "SignatureMakerApp: $msg")
}
fun View.animateView() {
    val scaleX: ObjectAnimator = ObjectAnimator.ofFloat(this, "scaleX", 0.9f, 1.1f)
    val scaleY: ObjectAnimator = ObjectAnimator.ofFloat(this, "scaleY", 0.9f, 1.1f)
    scaleX.repeatCount = ObjectAnimator.INFINITE
    scaleX.repeatMode = ObjectAnimator.REVERSE
    scaleY.repeatCount = ObjectAnimator.INFINITE
    scaleY.repeatMode = ObjectAnimator.REVERSE
    val scaleAnim = AnimatorSet()
    scaleAnim.duration = 1000
    scaleAnim.play(scaleX).with(scaleY)
    scaleAnim.start()

}

fun View.animateSlightly() {
    val scaleX: ObjectAnimator = ObjectAnimator.ofFloat(this, "scaleX", 0.95f, 1.05f)
    val scaleY: ObjectAnimator = ObjectAnimator.ofFloat(this, "scaleY", 0.95f, 1.05f)
    scaleX.repeatCount = ObjectAnimator.INFINITE
    scaleX.repeatMode = ObjectAnimator.REVERSE
    scaleY.repeatCount = ObjectAnimator.INFINITE
    scaleY.repeatMode = ObjectAnimator.REVERSE
    val scaleAnim = AnimatorSet()
    scaleAnim.duration = 1000
    scaleAnim.play(scaleX).with(scaleY)
    scaleAnim.start()
}


