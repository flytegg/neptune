package org.example

import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Inject
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class MuteCommandKt {
    @Inject
    private lateinit var muteRegistry: MuteRegistryKt

    @Command(name = "mute", description = "Mutes a user!", permissions = [Permission.MODERATE_MEMBERS])
    fun onMute(e: SlashCommandInteractionEvent, user: User, number: Int, @Optional reasonTest: String?) {
        muteRegistry.mute(user, reasonTest ?: "No reason given.")
        println(number)
        e.reply("Successfully muted ${user.asTag}!").queue()
    }
}