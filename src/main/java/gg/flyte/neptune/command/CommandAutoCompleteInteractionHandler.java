package gg.flyte.neptune.command;

import gg.flyte.neptune.util.ArgumentConverter;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gg.flyte.neptune.Neptune.LOGGER;

public class CommandAutoCompleteInteractionHandler extends ListenerAdapter {
    private @Nullable CommandManager commandManager;

    public void inject(@NotNull CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (commandManager == null) {
            LOGGER.error("Unable to process onCommandAutoCompleteInteraction event: CommandManager is null");
            return;
        }

        MappedCommand command = commandManager.getCommand(event.getName());
        // If it's not our command, return
        if (command == null) return;

        MappedCommand.Parameter parameter = command.getParameter(event.getFocusedOption().getName());

        // If we can't find a parameter with the name of the option that's currently focused
        if (parameter == null) {
            LOGGER.error("Unable to process onCommandAutoCompleteInteraction event: No parameter named " + event.getFocusedOption().getName() + " in " + command.getName());
            return;
        }

        event.replyChoices(Stream.of(parameter.autoComplete())
                .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // Only display words that start with the user's current input
                .map(word -> {
                    OptionType optionType = ArgumentConverter.toOptionType(parameter.type());
                    return switch (optionType) {
                        case STRING -> new Command.Choice(word, word);
                        case INTEGER -> new Command.Choice(word, Long.parseLong(word));
                        case NUMBER -> new Command.Choice(word, Double.parseDouble(word));
                        case UNKNOWN, SUB_COMMAND, SUB_COMMAND_GROUP, BOOLEAN, USER, CHANNEL, ROLE, MENTIONABLE, ATTACHMENT ->
                                null;
                    };
                }) // Map the words to choices
                .filter(Objects::nonNull)
                .collect(Collectors.toList())).queue();
    }
}
