package org.zoid.latestZoidCore.utils

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import org.zoid.latestZoidCore.discord.DiscordButtons
import org.zoid.latestZoidCore.discord.DiscordTicket
import org.zoid.latestZoidCore.discord.DiscordTier

    class DiscordUtils(private val token: String) {
        fun start() {
            val jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("with Zoid's balls"))
                .addEventListeners(DiscordTier(), DiscordButtons(), DiscordTicket())
                .build()
        }
    }