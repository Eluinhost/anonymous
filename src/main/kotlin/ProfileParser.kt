package gg.uhc.anonymous

import com.google.common.io.Resources
import com.google.gson.Gson
import java.net.URL
import java.util.*

private const val URL_FORMAT = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false"

open class ProfileParser(){
    protected val parser = Gson()

    protected open fun getUrlForUuid(uuid: UUID) = String.format(URL_FORMAT, uuid.toString().replace("-", ""))

    open fun fetchForUuuid(uuid: UUID) : ParsedProfile {
        val json = Resources.toString(URL(getUrlForUuid(uuid)), Charsets.UTF_8)

        val profile = parser.fromJson(json, ParsedProfile::class.java)

        if (!profile.isValid()) throw IllegalArgumentException("Invalid response from Mojand server")

        return profile
    }
}

data class ParsedProfile(val id: String = "", val name: String = "", val properties: Array<Property> = arrayOf()) {
    fun isValid() = id.isNotEmpty() && name.isNotEmpty() && properties.all { it.isValid() }
}

data class Property(val name: String = "", val value: String = "", val signature: String = ""){
    fun isValid() = name.isNotEmpty() && value.isNotEmpty() && signature.isNotEmpty()
}