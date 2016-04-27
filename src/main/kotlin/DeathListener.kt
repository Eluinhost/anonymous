package gg.uhc.anonymous

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.Plugin

/**
 * A Bukkit event listener that listens for player deaths and rewrites all online player names found within. Players
 * with [DEATH_BYPASS_PERMISSION] will see the original message instead
 *
 * @param plugin used to send messages to online players
 */
class DeathListener(protected val plugin: Plugin, protected val replacement: String) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST) fun on(event: PlayerDeathEvent) {
        val original = event.deathMessage
        event.deathMessage = ""

        // replace each online player name in the string with the replacement
        var modified = plugin.server.onlinePlayers
                .map { it.name }
                .fold(event.deathMessage, { accumulator, name ->
                    accumulator.replace(name, replacement)
                })

        plugin.server.onlinePlayers.forEach {
            it.sendMessage(when {
                it.hasPermission(DEATH_BYPASS_PERMISSION) -> original
                else -> modified
            })
        }
    }
}
