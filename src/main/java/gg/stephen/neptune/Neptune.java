package gg.stephen.neptune;

import gg.stephen.neptune.command.CommandDispatcher;
import gg.stephen.neptune.command.CommandManager;
import gg.stephen.neptune.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

public class Neptune {

    private static JDA jda;
    private static CommandManager manager;
    public static final Logger LOGGER = new Logger();

    private Neptune(JDA jda, Object clazz, boolean clearCommands, Guild... guilds) {
        double start = System.currentTimeMillis();
        LOGGER.info("Starting Neptune...");
        Neptune.jda = jda;
        jda.addEventListener(manager = new CommandManager());
        try {
            new CommandDispatcher(jda, clazz, manager, clearCommands, guilds);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | IOException |
                 ClassNotFoundException x) {
            LOGGER.error("Error registering commands. Did you read the README.md?");
            x.printStackTrace();
        }
        LOGGER.info("Finished enabling, registered " + manager.size() + " commands in " + new DecimalFormat("#.###").format((System.currentTimeMillis() - start) / 1000D) + "s.");
    }

    public static Neptune start(JDA jda, Object clazz, Guild... guilds) {
        return new Neptune(jda, clazz, false, guilds);
    }

    public static Neptune start(JDA jda, Object clazz, boolean clearCommands, Guild... guilds) {
        return new Neptune(jda, clazz, clearCommands, guilds);
    }

    public static void terminate() {
        LOGGER.info("Terminating Neptune...");
        jda.removeEventListener(manager);
        LOGGER.info("Removed JDA event listener.");
        manager.terminate();
        System.gc();
        LOGGER.info("Terminated.");
    }
}