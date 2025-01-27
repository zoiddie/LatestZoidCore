package org.zoid.latestZoidCore.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Chat(private val plugin: JavaPlugin) : Listener {

    private val miniMessage = MiniMessage.miniMessage()
    private val luckPerms: LuckPerms = LuckPermsProvider.get()
    private val prefixCache = ConcurrentHashMap<UUID, String>()
    private val onlinePlayers = Collections.newSetFromMap(ConcurrentHashMap<Player, Boolean>())

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        event.isCancelled = true
        val player = event.player
        val playerName = player.name
        val message = event.message
        val prefix = prefixCache.computeIfAbsent(player.uniqueId) {
            luckPerms.userManager.getUser(player.uniqueId)?.cachedData?.metaData?.prefix ?: ""
        }

        val messageFormat = StringBuilder(prefix.length + playerName.length + message.length + 15)
            .append(prefix)
            .append(playerName)
            .append(" <gray>-> <white>")
            .append(message)
            .toString()

        val formattedMessage = miniMessage.deserialize(messageFormat)

        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            onlinePlayers.forEach { it.sendMessage(formattedMessage) }
        })
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        onlinePlayers.add(event.player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        onlinePlayers.remove(event.player)
    }
}