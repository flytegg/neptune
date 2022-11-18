package gg.stephen.neptune.command;

import gg.stephen.neptune.util.ArgumentConverter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.HashMap;
import java.util.Map;

public class CommandManager extends ListenerAdapter {

    private Map<String, CommandMapping> commands = new HashMap<>();

    public void addCommand(String command, CommandMapping mapping) { commands.put(command, mapping); }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if (commands.containsKey(e.getName())) {
            CommandMapping mapping = commands.get(e.getName());
            Object[] paramValues = new Object[mapping.getParameters().length + 1];
            paramValues[0] = e;

            for (int i = 0; i < mapping.getParameters().length; i++) {
                OptionMapping option = e.getOption(mapping.getParameters()[i].getName());
                paramValues[i + 1] = option == null ? null : ArgumentConverter.toValue(option);
            }

            try {
                mapping.getMethod().invoke(mapping.getClassInstance(), paramValues);
            } catch (Exception x) {
                System.out.println("[Neptune] Error triggering '" + e.getCommandString() + "' command. Did you read the README.md?");
                x.printStackTrace();
            }
        }

    }

    public void terminate() { commands.clear(); }

}