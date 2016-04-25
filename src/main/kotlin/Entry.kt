package gg.uhc.anonymous

import com.comphenix.protocol.ProtocolLibrary
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

class Entry() : JavaPlugin() {
    protected var disguiser: DisguiseController? = null

    fun invalidConfig(message: String) {
        logger.severe("Invalid configuration: $message")
        isEnabled = false
    }

    override fun onEnable() {
        config.options().copyDefaults(true)
        saveConfig()

        val name: String? = config.getString(USERNAME_KEY)
        val skin: String? = config.getString(SKIN_KEY)

        if (name == null || skin == null) {
            return invalidConfig("Must supply both a `$USERNAME_KEY` and a `$SKIN_KEY` key")
        }

        val uuid: UUID
        try {
            uuid = UUID.fromString(skin)
        } catch (ex: IllegalArgumentException) {
            return invalidConfig("Invalid UUID for `$SKIN_KEY`")
        }

        disguiser = DisguiseController(
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