package gg.uhc.anonymous

import com.comphenix.protocol.ProtocolLibrary
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

const val USERNAME_KEY: String = "username"
const val SKIN_KEY: String = "skin"
const val DISABLE_CHAT_KEY = "disable chat"
const val REWRITE_TAB_COMPLETES_KEY = "rewrite names in tab complete"
const val REWRITE_JOIN_LEAVES_KEY = "rewrite joins and leaves"
const val SKIN_REFRESH_TIME_KEY = "refresh skin minutes"

const val CHAT_BYPASS_PERMISSION = "anonymous.chat.bypass"
const val TAB_COMPLETE_BYPASS_PERMISSION = "anonymous.tabcomplete.bypass"
const val SKIN_BYPASS_PERMISSION = "anonymous.skin.bypass"
const val JOIN_LEAVE_BYPASS_PERMISSION = "anonymous.joinleave.bypass"

/**
 * Entry class created by Bukkit, [onEnable] is called on plugin load
 */
class Entry() : JavaPlugin() {
    fun invalidConfig(message: String) : InvalidConfigurationException {
        val withPrefix = "Invalid configuration: $message";
        logger.severe(withPrefix)
        isEnabled = false
        return InvalidConfigurationException(withPrefix)
    }

    override fun onEnable() {
        config.options().copyDefaults(true)
        saveConfig()

        val name: String = config.getString(USERNAME_KEY) ?: throw invalidConfig("Must supply the key `$USERNAME_KEY`")
        val skin: String = config.getString(SKIN_KEY) ?: throw invalidConfig("Must supply the key `$SKIN_KEY`")

        val uuid: UUID = try {
            UUID.fromString(skin)
        } catch (ex: IllegalArgumentException) {
            throw invalidConfig("Invalid UUID for `$SKIN_KEY`")
        }

        // Disguise controller handles it's own registering/scheduling
        DisguiseController(
            skinUUID = uuid,
            name = name,
            profiles = CachedProfileParser(MojangAPIProfileParser(), File(dataFolder, "skin-cache.yml"), 2),
            plugin = this,
            manager = ProtocolLibrary.getProtocolManager(),
            refreshTime = config.getLong(SKIN_REFRESH_TIME_KEY)
        )

        if (config.getBoolean(DISABLE_CHAT_KEY)) {
            server.pluginManager.registerEvents(DisableChatListener(), this)
        }

        if (config.getBoolean(REWRITE_TAB_COMPLETES_KEY)) {
            server.pluginManager.registerEvents(RewriteTabCompleteListener(this, name), this)
        }

        if (config.getBoolean(REWRITE_JOIN_LEAVES_KEY)) {
            server.pluginManager.registerEvents(JoinLeaveListener(this), this)
        }
    }
}