package gg.flyte.neptune.command;

import gg.flyte.neptune.util.ArgumentConverter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static gg.flyte.neptune.Neptune.LOGGER;

public final class CommandManager extends ListenerAdapter {
    private final @NotNull Map<String, MappedCommand> commands = new HashMap<>();

    public void addCommand(MappedCommand command) {
        commands.put(command.getName(), command);
    }

    public @Nullable MappedCommand getCommand(@NotNull String name) {
        return commands.get(name);
    }

    public int size() {
        return commands.size();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if (commands.containsKey(e.getName())) {
            MappedCommand command = commands.get(e.getName());
            Collection<MappedCommand.Parameter> parameters = command.getParameters();

            Object[] methodArguments = new Object[parameters.size() + 1];
            // Add the event as the first argument
            methodArguments[0] = e;

            int i = 1;
            for (MappedCommand.Parameter parameter : command.getParameters()) {
                OptionMapping option = e.getOption(parameter.name());
                methodArguments[i] = option == null ? null : ArgumentConverter.toValue(option);
                i++;
            }

            try {
                command.getCommandMethod().invoke(command.getCommandClassInstance(), methodArguments);
            } catch (Exception x) {
                LOGGER.error("Error triggering '" + e.getCommandString() + "' command.");
                x.printStackTrace();
            }
        }
    }

    public void terminate() {
        commands.clear();
        LOGGER.info("Cleared all commands.");
    }
}