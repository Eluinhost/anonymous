package gg.uhc.anonymous

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerChatTabCompleteEvent
import org.bukkit.plugin.Plugin

class DisableChatListener : Listener {
    @EventHandler fun on(event: AsyncPlayerChatEvent) {
        if (!event.player.hasPermission(CHAT_BYPASS_PERMISSION)) {
            event.isCancelled = true
        }
    }
}

class RewriteTabCompleteListener(protected val plugin: Plugin, protected val replacement: String) : Listener {
    @EventHandler(priority = EventPriority.HIGH) fun on(event: PlayerChatTabCompleteEvent) {
        if (!event.player.hasPermission(REWRITE_TAB_COMPLETES_KEY)) {
            if (event.tabCompletions.removeAll(plugin.server.onlinePlayers.map { it.name })) {
                event.tabCompletions.add(replacement)
            }
        }
    }
}