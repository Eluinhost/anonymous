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
