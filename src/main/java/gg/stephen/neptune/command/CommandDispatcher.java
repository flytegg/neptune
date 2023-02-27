package gg.stephen.neptune.command;

import gg.stephen.neptune.annotation.Command;
import gg.stephen.neptune.annotation.Inject;
import gg.stephen.neptune.annotation.Instantiate;
import gg.stephen.neptune.util.ArgumentConverter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CommandDispatcher {

    public CommandDispatcher(JDA jda, Object clazz, CommandManager manager, Guild... guilds) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (guilds != null) {
            for (Guild guild : guilds) {
                for (net.dv8tion.jda.api.interactions.commands.Command command : guild.retrieveCommands().complete()) {
                    command.delete().complete();
                }
            }
        } else {
            for (net.dv8tion.jda.api.interactions.commands.Command command : jda.retrieveCommands().complete()) {
                command.delete().complete();
            }
        }

        Class<?> classType = clazz.getClass();

        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        HashMap<String, Object> toInject = new HashMap<>();
        for (Method method : classType.getMethods()) {
            if (method.isAnnotationPresent(Instantiate.class)) {
                toInject.put(method.getName(), method.invoke(clazz, null));
            }
        }

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(classType.getPackage().getName()))));

        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        for (Class<?> foundClass : classes) {
            Object instance = foundClass.getName().equals(clazz.getClass().getName()) ? clazz : null;
            for (Method method : foundClass.getMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    if (instance == null) {
                        instance = foundClass.newInstance();
                    }
                    Command command = method.getAnnotation(Command.class);
                    SlashCommandData commandData = Commands.slash(command.name(), command.description());
                    commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.permissions()));
                    CommandMapping mapping = new CommandMapping(method, instance);
                    for (CommandMapping.NamedParameter param : mapping.getParameters()) {
                        commandData.addOption(ArgumentConverter.toOptionType(param.getType()), param.getName(), param.getName(), param.isRequired());
                    }

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
            if (instance != null) {
                for (Field field : instance.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        field.setAccessible(true);
                        field.set(instance, toInject.get(field.getName()));
                    }
                }
            }
        }
    }

}