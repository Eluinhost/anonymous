package gg.uhc.anonymous

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerChatTabCompleteEvent
import org.bukkit.plugin.Plugin

/**
 * A bukkit event listener that cancels chat messages.
 *
 * Players with [CHAT_BYPASS_PERMISSION] will not be affected
 */
class DisableChatListener : Listener {
    @EventHandler fun on(event: AsyncPlayerChatEvent) {
        if (!event.player.hasPermission(CHAT_BYPASS_PERMISSION)) {
            event.isCancelled = true
        }
    }
}

/**
 * A bukkit event listener that changes all player names in any tab complete to just a single instance of the param
 * `replacement`.
 *
 * Players with [TAB_COMPLETE_BYPASS_PERMISSION] will not be affected
 *
 * @param plugin used to get the list of online players
 * @param replacement name to replace with
 */
class RewriteTabCompleteListener(protected val plugin: Plugin, protected val replacement: String) : Listener {
    @EventHandler(priority = EventPriority.HIGH) fun on(event: PlayerChatTabCompleteEvent) {
        if (!event.player.hasPermission(TAB_COMPLETE_BYPASS_PERMISSION)) {
            if (event.tabCompletions.removeAll(plugin.server.onlinePlayers.map { it.name })) {
                event.tabCompletions.add(replacement)
            }
        }
    }
}