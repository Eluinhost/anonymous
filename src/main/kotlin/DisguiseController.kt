package gg.uhc.anonymous

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import org.bukkit.plugin.Plugin
import java.util.*

const val BYPASS_PERMISSION = "uhc.anonymous.bypass"

open class DisguiseController(val skinUUID: UUID, val name: String, plugin: Plugin, manager: ProtocolManager) {
    protected val profile = WrappedGameProfile(skinUUID, name)
    protected val wrappedName = WrappedChatComponent.fromText(name);


    protected val spawnPlayerListener = object : PacketAdapter(plugin, PacketType.Play.Server.PLAYER_INFO) {
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

    }

    protected open fun onTabListPacket(packet: PacketContainer) = packet.playerInfoDataLists.write(
        0,
        packet.playerInfoDataLists.read(0).map {
            it.profile.properties.replaceValues("textures", profile.properties.get("textures"))
            PlayerInfoData(it.profile, it.ping, it.gameMode, wrappedName)
        }
    )

    init {
        manager.addPacketListener(spawnPlayerListener)
        manager.addPacketListener(tabListPlayersListener)
    }
}
