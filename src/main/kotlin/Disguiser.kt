package gg.uhc.anonymous

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.Disguise
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import java.util.*

open class Disguiser(val baseDisguise: Disguise, plugin: Plugin) : PacketAdapter(plugin, PacketType.Play.Server.PLAYER_INFO), Listener {
    constructor(name: String, skin: UUID, plugin: Plugin) : this(PlayerDisguise(WrappedGameProfile(skin, name)), plugin)

    @EventHandler
    open fun on(event: PlayerJoinEvent) = disguisePlayer(event.player)

    /**
     * Intercepts tab list packets sent out of the server and rewrites them
     */
    override fun onPacketSending(event: PacketEvent) {
        event.packet.playerInfoDataLists.write(0, event.packet.playerInfoDataLists.read(0).map { transformPlayerInfoData(it) })
    }

    /**
     * Transforms PlayerInfoData being sent out in packets
     */
    open internal fun transformPlayerInfoData(data: PlayerInfoData) : PlayerInfoData {
        // Get the disguise for the player themselves
        val disguise: Disguise? = DisguiseAPI.getDisguise(plugin.server.getPlayer(data.profile.uuid))

        return when (disguise) {
            // Don't modify tab if the player isn't disguised as a player
            null, !is PlayerDisguise -> data
            // Use the skin and name from the disguise
            else -> PlayerInfoData(data.profile, data.ping, data.gameMode, WrappedChatComponent.fromText(disguise.name))
        }
    }

    /**
     * Undisguises the player first if required
     */
    open fun disguisePlayer(player: Player) {
        if (DisguiseAPI.isDisguised(player)) return

        DisguiseAPI.disguiseEntity(player, baseDisguise)
    }
}