package org.zoid.latestZoidCore.tpa

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TPACommand(private val plugin: Plugin) : CommandExecutor {
    private val miniMessage = MiniMessage.miniMessage()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(miniMessage.deserialize("<red>Only players can use this command."))
            return true
        }

        when (command.name.lowercase()) {
            "tpa" -> {
                if (args.isEmpty()) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /tpa <player>"))
                    return true
                }
                val target = plugin.server.getPlayer(args[0]) ?: run {
                    sender.sendMessage(miniMessage.deserialize("<red>Player not found."))
                    return true
                }
                TPAManager.sendTPARequest(sender, target, plugin)
            }
            "tpahere" -> {
                if (args.isEmpty()) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /tpahere <player>"))
                    return true
                }
                val target = plugin.server.getPlayer(args[0]) ?: run {
                    sender.sendMessage(miniMessage.deserialize("<red>Player not found."))
                    return true
                }
                TPAManager.sendTPAHereRequest(sender, target, plugin)
            }
            "tpaccept" -> {
                val senderName = if (args.isNotEmpty()) args[0] else null
                TPAManager.acceptTPARequest(sender, senderName, plugin)
            }
            "tpdeny" -> {
                if (args.isEmpty()) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /tpdeny <player>"))
                    return true
                }
                val senderName = args[0]
                TPAManager.denyTPARequest(sender, senderName)
            }
            "tpacancel" -> TPAManager.cancelTPARequest(sender)
            "back" -> TPAManager.teleportToDeathLocation(sender)
            else -> sender.sendMessage(miniMessage.deserialize("<red>Unknown command."))
        }
        return true
    }
}