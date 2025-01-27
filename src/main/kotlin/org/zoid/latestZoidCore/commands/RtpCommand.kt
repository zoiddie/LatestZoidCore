package org.zoid.latestZoidCore.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class RtpCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        val biome = when {
            args.isEmpty() -> "DESERT"
            args[0].equals("DESERT", true) -> "DESERT"
            args[0].equals("SNOWY_PLAINS", true) -> "SNOWY_PLAINS"
            args[0].equals("PLAINS", true) -> "PLAINS"
            else -> return false
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "betterrtp player_sudo ${sender.name} $biome")
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> listOf("DESERT", "SNOWY_PLAINS", "PLAINS")
            else -> emptyList()
        }
    }
}