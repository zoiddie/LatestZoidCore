package org.zoid.latestZoidCore.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.zoid.latestZoidCore.utils.TierUtils
import java.awt.Color

class DiscordTier : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || !event.isFromGuild) return

        val message = event.message.contentRaw
        if (!message.startsWith(">tier ") || event.author.id != event.guild.ownerId) return

        val playerName = message.substringAfter(">tier ").trim()
        if (playerName.isEmpty()) return

        TierUtils.requestFromAPI(playerName).thenAcceptAsync { tierPlayer ->
            val tierDisplayName = tierPlayer.tier.displayName.replace(Regex("<#[A-Fa-f0-9]{6}>"), "")
            val embed = EmbedBuilder()
                .setDescription("The player `$playerName`'s tier is `$tierDisplayName`")
                .setColor(Color.decode("#2c2d31"))
                .build()

            event.message.replyEmbeds(embed).queue()
        }
    }
}