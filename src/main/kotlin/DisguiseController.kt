package gg.uhc.anonymous

import com.comphenix.executors.BukkitExecutors
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
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Handles disguising players on join by changing their skin/tab name/display name
 *
 * @param skinUUID uuid of the skin to display
 * @param name name of the player to show in tab/as display name
 * @param profiles used to fetch skin data to show to clients
 * @param plugin used to register events/packet listeners and for logging
 * @param refreshTime amount of time between trying to refresh the skin data in minutes
 * @param manager used to register packet listeners for modifying skins
 */
open class DisguiseController(
    protected val skinUUID: UUID,
    protected val name: String,
    protected val profiles: ProfileParser,
    protected val plugin: Plugin,
    protected val refreshTime: Long,
    manager: ProtocolManager
) : Listener {
    protected val wrappedName = WrappedChatComponent.fromText(name)
    protected var texture: WrappedSignedProperty? = null

    protected val asyncExecutor = BukkitExecutors.newAsynchronous(plugin)

    /**
     * A listener that calls [onTabListPacket] when receiving a [tab list packet][PacketType.Play.Server.PLAYER_INFO]
     * and the player does not have the [bypass permission][SKIN_BYPASS_PERMISSION]. This means anyone with the
     * permission will *see* valid skins, not that people will see the player.
     */
    protected val tabListPlayersListener = object : PacketAdapter(plugin, PacketType.Play.Server.PLAYER_INFO) {
        override fun onPacketSending(event: PacketEvent) {
            if (!event.player.hasPermission(SKIN_BYPASS_PERMISSION)) {
                onTabListPacket(event.packet)
            }
        }
    }

    /**
     * Changes the textures and name for the player/s on the tab list for the receiver
     *
     * @param packet raw packet to be modified
     */
    protected open fun onTabListPacket(packet: PacketContainer) = packet.playerInfoDataLists.write(
        0,
        packet.playerInfoDataLists.read(0).map {
            val newProfile = WrappedGameProfile(it.profile.uuid, name)
            newProfile.properties.putAll(it.profile.properties)
            newProfile.properties.replaceValues("textures", if (texture == null) listOf() else listOf(texture))
            PlayerInfoData(newProfile, it.ping, it.gameMode, wrappedName)
        }
    )

    @EventHandler(priority = EventPriority.LOW) fun on(event: PlayerLoginEvent) = onPlayerLogin(event.player)

    /**
     * Called whenever a player logs in, simply modifies their display name so that other plugins can use it
     *
     * @param player the player that has just logged in
     */
    protected open fun onPlayerLogin(player: Player) {
        player.displayName = name
    }

    /**
     * Attempts to update the [stored skin textures][texture] from the [provided profile fetcher][profiles]. Logs
     * information via [plugin] on success/failure.
     *
     * *NOTE: do not call this on the main thread as [profiles] can be working over the network*
     */
    protected open fun updateSkin() {
        plugin.logger.info("Starting update of skin texture")

        try {
            val profile = profiles.getForUuid(skinUUID)
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
        // add listener for rewriting tab packets
        manager.addPacketListener(tabListPlayersListener)

        // add listener for log in events
        plugin.server.pluginManager.registerEvents(this, plugin)

        // force 'login event' for each player that is already online (/reload)
        // we don't rebuild the tab list so these players will need to reload
        // to see the new skin/names
        plugin.server.onlinePlayers.forEach { onPlayerLogin(it) }

        // start updating the stored texture on a schedule, we run async because network could be involved
        asyncExecutor.scheduleAtFixedRate({ updateSkin() }, 0, refreshTime, TimeUnit.MINUTES)
    }
}
