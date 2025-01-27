package org.zoid.latestZoidCore

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.zoid.latestZoidCore.commands.FlyCommand
import org.zoid.latestZoidCore.commands.RtpCommand
import org.zoid.latestZoidCore.commands.SpawnCommand
import org.zoid.latestZoidCore.events.PlayerEvents
import org.zoid.latestZoidCore.tpa.DeathListener
import org.zoid.latestZoidCore.tpa.TPACommand
import org.zoid.latestZoidCore.utils.*


class LatestZoidCore : JavaPlugin() {


    override fun onEnable() {
        logger.info("ZoidCore has been enabled!")

        World.loadWorld("spawn")
        World.loadWorld("PLAINS")
        World.loadWorld("DESERT")
        World.loadWorld("SNOWY_PLAINS")

        PlayerEvents(this)
        Chat(this)
        Arena(this).start()

        val rtpCommand = getCommand("rtp")
        rtpCommand?.setExecutor(RtpCommand())
        rtpCommand?.tabCompleter = RtpCommand()


        getCommand("tpa")?.setExecutor(TPACommand(this))
        getCommand("tpahere")?.setExecutor(TPACommand(this))
        getCommand("tpaccept")?.setExecutor(TPACommand(this))
        getCommand("tpdeny")?.setExecutor(TPACommand(this))
        getCommand("tpacancel")?.setExecutor(TPACommand(this))
        getCommand("back")?.setExecutor(TPACommand(this))

        DiscordUtils("mycutiepietoken").start()

        Region.initialize(this)
        Region.loadRegions()
        getCommand("region")?.setExecutor(RegionCommandHandler(this))
        server.pluginManager.registerEvents(RegionEventListener(), this)

        getCommand("spawn")?.setExecutor(SpawnCommand())

        Bukkit.getPluginManager().registerEvents(DeathListener(), this)

        getCommand("flyspeed")?.setExecutor(FlyCommand())

    }

    override fun onDisable() {
        logger.info("latestZoidCore has been disabled!")
        Region.saveRegions()
    }
}