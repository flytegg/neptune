package gg.stephen.neptune;

import gg.stephen.neptune.command.CommandDispatcher;
import gg.stephen.neptune.command.CommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Neptune {

    private static JDA jda;
    private static CommandManager manager;

    private Neptune(JDA jda, Object clazz, Guild... guilds) {
        this.jda = jda;
        jda.addEventListener(manager = new CommandManager());
        try {
            new CommandDispatcher(jda, clazz, manager, guilds);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | IOException | ClassNotFoundException x) {
            System.out.println("[Neptune] Error registering commands. Did you read the README.md?");
            x.printStackTrace();
        }
    }

    public static Neptune start(JDA jda, Object clazz) { return new Neptune(jda, clazz); }

    public static Neptune start(JDA jda, Object clazz, Guild... guilds) { return new Neptune(jda, clazz, guilds); }

    public static void terminate() {
        jda.removeEventListener(manager);
        manager.terminate();
        System.gc();
    }

}