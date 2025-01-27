package org.zoid.latestZoidCore.utils

import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.WorldCreator
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.plugin.java.JavaPlugin

object World {
    fun loadWorld(worldName: String) {
        var world = Bukkit.getWorld(worldName) ?: Bukkit.createWorld(WorldCreator(worldName))
        world?.apply {
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            setGameRule(GameRule.DO_MOB_SPAWNING, false)
            setGameRule(GameRule.DO_MOB_LOOT, false)
            println("World '$worldName' loaded and game rules applied successfully.")
        } ?: println("Failed to load world '$worldName'.")
    }
}

class IceHandler : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.block.type == Material.ICE) {
            event.isCancelled = true
            event.block.type = Material.AIR
        }
    }

    @EventHandler
    fun onBlockExplode(event: BlockExplodeEvent) {
        event.blockList().removeIf { it.type == Material.ICE }
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        event.blockList().removeIf { it.type == Material.ICE }
    }
}