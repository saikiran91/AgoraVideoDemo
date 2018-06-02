package io.agora.agoravideodemo.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.*


/**
 * Created by saiki on 30-05-2018.
 **/
fun deleteCountry(phone: String, countryCode: String): String {
    val phoneInstance = PhoneNumberUtil.getInstance()
    try {
        val phoneNumber = phoneInstance.parse(phone, countryCode)
        return phoneNumber?.nationalNumber?.toString() ?: phone
    } catch (_: Exception) {
    }
    return phone
}

fun View.hideKeyboard() {
    val input = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    input.hideSoftInputFromWindow(this.applicationWindowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun View.showKeyboard() {
    val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.show(visible: Boolean = true) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.hide() = show(visible = false)

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun getWelcomeMessage(): String {
    val c = Calendar.getInstance()
    val timeOfDay = c.get(Calendar.HOUR_OF_DAY)
    return when (timeOfDay) {
        in 0..11 -> "Good Morning"
        in 12..15 -> "Good Afternoon"
        in 16..20 -> "Good Evening"
        in 21..23 -> "Good Evening. Time to sleep :)"
        else -> "Hello"
    }
}

fun String.decodeFromBase64(): String {
    return android.util.Base64.decode(this, android.util.Base64.DEFAULT).toString(charset("UTF-8"))
}

fun String.encodeToBase64(): String {
    return android.util.Base64.encodeToString(this.toByteArray(charset("UTF-8")), android.util.Base64.DEFAULT)
}

fun Gson.toBase64Encode(obj: Any) = toJson(obj).encodeToBase64()
