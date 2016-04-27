package gg.uhc.anonymous

import com.google.common.io.Resources
import com.google.gson.Gson
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

private const val URL_FORMAT = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false"
private val CACHE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

/**
 * Used to fetch profiles from a source by their UUID
 */
interface ProfileParser {
    /**
     * Fetch for the given UUID, any returned profile should have [ParsedProfile.isValid] true.
     *
     * @param uuid account uuid to fetch profile for
     * @throws java.io.IOException on potential IO problems
     */
    fun getForUuid(uuid: UUID) : ParsedProfile
}

/**
 * Fetches profiles from the Mojang API. This API is rate limited so care should be taken when using this.
 */
open class MojangAPIProfileParser() : ProfileParser {
    protected val parser = Gson()

    protected open fun getUrlForUuid(uuid: UUID) = String.format(URL_FORMAT, uuid.toString().replace("-", ""))

    override fun getForUuid(uuid: UUID) : ParsedProfile = fromJson(Resources.toString(URL(getUrlForUuid(uuid)), Charsets.UTF_8))

    protected open fun fromJson(json: String) : ParsedProfile {
        val profile = parser.fromJson(json, ParsedProfile::class.java)

        if (!profile.isValid()) throw IllegalArgumentException("Invalid response from Mojang server")

        return profile
    }
}

/**
 * Caches for the given [ProfileParser] to the given [File].
 *
 * @param parser when cache doesn't have an entry or has timed out, delegates to this parser
 * @param cacheFile the file to write the profiles to as a backup
 * @param timeoutMins how long to wait before attempting to fetch a skin again
 */
open class CachedProfileParser(protected val parser: ProfileParser, protected val cacheFile: File, protected val timeoutMins: Int) : ProfileParser {
    protected val cacheConfig = YamlConfiguration()
    protected val cache = mutableMapOf<UUID, Pair<Calendar, ParsedProfile>>()
    init {
        if (!cacheFile.exists()) {
            cacheFile.createNewFile()
        }
        cacheConfig.load(cacheFile)

        // restore all from the config file into the live cache map
        cache.putAll(
            cacheConfig
                .getKeys(false)
                .map {
                    val section = cacheConfig.getConfigurationSection(it)
                    val date = Calendar.getInstance()
                    date.time = CACHE_DATE_FORMAT.parse(section.getString("fetched"))

                    Pair(
                        UUID.fromString(it),
                        Pair(date, ParsedProfile.fromYaml(section.getConfigurationSection("data")))
                    )
                }
        )
    }

    /**
     * Fetch for the given UUID, any returned profile should have [ParsedProfile.isValid] true.
     *
     * If a [ParsedProfile] for [uuid] is still within [timeoutMins] it will be returned immediately, otherwise it is
     * fetched and cached before returning. If the [ProfileParser] fails to fetch a valid [ParsedProfile] then the last
     * cached version is used instead (if none exists the exception will be re-thrown)
     *
     * @param uuid account uuid to fetch profile for
     * @throws java.io.IOException on potential IO problems with no fallback cache
     */
    override fun getForUuid(uuid: UUID) : ParsedProfile {
        val fromCache = cache[uuid]
        val timeoutBreakpoint = Calendar.getInstance()
        timeoutBreakpoint.add(Calendar.MINUTE, -timeoutMins)

        if (fromCache != null && fromCache.first.after(timeoutBreakpoint)) {
            return fromCache.second
        }
        return try {
            val fetched = parser.getForUuid(uuid);

            // store in caches for later
            val now = Calendar.getInstance()
            cache[uuid] = Pair(now, fetched)
            cacheConfig.set(uuid.toString(), mapOf(
                Pair("fetched", CACHE_DATE_FORMAT.format(now.time)),
                Pair("data", fetched.toYaml())
            ))
            cacheConfig.save(cacheFile)

            fetched
        } catch (ex: Throwable) {
            // Rethrow the exception if we didn't have a cache to fall back to
            fromCache?.second ?: throw ex
        }
    }
}

data class ParsedProfile(val id: String = "", val name: String = "", val properties: Array<Property> = arrayOf()) {
    fun isValid() = id.isNotEmpty() && name.isNotEmpty() && properties.all { it.isValid() }

    fun toYaml() : ConfigurationSection {
        val section = MemoryConfiguration()

        section.set("id", id)
        section.set("name", name)

        val propertiesSection = section.createSection("properties")
        properties.forEach { propertiesSection.set(it.name, it.toYaml()) }

        return section
    }

    companion object Factory {
        fun fromYaml(section: ConfigurationSection) : ParsedProfile {
            val propertiesSection = section.getConfigurationSection("properties")

            return ParsedProfile(
                id = section.getString("id"),
                name = section.getString("name"),
                properties = propertiesSection
                            .getKeys(false)
                            .map { propertiesSection.getConfigurationSection(it) }
                            .map { Property.fromYaml(it) }
                            .toTypedArray()
            )
        }
    }
}

data class Property(val name: String = "", val value: String = "", val signature: String = ""){
    fun isValid() = name.isNotEmpty() && value.isNotEmpty() && signature.isNotEmpty()

    fun toYaml() : ConfigurationSection {
        val section = MemoryConfiguration()

        section.set("name", name)
        section.set("value", value)
        section.set("signature", signature)

        return section
    }

    companion object Factory {
        fun fromYaml(section: ConfigurationSection) = Property(
            name = section.getString("name"),
            value = section.getString("value"),
            signature = section.getString("signature")
        )
    }
}