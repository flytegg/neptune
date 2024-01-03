package org.example;

import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Exclude;
import gg.flyte.neptune.annotation.Inject;
import gg.flyte.neptune.annotation.Option;
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
    public void onMute(
            SlashCommandInteractionEvent e,
            @Option(description = "the user you wish to mute") User user,
            @Option(description = "asdasdasd", required = false, autocomplete = {"silly", "annoying", "too cool"}) String theReason,
            @Option(required = false, autocomplete = {"2", "4", "5"}) int duration
    ) {
        if (theReason == null) theReason = "No reason given.";
        muteRegistry.mute(user, theReason, duration);
        e.reply("Successfully muted " + user.getAsTag() + " for " + duration + "!").queue();
    }
}