package gg.stephen.neptune;

import gg.stephen.neptune.command.CommandDispatcher;
import gg.stephen.neptune.command.CommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.lang.reflect.InvocationTargetException;

public class Neptune {

    private static JDA jda;
    private static CommandManager manager;

    private Neptune(JDA jda, Guild[] guilds, Object clazz) {
        this.jda = jda;
        jda.addEventListener(manager = new CommandManager());
        try {
            new CommandDispatcher(jda, guilds, clazz, manager);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException x) {
            System.out.println("[Neptune] Error registering commands. Did you read the README.md?");
            x.printStackTrace();
        }
    }

    public static Neptune start(JDA jda, Object clazz) { return new Neptune(jda, null, clazz); }

    public static Neptune start(JDA jda, Guild guild, Object clazz) { return new Neptune(jda, new Guild[]{guild}, clazz); }

    public static Neptune start(JDA jda, Guild[] guilds, Object clazz) { return new Neptune(jda, guilds, clazz); }

    public static void terminate() {
        jda.removeEventListener(manager);
        manager.terminate();
        System.gc();
    }

}