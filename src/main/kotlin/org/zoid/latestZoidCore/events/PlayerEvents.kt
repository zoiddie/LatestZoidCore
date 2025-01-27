package org.zoid.latestZoidCore.events

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PlayerEvents(private val plugin: JavaPlugin) : Listener {

    private val miniMessage = MiniMessage.miniMessage()

    // Pre-deserialize templates
    private val deathMessageTemplate = miniMessage.deserialize("<gray>☠ %s died")
    private val deathMessageToPlayerTemplate = miniMessage.deserialize("<gray>☠ %s died to %s")
    private val deathMessageExplosionTemplate = miniMessage.deserialize("<gray>☠ %s died to an explosion")
    private val joinMessageTemplate = miniMessage.deserialize("<white>%s <#91E163>joined")
    private val leaveMessageTemplate = miniMessage.deserialize("<white>%s <#E16363>left")

    // Use UUID as key for playerNameCache
    private val playerNameCache = ConcurrentHashMap<UUID, String>()

    // Track online players dynamically
    private val onlinePlayers = Collections.newSetFromMap(ConcurrentHashMap<Player, Boolean>())

    // Lazy initialization of spawnLocation
    private val spawnLocation by lazy {
        Location(Bukkit.getWorld("spawn"), 0.0, 90.0, 0.0)
    }

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val lastDamageCause = player.lastDamageCause
        val deathMessage = when {
            lastDamageCause is EntityDamageByEntityEvent && lastDamageCause.damager is Player -> {
                deathMessageToPlayerTemplate.replaceText {
                    it.match("%s").replacement(playerNameCache[player.uniqueId] ?: player.name)
                }.replaceText {
                    it.match("%s").replacement(playerNameCache[lastDamageCause.damager.uniqueId] ?: lastDamageCause.damager.name)
                }
            }
            lastDamageCause is EntityDamageEvent && lastDamageCause.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION -> {
                deathMessageExplosionTemplate.replaceText {
                    it.match("%s").replacement(playerNameCache[player.uniqueId] ?: player.name)
                }
            }
            else -> deathMessageTemplate.replaceText {
                it.match("%s").replacement(playerNameCache[player.uniqueId] ?: player.name)
            }
        }
        event.deathMessage(deathMessage)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(null)
        val player = event.player
        playerNameCache[player.uniqueId] = player.name
        onlinePlayers.add(player)

        val joinMessage = joinMessageTemplate.replaceText {
            it.match("%s").replacement(player.name)
        }
        onlinePlayers.forEach { it.sendMessage(joinMessage) }

        if (player.location != spawnLocation) {
            player.teleportAsync(spawnLocation)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage(null)
        val player = event.player
        playerNameCache.remove(player.uniqueId)
        onlinePlayers.remove(player)

        val leaveMessage = leaveMessageTemplate.replaceText {
            it.match("%s").replacement(player.name)
        }
        onlinePlayers.forEach { it.sendMessage(leaveMessage) }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        event.respawnLocation = spawnLocation
    }
}