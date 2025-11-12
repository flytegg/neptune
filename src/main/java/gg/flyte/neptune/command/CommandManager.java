package gg.flyte.neptune.command;

import gg.flyte.neptune.util.ArgumentConverter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class CommandManager extends ListenerAdapter {
    private final @NotNull Map<String, CommandMapping> commands = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);

    public void addCommand(String command, CommandMapping mapping) {
        commands.put(command, mapping);
    }

    public int size() {
        return commands.size();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if (commands.containsKey(e.getName())) {
            CommandMapping mapping = commands.get(e.getName());
            Object[] paramValues = new Object[mapping.getParameters().length + 1];
            paramValues[0] = e;

            for (int i = 0; i < mapping.getParameters().length; i++) {
                OptionMapping option = e.getOption(mapping.getParameters()[i].name());
                paramValues[i + 1] = option == null ? null : ArgumentConverter.toValue(option);
            }

            try {
                mapping.getMethod().invoke(mapping.getClassInstance(), paramValues);
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