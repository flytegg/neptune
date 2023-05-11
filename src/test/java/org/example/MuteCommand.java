package org.example;

import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Inject;
import gg.flyte.neptune.annotation.Optional;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class MuteCommand {
    @Inject
    private MuteRegistry muteRegistry;

    @Command(
            name = "mute",
            description = "Mutes a user!",
            permissions = Permission.MODERATE_MEMBERS
    )
    public void onMute(SlashCommandInteractionEvent e, User user, @Optional String reason) {
        muteRegistry.mute(user, reason == null ? "No reason given." : reason);
        e.reply("Successfully muted " + user.getAsTag() + "!").queue();
    }
}