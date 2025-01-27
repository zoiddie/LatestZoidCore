package org.zoid.latestZoidCore.utils

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class Arena(private val plugin: JavaPlugin) {

    private val commands = listOf(
        "/world spawn",
        "/pos1 0,90,0",
        "/pos2 0,90,0",
        "/schem load spawnflat.schem",
        "/paste",
        "say <> Arena has reset successfully"
    )

    fun start() {
        val delay = 5 * 60 * 20L
        val period = 5 * 60 * 20L

        object : BukkitRunnable() {
            override fun run() {
                runCommands()
            }
        }.runTaskTimer(plugin, delay, period)
    }

    private fun runCommands() {
        var delay = 0L
        for (command in commands) {
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
                } catch (e: Exception) {
                    plugin.logger.severe("Failed to execute command: $command")
                    e.printStackTrace()
                }
            }, delay)
            delay += 20L
        }
    }
}