package org.zoid.latestZoidCore.tpa

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object TPAManager {
    private val miniMessage = MiniMessage.miniMessage()
    private val tpaRequests = HashMap<UUID, UUID>()
    private val tpaHereRequests = HashMap<UUID, UUID>()
    private val cooldowns = HashMap<UUID, Long>()
    private val deathLocations = HashMap<UUID, org.bukkit.Location>()
    private const val REQUEST_EXPIRE_TIME = 600L

    fun sendTPARequest(sender: Player, target: Player, plugin: Plugin) {
        if (sender.uniqueId == target.uniqueId) {
            sender.sendMessage(miniMessage.deserialize("<red>You cannot send a teleport request to yourself."))
            return
        }

        val senderId = sender.uniqueId
        val targetId = target.uniqueId

        if (tpaRequests.containsKey(targetId) || tpaHereRequests.containsKey(targetId)) {
            sender.sendMessage(miniMessage.deserialize("<red>${target.name} already has a pending TPA request."))
            return
        }

        tpaRequests[targetId] = senderId
        cooldowns[senderId] = System.currentTimeMillis()

        sender.sendMessage(miniMessage.deserialize(" \n <#e6b8aa>Sent <#A5D8FF>teleport <#e6b8aa>request to <#A5D8FF>${target.name}\n <#e6b8aa>They have 30 seconds to accept\n     <#db0718><click:run_command:/tpacancel><hover:show_text:' '>[CANCEL]</hover></click>\n "))
        target.sendMessage(miniMessage.deserialize(" \n <#e6b8aa>Recieved <#A5D8FF>teleport <#e6b8aa>request by <#A5D8FF>${sender.name}\n <#e6b8aa>You have 30 seconds to accept\n     <#db0718><click:run_command:/tpadeny ${sender.name}><hover:show_text:' '>[DENY]</hover></click> <gray>- <#A5D8FF><click:run_command:/tpaccept ${sender.name}><hover:show_text:' '>[ACCEPT]</hover></click>\n "))

        object : BukkitRunnable() {
            override fun run() {
                if (tpaRequests.remove(targetId) != null) {
                    sender.sendMessage(miniMessage.deserialize("<red>Your teleport request to ${target.name} has timed out."))
                }
            }
        }.runTaskLater(plugin, REQUEST_EXPIRE_TIME)
    }

    fun sendTPAHereRequest(sender: Player, target: Player, plugin: Plugin) {
        if (sender.uniqueId == target.uniqueId) {
            sender.sendMessage(miniMessage.deserialize("<red>You cannot send a teleport request to yourself."))
            return
        }

        val senderId = sender.uniqueId
        val targetId = target.uniqueId

        if (tpaRequests.containsKey(targetId) || tpaHereRequests.containsKey(targetId)) {
            sender.sendMessage(miniMessage.deserialize("<red>${target.name} already has a pending TPA request."))
            return
        }

        tpaHereRequests[targetId] = senderId
        cooldowns[senderId] = System.currentTimeMillis()

        sender.sendMessage(miniMessage.deserialize(" \n <#e6b8aa>Sent <#A5D8FF>teleport here <#e6b8aa>request to <#A5D8FF>${target.name}\n <#e6b8aa>They have 30 seconds to accept\n     <#db0718><click:run_command:/tpacancel><hover:show_text:' '>[CANCEL]</hover></click>\n "))
        target.sendMessage(miniMessage.deserialize(" \n <#e6b8aa>Recieved <#A5D8FF>teleport here <#e6b8aa>request by <#A5D8FF>${sender.name}\n <#e6b8aa>You have 30 seconds to accept\n     <#db0718><click:run_command:/tpadeny ${sender.name}><hover:show_text:' '>[DENY]</hover></click> <gray>- <#A5D8FF><click:run_command:/tpaccept ${sender.name}><hover:show_text:' '>[ACCEPT]</hover></click>\n "))

        object : BukkitRunnable() {
            override fun run() {
                if (tpaHereRequests.remove(targetId) != null) {
                    sender.sendMessage(miniMessage.deserialize("<red>Your teleport request to ${target.name} has timed out."))
                }
            }
        }.runTaskLater(plugin, REQUEST_EXPIRE_TIME)
    }

    fun acceptTPARequest(target: Player, senderName: String? = null, plugin: Plugin) {
        val targetId = target.uniqueId

        val senderId = if (senderName != null) {
            val sender = Bukkit.getPlayer(senderName) ?: run {
                target.sendMessage(miniMessage.deserialize("<red>The player who sent the request is no longer online."))
                return
            }
            sender.uniqueId
        } else {
            tpaRequests.entries.find { it.key == targetId }?.value
                ?: tpaHereRequests.entries.find { it.key == targetId }?.value
                ?: run {
                    target.sendMessage(miniMessage.deserialize("<red>You have no pending TPA requests."))
                    return
                }
        }

        val sender = Bukkit.getPlayer(senderId) ?: run {
            target.sendMessage(miniMessage.deserialize("<red>The player who sent the request is no longer online."))
            return
        }

        if (tpaRequests[targetId] == senderId) {
            tpaRequests.remove(targetId)
            object : BukkitRunnable() {
                override fun run() {
                    sender.teleport(target.location)
                    sender.sendMessage(miniMessage.deserialize("<#e6b8aa>You got teleported to <#A5D8FF>${target.name}"))
                    target.sendMessage(miniMessage.deserialize("<#e6b8aa>${sender.name} got teleported to you"))
                    playBanjoSound(sender, plugin)
                    playBanjoSound(target, plugin)
                }
            }.runTask(plugin)
        } else if (tpaHereRequests[targetId] == senderId) {
            tpaHereRequests.remove(targetId)
            object : BukkitRunnable() {
                override fun run() {
                    target.teleport(sender.location)
                    target.sendMessage(miniMessage.deserialize("<#e6b8aa>You got teleported to <#A5D8FF>${sender.name}"))
                    sender.sendMessage(miniMessage.deserialize("<#e6b8aa>${target.name} got teleported to you"))
                    playBanjoSound(sender, plugin)
                    playBanjoSound(target, plugin)
                }
            }.runTask(plugin)
        } else {
            target.sendMessage(miniMessage.deserialize("<red>You have no pending TPA request from ${sender.name}."))
        }
    }

    fun denyTPARequest(target: Player, senderName: String) {
        val targetId = target.uniqueId
        val sender = Bukkit.getPlayer(senderName) ?: run {
            target.sendMessage(miniMessage.deserialize("<red>The player who sent the request is no longer online."))
            return
        }

        val senderId = sender.uniqueId

        if (tpaRequests[targetId] != senderId && tpaHereRequests[targetId] != senderId) {
            target.sendMessage(miniMessage.deserialize("<red>You have no pending TPA request from ${sender.name}."))
            return
        }

        tpaRequests.remove(targetId)
        tpaHereRequests.remove(targetId)
        sender.sendMessage(miniMessage.deserialize("<red>${target.name} has denied your TPA request."))
        target.sendMessage(miniMessage.deserialize("<red>You have denied ${sender.name}'s TPA request."))
    }

    fun cancelTPARequest(sender: Player) {
        val senderId = sender.uniqueId
        val targetId = tpaRequests.entries.find { it.value == senderId }?.key
            ?: tpaHereRequests.entries.find { it.value == senderId }?.key
            ?: run {
                sender.sendMessage(miniMessage.deserialize("<red>You have no pending TPA requests to cancel."))
                return
            }

        tpaRequests.remove(targetId)
        tpaHereRequests.remove(targetId)
        sender.sendMessage(miniMessage.deserialize("<#e6b8aa>You cancelled your request to ${Bukkit.getPlayer(targetId)?.name}"))
        Bukkit.getPlayer(targetId)
            ?.sendMessage(miniMessage.deserialize("<red>${sender.name} has canceled their TPA request."))
    }

    fun setDeathLocation(player: Player) {
        deathLocations[player.uniqueId] = player.location
    }

    fun teleportToDeathLocation(player: Player) {
        val location = deathLocations[player.uniqueId] ?: run {
            player.sendMessage(miniMessage.deserialize("<red>No death location found."))
            return
        }
        player.teleport(location)
        player.sendMessage(miniMessage.deserialize("<#e6b8aa>You have been teleported to your death location."))
    }

    private fun playBanjoSound(player: Player, plugin: Plugin) {
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f, 1.0f)
        object : BukkitRunnable() {
            override fun run() {
                player.stopSound(Sound.BLOCK_NOTE_BLOCK_BANJO)
            }
        }.runTaskLater(plugin, 30L)
    }
}