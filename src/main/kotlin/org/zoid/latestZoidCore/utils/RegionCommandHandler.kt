package org.zoid.latestZoidCore.utils

import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.zoid.latestZoidCore.LatestZoidCore

class RegionCommandHandler(private val plugin: LatestZoidCore) : CommandExecutor, TabCompleter, Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player || !sender.hasPermission("zoidcore.region")) return true

        when (args.getOrNull(0)?.lowercase()) {
            "selector" -> {
                Region.setSelector(sender, null, null)
                sender.sendMessage("Region selector enabled. Right-click and left-click to set points.")
                return true
            }
            "create" -> {
                val name = args.getOrNull(1) ?: return false
                val (point1, point2) = Region.getSelector(sender) ?: run {
                    sender.sendMessage("You need to set both points first.")
                    return true
                }

                if (point1 == null || point2 == null) {
                    sender.sendMessage("You need to set both points first.")
                    return true
                }

                Region.addRegion(Region(name, point1, point2))
                Region.clearSelector(sender)
                sender.sendMessage("Region '$name' created successfully.")
                return true
            }
        }

        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> listOf("selector", "create")
            else -> emptyList()
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val selector = Region.getSelector(player) ?: return

        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> event.clickedBlock?.location?.let {
                Region.setSelector(player, selector.first, it)
                player.sendMessage("Second point set at ${it.toVector()}")
            }
            Action.RIGHT_CLICK_BLOCK -> event.clickedBlock?.location?.let {
                Region.setSelector(player, it, selector.second)
                player.sendMessage("First point set at ${it.toVector()}")
            }
            else -> return
        }
    }
}