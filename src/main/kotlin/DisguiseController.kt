package gg.uhc.anonymous

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import com.comphenix.protocol.wrappers.WrappedSignedProperty
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import java.util.*

const val BYPASS_PERMISSION = "uhc.anonymous.bypass"

open class DisguiseController(
    protected val skinUUID: UUID,
    protected val name: String,
    protected val profileParser: ProfileParser,
    protected val plugin: Plugin,
    manager: ProtocolManager
) : Listener {
    protected val wrappedName = WrappedChatComponent.fromText(name)
    protected var texture: WrappedSignedProperty? = null

    protected val tabListPlayersListener = object : PacketAdapter(plugin, PacketType.Play.Server.PLAYER_INFO) {
        override fun onPacketSending(event: PacketEvent) {
            if (!event.player.hasPermission(BYPASS_PERMISSION)) {
                onTabListPacket(event.packet)
            }
        }
    }

    protected open fun onTabListPacket(packet: PacketContainer) = packet.playerInfoDataLists.write(
        0,
        packet.playerInfoDataLists.read(0).map {
            val newProfile = WrappedGameProfile(it.profile.uuid, name)
            newProfile.properties.putAll(it.profile.properties)
            newProfile.properties.replaceValues("textures", if (texture == null) listOf() else listOf(texture))
            PlayerInfoData(newProfile, it.ping, it.gameMode, wrappedName)
        }
    )

    @EventHandler fun on(event: PlayerJoinEvent) = setupPlayer(event.player)

    protected open fun setupPlayer(player: Player) {
        player.displayName = name
    }

    protected open fun updateSkin() {
        plugin.logger.info("Starting update of skin texture")

        try {
            val profile = profileParser.fetchForUuuid(skinUUID)
            val x = profile.properties.find { it.name == "textures" }
                    ?: throw IllegalArgumentException("Skin data missing textures property")

            val newTexture = WrappedSignedProperty.fromValues(x.name, x.value, x.signature)

            if (!newTexture.equals(texture)) {
                texture = newTexture
                plugin.logger.info("Updated skin texture")
            } else {
                plugin.logger.info("Skin texture already up to date")
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            plugin.logger.warning("Failed to fetch the skin data for the uuid $skinUUID")
        }
    }

    init {
        manager.addPacketListener(tabListPlayersListener)
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.server.onlinePlayers.forEach { setupPlayer(it) }
        plugin.server.scheduler.runTaskTimerAsynchronously(plugin, { updateSkin() }, 0, 60 * 20 * 5)
    }
}
