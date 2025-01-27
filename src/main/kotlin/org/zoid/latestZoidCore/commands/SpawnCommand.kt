package org.zoid.latestZoidCore.commands

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class SpawnCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            val spawnLocation = Location(Bukkit.getWorld("spawn"), 0.0, 90.0, 0.0)
            val plugin = Bukkit.getPluginManager().getPlugin("LatestZoidCore") ?: return true
            object : BukkitRunnable() {
                override fun run() {
                    sender.teleport(spawnLocation)
                }
            }.runTask(plugin)
        }
        return true
    }
}