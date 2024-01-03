package org.example;

import gg.flyte.neptune.command.CommandAutoCompleteInteractionHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TestAutoCompleteHandler extends CommandAutoCompleteInteractionHandler {
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        var choice = new Command.Choice("piss", "piss");
        event.replyChoices(List.of(choice, choice, choice)).queue();
    }
}
