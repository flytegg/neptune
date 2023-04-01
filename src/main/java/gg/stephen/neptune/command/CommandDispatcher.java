package gg.stephen.neptune.command;

import gg.stephen.neptune.annotation.Command;
import gg.stephen.neptune.annotation.Inject;
import gg.stephen.neptune.annotation.Instantiate;
import gg.stephen.neptune.util.ArgumentConverter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static gg.stephen.neptune.Neptune.LOGGER;

public class CommandDispatcher {

    public CommandDispatcher(JDA jda, Object clazz, CommandManager manager, boolean clearCommands, Guild... guilds) throws InstantiationException, IllegalAccessException, InvocationTargetException, IOException, ClassNotFoundException {
        LOGGER.debug("Instantiating dispatcher...");
        if (clearCommands) {
            if (guilds != null) {
                LOGGER.debug("Specified guilds, clearing all guild commands....");
                int amount = 0;
                for (Guild guild : guilds) {
                    for (net.dv8tion.jda.api.interactions.commands.Command command : guild.retrieveCommands().complete()) {
                        command.delete().complete();
                        LOGGER.debug("Deleted " + command + " in " + guild + ".");
                        amount++;
                    }
                }
                LOGGER.debug("Cleared " + amount + " commands across " + guilds.length + " guilds.");
            } else {
                LOGGER.debug("No guilds were specified, clearing all commands globally...");
                int amount = 0;
                for (net.dv8tion.jda.api.interactions.commands.Command command : jda.retrieveCommands().complete()) {
                    command.delete().complete();
                    LOGGER.debug("Deleted " + command + " globally.");
                    amount++;
                }
                LOGGER.debug("Cleared " + amount + " commands globally.");
            }
        }

        Class<?> classType = clazz.getClass();

        HashMap<String, Object> toInject = new HashMap<>();
        for (Method method : classType.getMethods()) {
            if (method.isAnnotationPresent(Instantiate.class)) {
                toInject.put(method.getName(), method.invoke(clazz, null));
            }
        }

        LOGGER.debug("Found " + toInject.size() + " methods to inject with Neptune: " + toInject.keySet());

        String packageName = classType.getPackage().getName();
        LOGGER.debug("Scanning classes with related package \"" + packageName + "\".");

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(packageName)
                .setScanners(new SubTypesScanner(false), new ResourcesScanner())
        );
        Set<Class<?>> objects = reflections.getSubTypesOf(Object.class);
        Set<Class<? extends ListenerAdapter>> listeners = reflections.getSubTypesOf(ListenerAdapter.class);

        LOGGER.debug("Found " + objects.size() + " objects.");
        LOGGER.debug("Found " + listeners.size() + " listeners.");

        objects.addAll(listeners);

        for (Class<?> foundClass : objects) {
            Object instance = foundClass.getName().equals(clazz.getClass().getName()) ? clazz : null;
            for (Method method : foundClass.getMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    if (instance == null) instance = foundClass.newInstance();

                    Command command = method.getAnnotation(Command.class);
                    SlashCommandData commandData = Commands.slash(command.name(), command.description());
                    commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.permissions()));
                    CommandMapping mapping = new CommandMapping(method, instance);

                    for (CommandMapping.NamedParameter param : mapping.getParameters())
                        commandData.addOption(ArgumentConverter.toOptionType(param.getType()), param.getName(), param.getName(), param.isRequired());


                    manager.addCommand(command.name(), mapping);
                    if (guilds != null) {
                        for (Guild guild : guilds) {
                            guild.upsertCommand(commandData).queue();
                        }
                    } else {
                        jda.upsertCommand(commandData).queue();
                    }
                }
            }

            for (Field field : foundClass.getDeclaredFields()) {
                if (instance == null && field.isAnnotationPresent(Inject.class)) {
                    instance = foundClass.newInstance();
                    LOGGER.debug("Instantiated " + instance.getClass().getSimpleName() + " due to it's @Inject annotation usage.");
                }
            }

            if (instance != null) {
                for (Field field : instance.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        LOGGER.debug("Injected " + field.getName() + " into " + instance.getClass().getSimpleName() + ".");
                        field.setAccessible(true);
                        field.set(instance, toInject.get(field.getName()));
                    }
                }

                if (instance.getClass().getSuperclass().getName().equals("net.dv8tion.jda.api.hooks.ListenerAdapter")) {
                    jda.addEventListener(instance);
                    LOGGER.debug("Automatically registered " + instance.getClass().getSimpleName() + " as an EventListener due to it having an injected dependency.");
                }
            }
        }
    }

}