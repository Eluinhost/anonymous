package gg.uhc.anonymous

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TranslatableComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

private const val JOIN_TRANSLATION_STRING = "multiplayer.player.joined"
private const val QUIT_TRANSLATION_STRING = "multiplayer.player.left"

/**
 * A Bukkit event listener that listens on leave/join events and cancels the messages. It then sends a reformatted
 * message to each player with the player's display name instead. Uses Minecraft translatable messages to send the
 * message to clients for their own language. Players with the [JOIN_LEAVE_BYPASS_PERMISSION] will see the original
 * messages instead.
 *
 * @param plugin used to send messages to online players
 */
class JoinLeaveListener(protected val plugin: Plugin) : Listener {
    @EventHandler fun on(event: PlayerJoinEvent) {
        event.joinMessage = ""

        val actual = TranslatableComponent(JOIN_TRANSLATION_STRING, event.player.name)
        actual.color = ChatColor.YELLOW
        val modified = TranslatableComponent(JOIN_TRANSLATION_STRING, event.player.displayName)
        modified.color = ChatColor.YELLOW

        plugin.server.onlinePlayers.forEach {
            it.spigot().sendMessage(when {
                it.hasPermission(JOIN_LEAVE_BYPASS_PERMISSION) -> actual
                else -> modified
            })
        }
    }

    @EventHandler fun on(event: PlayerQuitEvent) {
        event.quitMessage = ""

        val actual = TranslatableComponent(QUIT_TRANSLATION_STRING, event.player.name)
        actual.color = ChatColor.YELLOW
        val modified = TranslatableComponent(QUIT_TRANSLATION_STRING, event.player.displayName)
        modified.color = ChatColor.YELLOW

        plugin.server.onlinePlayers.forEach {
            it.spigot().sendMessage(when {
                it.hasPermission(JOIN_LEAVE_BYPASS_PERMISSION) -> actual
                else -> modified
            })
        }
    }
}
