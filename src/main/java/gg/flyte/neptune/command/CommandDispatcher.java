package gg.flyte.neptune.command;

import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Inject;
import gg.flyte.neptune.annotation.Instantiate;
import gg.flyte.neptune.util.ArgumentConverter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static gg.flyte.neptune.Neptune.LOGGER;

public final class CommandDispatcher {
    private final @NotNull JDA jda;
    private final @NotNull CommandManager commandManager;
    private final @NotNull List<Guild> guilds;
    private final boolean registerAllListeners;

    public CommandDispatcher(@NotNull JDA jda, Object mainClass, @NotNull CommandManager commandManager, @NotNull List<Guild> guilds, boolean clearCommands, boolean registerAllListeners) throws InstantiationException, IllegalAccessException, InvocationTargetException, IOException, ClassNotFoundException {
        LOGGER.debug("Instantiating dispatcher...");
        this.jda = jda;
        this.commandManager = commandManager;
        this.guilds = guilds;
        this.registerAllListeners = registerAllListeners;

        if (clearCommands) unregisterCommands();

        Map<String, Object> toInject = getRequiredInstantiations(mainClass);

        for (Class<?> foundClass : findClasses(mainClass.getClass().getPackage().getName())) { // Loop all classes in the package
            Object instance = foundClass.getName().equals(mainClass.getClass().getName()) ? mainClass : null; // If not main class, null

            if (isListener(foundClass)) instance = registerListener(foundClass);

            for (Method method : foundClass.getMethods())
                if (method.isAnnotationPresent(Command.class)) instance = registerCommand(instance, foundClass, method);

            for (Field field : foundClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    if (instance == null) {
                        instance = foundClass.newInstance();
                        LOGGER.debug("Instantiated " + foundClass.getSimpleName() + " due to its @Inject usage.");
                    }
                    field.setAccessible(true);
                    field.set(instance, toInject.get(field.getName()));
                    LOGGER.debug("Injected " + field.getName() + " into " + instance.getClass().getSimpleName() + ".");
                }
            }
        }
    }

    private void unregisterCommands() {
        int amount = 0;
        if (guilds.isEmpty()) {
            LOGGER.debug("No guilds were specified, clearing all commands globally...");
            for (net.dv8tion.jda.api.interactions.commands.Command command : jda.retrieveCommands().complete()) {
                command.delete().complete();
                LOGGER.debug("Deleted " + command + " globally.");
                amount++;
            }
            LOGGER.debug("Cleared " + amount + " commands globally.");
            return;
        }

        LOGGER.debug("Specified guilds, clearing all guild commands....");
        for (Guild guild : guilds) {
            for (net.dv8tion.jda.api.interactions.commands.Command command : guild.retrieveCommands().complete()) {
                command.delete().complete();
                LOGGER.debug("Deleted " + command + " in " + guild + ".");
                amount++;
            }
        }
        LOGGER.debug("Cleared " + amount + " commands across " + guilds.size() + " guilds.");
    }

    private @NotNull Map<String, Object> getRequiredInstantiations(@NotNull Object mainClass) throws InvocationTargetException, IllegalAccessException {
        Map<String, Object> toInject = new HashMap<>();
        // Get all methods
        for (Method method : mainClass.getClass().getMethods()) {
            if (method.isAnnotationPresent(Instantiate.class)) {
                toInject.put(method.getName(), method.invoke(mainClass));
            }
        }
        // Get all class fields
        for (Field field : mainClass.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Instantiate.class)) {
                field.setAccessible(true);
                toInject.put(field.getName(), field.get(mainClass));
            }
        }
        LOGGER.debug("Found " + toInject.size() + " methods/fields to inject with Neptune: " + toInject.keySet());
        return toInject;
    }

    private @NotNull Set<Class<?>> findClasses(@NotNull String packageName) {
        LOGGER.debug("Scanning classes with related package \"" + packageName + "\".");

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(packageName)
                .setScanners(new SubTypesScanner(false), new ResourcesScanner())
        );

        Set<Class<?>> objects = reflections.getSubTypesOf(Object.class).stream()
                .filter(aClass -> aClass.getPackage().getName().startsWith(packageName))
                .collect(Collectors.toSet());
        Set<Class<? extends ListenerAdapter>> listeners = reflections.getSubTypesOf(ListenerAdapter.class).stream()
                .filter(aClass -> aClass.getPackage().getName().startsWith(packageName))
                .collect(Collectors.toSet());
        objects.addAll(listeners);

        LOGGER.debug("Found " + objects.size() + " classes.");
        return objects;
    }

    private @NotNull Object registerCommand(@Nullable Object instance, @NotNull Class<?> commandClass, @NotNull Method commandMethod) throws InstantiationException, IllegalAccessException {
        if (instance == null) {
            instance = commandClass.newInstance();
            LOGGER.debug("Instantiated " + instance.getClass().getSimpleName() + " as a command.");
        }

        Command command = commandMethod.getAnnotation(Command.class);
        SlashCommandData commandData = Commands.slash(command.name(), command.description());
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.permissions()));
        CommandMapping mapping = new CommandMapping(commandMethod, instance);

        for (CommandMapping.NamedParameter param : mapping.getParameters())
            commandData.addOption(ArgumentConverter.toOptionType(param.type()), lowercaseParameterName(param.name()), param.description() == null ? param.name() : param.description(), param.required());

        commandManager.addCommand(command.name(), mapping);

        if (guilds.isEmpty()) jda.upsertCommand(commandData).queue();
        else for (Guild guild : guilds) guild.upsertCommand(commandData).queue();

        return instance;
    }

    private @Nullable Object registerListener(@NotNull Class<?> listenerClass) throws InstantiationException, IllegalAccessException {
        if (registerAllListeners || usesInject(listenerClass)) {
            Object listener = listenerClass.newInstance();
            jda.addEventListener(listener);
            if (registerAllListeners)
                LOGGER.debug("Automatically registered " + listenerClass.getSimpleName() + " as an EventListener.");
            else
                LOGGER.debug("Registered " + listenerClass.getSimpleName() + " as an EventListener due to its @Inject usage.");
            return listener;
        }
        return null;
    }

    private boolean isListener(@NotNull Class<?> clazz) {
        return clazz.getSuperclass().getName().equals("net.dv8tion.jda.api.hooks.ListenerAdapter");
    }

    private boolean usesInject(@NotNull Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields())
            if (field.isAnnotationPresent(Inject.class)) return true;
        return false;
    }

    private @NotNull String lowercaseParameterName(@NotNull String name) {
        if (name.toLowerCase().equals(name)) return name;
        LOGGER.debug("Converted " + name + " command option to lowercase due to Discord limitation.");
        return name.toLowerCase();
    }
}