package gg.flyte.neptune.command;

import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Option;
import gg.flyte.neptune.util.ArgumentConverter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

import static gg.flyte.neptune.Neptune.LOGGER;

public final class MappedCommand {
    private final @NotNull Method commandMethod;
    private final @NotNull Object commandClassInstance;

    private final @Nullable Command command;
    private final @NotNull Map<String, Parameter> parameters = new LinkedHashMap<>();

    public MappedCommand(@NotNull Method commandMethod, @NotNull Object commandClassInstance) {
        this.commandMethod = commandMethod;
        this.commandClassInstance = commandClassInstance;
        this.command = commandMethod.getAnnotation(Command.class);

        // Get all parameters on the command method
        java.lang.reflect.Parameter[] parameters = commandMethod.getParameters();
        // and their names
        String[] parameterNames = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer())).lookupParameterNames(commandMethod);

        for (int i = 1; i < parameters.length; i++) {
            java.lang.reflect.Parameter parameter = parameters[i];
            String optionName = lowercaseParameterName(parameterNames[i]);
            Option option = parameter.getAnnotation(Option.class);

            if (option == null) {
                this.parameters.put(optionName, new Parameter(
                        parameter.getType(),
                        optionName,
                        null,
                        false,
                        new String[0]
                ));
            } else {
                OptionType optionType = ArgumentConverter.toOptionType(parameter.getType());
                String[] autocomplete = option.autocomplete();

                if (!(optionType == OptionType.STRING || optionType == OptionType.INTEGER || optionType == OptionType.NUMBER) && option.autocomplete().length != 0) {
                    LOGGER.warn("Command option " + optionName + " has autocomplete values but is not a valid autocomplete type, disabling autocomplete.");
                    autocomplete = new String[0];
                }

                this.parameters.put(optionName, new Parameter(
                        parameter.getType(),
                        optionName,
                        trimDescription(optionName, option.description()),
                        option.required(),
                        autocomplete
                ));
            }
        }
    }

    public record Parameter(
            @NotNull Class<?> type,
            @NotNull String name,
            @Nullable String description,
            boolean required,
            @NotNull String[] autoComplete
    ) {
        public boolean isAutoComplete() {
            return autoComplete.length != 0;
        }
    }

    public @Nullable Command getCommand() {
        return command;
    }

    public @NotNull String getName() {
        if (command == null) throw new NullPointerException("command is null for " + commandMethod.getName());
        return command.name();
    }

    public @NotNull String getDescription() {
        if (command == null) throw new NullPointerException("command is null for " + commandMethod.getName());
        return command.description();
    }

    public @NotNull Permission[] getPermissions() {
        if (command == null) throw new NullPointerException("command is null for " + commandMethod.getName());
        return command.permissions();
    }

    public @NotNull Method getCommandMethod() {
        return commandMethod;
    }

    public @NotNull Collection<Parameter> getParameters() {
        return parameters.values();
    }

    public @Nullable Parameter getParameter(@NotNull String name) {
        return parameters.get(name);
    }

    public @NotNull Object getCommandClassInstance() {
        return commandClassInstance;
    }

    private @Nullable String trimDescription(@NotNull String parameterName, @NotNull String description) {
        if (description.isEmpty()) return null;
        if (description.length() <= 100) return description;
        description = description.substring(0, 100);
        LOGGER.debug("We trimmed your description for argument/option \"" + parameterName + "\" as it was more than 100 characters (a Discord limitation).");
        return description;
    }

    private @NotNull String lowercaseParameterName(@NotNull String name) {
        if (name.toLowerCase().equals(name)) return name;
        LOGGER.debug("Converted " + name + " command option to lowercase due to Discord limitation.");
        return name.toLowerCase();
    }

    @Override
    public String toString() {
        return "MappedCommand{" +
                "commandMethod=" + commandMethod +
                ", commandClassInstance=" + commandClassInstance +
                ", command=" + command +
                ", parameters=" + parameters +
                '}';
    }
}