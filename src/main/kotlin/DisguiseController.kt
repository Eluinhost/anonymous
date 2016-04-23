package gg.uhc.anonymous

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import java.util.*

const val BYPASS_PERMISSION = "uhc.anonymous.bypass"
private const val ENTITY_CUSTOM_NAME = 2;

open class DisguiseController(val skinUUID: UUID, val name: String, plugin: Plugin, manager: ProtocolManager) : Listener {
    protected val profile = WrappedGameProfile(skinUUID, name)
    protected val wrappedName = WrappedChatComponent.fromText(name);

    protected val spawnPlayerListener = object : PacketAdapter(plugin, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
        override fun onPacketSending(event: PacketEvent) {
            if (!event.player.hasPermission(BYPASS_PERMISSION)) {
                onSpawnPlayerPacket(event.packet)
            }
        }
    }

    protected val tabListPlayersListener = object : PacketAdapter(plugin, PacketType.Play.Server.PLAYER_INFO) {
        override fun onPacketSending(event: PacketEvent) {
            if (!event.player.hasPermission(BYPASS_PERMISSION)) {
                onTabListPacket(event.packet)
            }
        }
    }

    protected open fun onSpawnPlayerPacket(packet: PacketContainer) {
//        packet.getSpecificModifier(UUID::class.java).write(0, profile.uuid)
        // TODO change custom display name
    }

    protected open fun onTabListPacket(packet: PacketContainer) = packet.playerInfoDataLists.write(
        0,
        packet.playerInfoDataLists.read(0).map {
            it.profile.properties.replaceValues("textures", profile.properties.get("textures"))
            PlayerInfoData(it.profile, it.ping, it.gameMode, wrappedName)
        }
    )

    @EventHandler fun on(event: PlayerJoinEvent) = setupPlayer(event.player)

    protected open fun setupPlayer(player: Player) {
        player.displayName = profile.name
    }

    init {
        manager.addPacketListener(spawnPlayerListener)
        manager.addPacketListener(tabListPlayersListener)
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.server.onlinePlayers.forEach { setupPlayer(it) }
    }
}
