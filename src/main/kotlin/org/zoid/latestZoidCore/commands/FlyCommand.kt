package org.zoid.latestZoidCore.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FlyCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (args.isNotEmpty()) {
                try {
                    val speed = args[0].toFloat()
                    if (speed < 0.1) {
                        sender.sendMessage("Fly speed must be at least 0.1")
                        return true
                    }
                    sender.flySpeed = speed / 10.0f
                    sender.sendMessage("Fly speed set to $speed")
                } catch (e: NumberFormatException) {
                    sender.sendMessage("Invalid number format. Please enter a valid number.")
                }
            } else {
                sender.sendMessage("Usage: /flyspeed <speed>")
            }
        } else {
            sender.sendMessage("This command can only be run by a player.")
        }
        return true
    }
}