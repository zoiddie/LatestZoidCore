package org.zoid.latestZoidCore.utils

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent

class RegionEventListener : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        Region.getAllRegions().forEach { it.handleBlockBreak(event) }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        Region.getAllRegions().forEach { it.handleBlockPlace(event) }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        Region.getAllRegions().forEach { it.handleEntityDamage(event) }
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        Region.getAllRegions().forEach { it.handleEntityExplode(event) }
    }

    @EventHandler
    fun onBlockExplode(event: BlockExplodeEvent) {
        Region.getAllRegions().forEach { it.handleBlockExplode(event) }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        Region.getAllRegions().forEach { it.handleEntityDamage(event) }
    }
}