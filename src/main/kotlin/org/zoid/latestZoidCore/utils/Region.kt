package org.zoid.latestZoidCore.utils

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.*
import java.util.concurrent.ConcurrentHashMap

class Region(val name: String, val point1: Location, val point2: Location) {

    companion object {
        private val regions = ConcurrentHashMap<String, Region>()
        private val selectors = ConcurrentHashMap<Player, Pair<Location?, Location?>>()
        private const val regionsFileName = "regions.dat"
        private lateinit var plugin: JavaPlugin

        fun initialize(plugin: JavaPlugin) {
            this.plugin = plugin
        }

        fun getRegion(name: String): Region? = regions[name]

        fun addRegion(region: Region) {
            regions[region.name] = region
        }

        fun removeRegion(name: String) {
            regions.remove(name)
        }

        fun getSelector(player: Player): Pair<Location?, Location?>? = selectors[player]

        fun setSelector(player: Player, point1: Location?, point2: Location?) {
            selectors[player] = Pair(point1, point2)
        }

        fun clearSelector(player: Player) {
            selectors.remove(player)
        }

        fun getAllRegions(): Collection<Region> = regions.values

        fun saveRegions() {
            val file = File(plugin.dataFolder, regionsFileName)
            file.parentFile.mkdirs()

            ObjectOutputStream(FileOutputStream(file)).use { outputStream ->
                regions.values.forEach { region ->
                    outputStream.writeObject(region.name)
                    outputStream.writeObject(serializeLocation(region.point1))
                    outputStream.writeObject(serializeLocation(region.point2))
                }
            }
        }

        fun loadRegions() {
            val file = File(plugin.dataFolder, regionsFileName)
            if (!file.exists()) return

            ObjectInputStream(FileInputStream(file)).use { inputStream ->
                while (true) {
                    try {
                        val name = inputStream.readObject() as String
                        val point1 = deserializeLocation(inputStream.readObject() as String)
                        val point2 = deserializeLocation(inputStream.readObject() as String)
                        regions[name] = Region(name, point1, point2)
                    } catch (e: EOFException) {
                        break
                    }
                }
            }
        }

        private fun serializeLocation(location: Location): String {
            return "${location.world?.name},${location.x},${location.y},${location.z},${location.yaw},${location.pitch}"
        }

        private fun deserializeLocation(serialized: String): Location {
            val parts = serialized.split(",")
            val world = Bukkit.getWorld(parts[0]) ?: throw IllegalStateException("World not found: ${parts[0]}")
            return Location(world, parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble(), parts[4].toFloat(), parts[5].toFloat())
        }
    }

    fun contains(location: Location): Boolean {
        val minX = minOf(point1.blockX, point2.blockX)
        val maxX = maxOf(point1.blockX, point2.blockX)
        val minY = minOf(point1.blockY, point2.blockY)
        val maxY = maxOf(point1.blockY, point2.blockY)
        val minZ = minOf(point1.blockZ, point2.blockZ)
        val maxZ = maxOf(point1.blockZ, point2.blockZ)

        return location.blockX in minX..maxX &&
                location.blockY in minY..maxY &&
                location.blockZ in minZ..maxZ
    }

    fun handleBlockBreak(event: BlockBreakEvent) {
        if (contains(event.block.location)) event.isCancelled = true
    }

    fun handleBlockPlace(event: BlockPlaceEvent) {
        if (contains(event.block.location)) event.isCancelled = true
    }

    fun handleEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.damager is Player && contains(event.entity.location)) event.isCancelled = true
    }

    fun handleEntityExplode(event: EntityExplodeEvent) {
        event.blockList().removeIf { contains(it.location) }
    }

    fun handleBlockExplode(event: BlockExplodeEvent) {
        event.blockList().removeIf { contains(it.location) }
    }

    fun handleEntityDamage(event: EntityDamageEvent) {
        if (contains(event.entity.location) && (event.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            event.isCancelled = true
        }
    }
}