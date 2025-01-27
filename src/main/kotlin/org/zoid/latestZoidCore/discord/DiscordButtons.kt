package org.zoid.latestZoidCore.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DiscordButtons : ListenerAdapter() {
    private val activeThreads = ConcurrentHashMap<String, ThreadChannel>()
    private val scheduler = Executors.newScheduledThreadPool(1)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.message.contentRaw != "\$zoid info") return
        if (event.guild?.ownerId != event.author.id) return

        val embed = EmbedBuilder()
            .setDescription("## <:nerd1:1330107166883053578> Nerd Studio\n\nThis is, Nerd Studio. We develop experiences\nnot plugins, servers etc. Tap the buttons \nfor more information.")
            .setColor(Color.decode("#2c2d31"))
            .build()

        event.channel.sendMessageEmbeds(embed)
            .setActionRow(Button.primary("rules_button", "Rules"))
            .queue()
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId != "rules_button") return

        val threadId = "rules_${event.user.id}"
        activeThreads[threadId]?.let { thread ->
            thread.addThreadMember(event.user).queue()
            event.reply("Click here to view rules: ${thread.asMention}").setEphemeral(true).queue()
            return
        }

        val channel = event.channel.asTextChannel()
        val thread = (channel as IThreadContainer).createThreadChannel("rules", true)
            .complete()

        thread.manager.setInvitable(false).queue()
        activeThreads[threadId] = thread
        thread.addThreadMember(event.user).queue()

        val rulesEmbed = EmbedBuilder()
            .setDescription("## :pencil: Nerd Studio - Rules\nWhen interacting within our community\nchannels please follow the rules!\n### Chat Rules\n- No spamming or flooding the chat.\n- Be respectful to all members.\n- Avoid excessive self-promotion.\n- Keep discussions friendly and appropriate.\n- Use the correct channels for specific topics.\n### Terms & Conditions\n- By using our services, you agree to our terms.\n- We're not responsible for third-party plugin issues.\n- Redistribution of our services is prohibited.\n- Support is available during business hours.\n- We can modify or end services anytime.\n### Discord Terms & Guidelines\n- Raiding or planning raids will result in a permanent ban.  \n- Self-bots are not allowed.  \n- No discussion of group buys.  \n- Users must be 13 or older to use Discord.")
            .setColor(Color.decode("#2c2d31"))
            .build()

        thread.sendMessageEmbeds(rulesEmbed).queue()
        thread.sendMessage(event.user.asMention).queue { msg ->
            msg.delete().queueAfter(1, TimeUnit.SECONDS)
        }

        event.reply("Click here to view rules: ${thread.asMention}").setEphemeral(true).queue()

        scheduler.schedule({
            thread.removeThreadMember(event.user).queue()
            if (thread.threadMembers.isEmpty()) {
                activeThreads.remove(threadId)
                thread.delete().queue()
            }
        }, 4, TimeUnit.MINUTES)
    }
}