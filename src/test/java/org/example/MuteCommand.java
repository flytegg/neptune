package org.example;

import gg.flyte.neptune.annotation.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Exclude
public final class MuteCommand {
    @Inject
    private MuteRegistry muteRegistry;

    @Command(
            name = "mute",
            description = "Mutes a user!",
            permissions = Permission.MODERATE_MEMBERS
    )
    public void onMute(SlashCommandInteractionEvent e, @Description("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa") User user, @Optional String reason) {
        muteRegistry.mute(user, reason == null ? "No reason given." : reason);
        e.reply("Successfully muted " + user.getAsTag() + "!").queue();
    }
}