package gg.uhc.anonymous

import com.comphenix.protocol.ProtocolLibrary
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

const val USERNAME_KEY: String = "username"
const val SKIN_KEY: String = "skin"
const val DISABLE_CHAT_KEY = "disable chat"

const val CHAT_BYPASS_PERMISSION = "anonymous.chat.bypass"
const val SKIN_BYPASS_PERMISSION = "anonymous.skin.bypass"

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
            profileParser = ProfileParser(),
            plugin = this,
            manager = ProtocolLibrary.getProtocolManager()
        )

        // Disable chat if required
        if (config.getBoolean(DISABLE_CHAT_KEY)) {
            server.pluginManager.registerEvents(object: Listener {
                @EventHandler(priority = EventPriority.HIGH)
                fun on(event: AsyncPlayerChatEvent) {
                    if (event.player.hasPermission(CHAT_BYPASS_PERMISSION).not()) {
                        event.isCancelled = true
                    }
                }
            }, this);
        }
    }
}