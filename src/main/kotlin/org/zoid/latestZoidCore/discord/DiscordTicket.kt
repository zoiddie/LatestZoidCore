package org.zoid.latestZoidCore.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

class DiscordTicket : ListenerAdapter() {

    private val staffRoles = listOf(
        "1330147612975956008",
        "1330148079290290188",
        "1330096943308996608"
    )

    private val userTickets = ConcurrentHashMap<String, String>() // Maps user ID to thread ID

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return

        val message = event.message.contentRaw
        if (message.startsWith("\$zoid setuptickets")) {
            if (!event.member!!.isOwner) return

            val embed = EmbedBuilder()
                .setDescription(
                    """
                    ## Tickets - Zoid Studio

                    Through this, you can create a ticket
                    and ask for support or order a service!

                    Note: Do not open tickets for fun.
                    """.trimIndent()
                )
                .setColor(Color.decode("#2c2d31"))
                .build()

            val button = Button.primary("create_ticket", "Create Ticket")

            event.channel.sendMessageEmbeds(embed)
                .setActionRow(button)
                .queue()
        } else if (message.startsWith("\$ticket close")) {
            if (!isStaffOrOwner(event.member!!)) return

            if (event.channel !is ThreadChannel) return

            val thread = event.channel as ThreadChannel
            thread.delete().queue()

            // Remove the user's ticket entry
            userTickets.entries.removeIf { it.value == thread.id }
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId != "create_ticket") return

        if (userTickets.containsKey(event.user.id)) {
            event.reply("You already have an open ticket. Please close your existing ticket before creating a new one.").setEphemeral(true).queue()
            return
        }

        val modal = Modal.create("ticket_modal", "Create Ticket")
            .addActionRow(
                TextInput.create("ticket_reason", "Reason", TextInputStyle.SHORT)
                    .setPlaceholder("Enter the reason for the ticket")
                    .setRequired(false)
                    .build()
            )
            .build()

        event.replyModal(modal).queue()
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId == "ticket_modal") {
            val reason = event.getValue("ticket_reason")?.asString ?: "No reason provided"
            val user = event.user
            val channel = event.guildChannel as TextChannel

            val threadName = "ticket-${channel.threadChannels.size + 1}" // Correctly calculate the thread name
            val thread = channel.createThreadChannel(threadName, true).complete()

            // Store the user's ticket entry
            userTickets[user.id] = thread.id

            val embed = EmbedBuilder()
                .setDescription(
                    """
                    ## Ticket - ${channel.threadChannels.size}
                    Ticket created by: ${user.asMention}
                    Ticket created for: $reason
                    """.trimIndent()
                )
                .setColor(Color.decode("#2c2d31"))
                .build()

            val closeButton = Button.danger("close_ticket", "Close")

            thread.sendMessageEmbeds(embed)
                .setActionRow(closeButton)
                .queue()

            staffRoles.forEach { roleId ->
                val role = event.guild?.getRoleById(roleId)
                role?.let {
                    event.guild?.getMembersWithRoles(it)?.forEach { member ->
                        thread.addThreadMember(member).queue(null) // Add members silently
                    }
                }
            }

            thread.sendMessage("${user.asMention}").queue { message ->
                message.delete().queue()
            }

            event.deferReply(true).setContent("Your ticket has been created: ${thread.asMention}").setEphemeral(true).queue()
        }
    }

    private fun isStaffOrOwner(member: Member): Boolean {
        return member.isOwner || member.roles.any { staffRoles.contains(it.id) }
    }
}