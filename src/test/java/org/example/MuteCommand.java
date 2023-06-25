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
    public void onMute(SlashCommandInteractionEvent e, @Description("the user you wish to mute") User user, @Optional @Description("asdasdasd") String theReason) {
        muteRegistry.mute(user, theReason == null ? "No reason given." : theReason);
        e.reply("Successfully muted " + user.getAsTag() + "!").queue();
    }
}