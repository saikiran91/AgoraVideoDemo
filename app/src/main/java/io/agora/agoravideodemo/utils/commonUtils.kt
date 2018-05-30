package io.agora.agoravideodemo.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.i18n.phonenumbers.PhoneNumberUtil


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