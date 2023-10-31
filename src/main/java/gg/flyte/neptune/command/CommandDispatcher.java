package gg.flyte.neptune.command;

import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Exclude;
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

    public CommandDispatcher(@NotNull JDA jda, Object mainClass, @NotNull CommandManager commandManager, @NotNull List<Guild> guilds, boolean clearCommands, boolean registerAllListeners) throws InstantiationException, IllegalAccessException, InvocationTargetException, IOException, ClassNotFoundException, NoSuchMethodException {
        LOGGER.debug("Instantiating dispatcher...");
        this.jda = jda;
        this.commandManager = commandManager;
        this.guilds = guilds;
        this.registerAllListeners = registerAllListeners;

        if (clearCommands) unregisterCommands();

        Map<String, Object> toInject = getRequiredInstantiations(mainClass);

        // Loop all classes in the package
        for (Class<?> foundClass : findClasses(mainClass.getClass().getPackage().getName())) {
            // If main class, ignore
            if (foundClass.getName().equals(mainClass.getClass().getName())) continue;

            Object instance = null;
            boolean isListener = isListener(foundClass);
            boolean usesInject = usesInject(foundClass);
            boolean shouldExclude = shouldExclude(foundClass);

            if (isListener) instance = registerListener(foundClass);

            for (Method method : foundClass.getDeclaredMethods())
                if (isCommand(method)) instance = registerCommand(instance, foundClass, method);

            /*
             * If it's null (since that means it's not a listener or command and hasn't already been instantiated)
             * If it uses @Inject, since we want to register those unless...
             * ...it should be excluded, in which case don't go ahead and make a new instance
             * Otherwise, make new instance as one won't already exist and we want one
             */
            if (instance == null && usesInject && shouldExclude) {
                LOGGER.debug("Not instantiated " + foundClass.getSimpleName() + " despite its @Inject usage due to its @Exclude usage.");
                continue;
            }

            // If it's still null but uses @Inject, we should make an instance to control
            if (instance == null && usesInject) {
                instance = foundClass.getDeclaredConstructor().newInstance();
                LOGGER.debug("Instantiated " + foundClass.getSimpleName() + " due to its @Inject usage.");
            }

            // If still null, we cannot inject into it below
            if (instance == null) continue;

            for (Field field : foundClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
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
        for (Method method : mainClass.getClass().getDeclaredMethods()) {
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
        LOGGER.debug("Found " + toInject.size() + " methods/fields to instantiate with Neptune: " + toInject.keySet());
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

    private @NotNull Object registerCommand(@Nullable Object instance, @NotNull Class<?> commandClass, @NotNull Method commandMethod) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (shouldExclude(commandClass)) {
            LOGGER.warn(commandClass.getSimpleName() + " is incorrectly annotated with @Exclude, command classes cannot be excluded.");
        }

        if (instance == null) {
            instance = commandClass.getDeclaredConstructor().newInstance();
            LOGGER.debug("Instantiated " + instance.getClass().getSimpleName() + " as a command.");
        }

        Command command = commandMethod.getAnnotation(Command.class);
        SlashCommandData commandData = Commands.slash(command.name(), command.description());
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.permissions()));
        CommandMapping mapping = new CommandMapping(commandMethod, instance);

        for (CommandMapping.NamedParameter param : mapping.getParameters())
            commandData.addOption(
                    ArgumentConverter.toOptionType(param.type()),
                    param.name(),
                    param.description() == null ? param.name() : param.description(),
                    param.required()
            );

        commandManager.addCommand(command.name(), mapping);

        if (guilds.isEmpty()) jda.upsertCommand(commandData).complete();
        else for (Guild guild : guilds) guild.upsertCommand(commandData).complete();

        return instance;
    }

    private @Nullable Object registerListener(@NotNull Class<?> listenerClass) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (shouldExclude(listenerClass)) {
            LOGGER.debug("Not registered listener " + listenerClass.getSimpleName() + " due to its @Exclude usage.");
            return null;
        }
        if (registerAllListeners || usesInject(listenerClass)) {
            Object listener = listenerClass.getDeclaredConstructor().newInstance();
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

    private boolean isCommand(@NotNull Method method) {
        return method.isAnnotationPresent(Command.class);
    }

    private boolean usesInject(@NotNull Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields())
            if (field.isAnnotationPresent(Inject.class)) return true;
        return false;
    }

    private boolean shouldExclude(@NotNull Class<?> clazz) {
        return clazz.isAnnotationPresent(Exclude.class);
    }
}