package io.agora.agoravideodemo.model

import com.chibatching.kotpref.KotprefModel

/**
 * Created by saiki on 01-06-2018.
 **/
/**
map.put("userId", user.getUid());
map.put("name", getPreferredName());
map.put("phone", getPhoneNumber());
map.put("countryCode", countryCodePicker.getDefaultCountryCodeAsInt());
map.put("lastUpdated", System.currentTimeMillis());
map.put("isVerified", true);
 */
data class FireUser(val userId: String = "", val name: String = "",
                    val phone: String = "0", val countryCode: Int = 0,
                    val lastUpdated: Long = 0L, val verified: Boolean = false)

object UserInfo : KotprefModel() {
    var userId by stringPref()
    var name by stringPref()
    var phone by stringPref()
    var countryCode by intPref()
}