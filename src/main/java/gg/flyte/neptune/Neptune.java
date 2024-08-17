package gg.flyte.neptune;

import gg.flyte.neptune.command.CommandDispatcher;
import gg.flyte.neptune.command.CommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public final class Neptune {
    public static final @NotNull Logger LOGGER = LoggerFactory.getLogger(Neptune.class);

    private final @NotNull JDA jda;
    private final @NotNull CommandManager manager;

    private Neptune(@NotNull JDA jda, @NotNull Object mainClass, @NotNull List<Guild> guilds, boolean clearCommands, boolean registerAllListeners) {
        double start = System.currentTimeMillis();
        LOGGER.info("Starting Neptune...");
        this.jda = jda;
        jda.addEventListener(manager = new CommandManager());

        try {
            new CommandDispatcher(jda, mainClass, manager, guilds, clearCommands, registerAllListeners);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | IOException |
                 ClassNotFoundException | NoSuchMethodException x) {
            LOGGER.error("Error registering commands. Did you read the README.md?");
            x.printStackTrace();
        }

        LOGGER.info("Finished enabling, registered " + manager.size() + " commands in " + new DecimalFormat("#.###").format((System.currentTimeMillis() - start) / 1000D) + "s.");
    }

    public void terminate() {
        LOGGER.info("Terminating Neptune...");
        jda.removeEventListener(manager);
        LOGGER.info("Removed JDA event listener.");
        manager.terminate();
        System.gc();
        LOGGER.info("Terminated.");
    }

    public static class Builder {
        private final @NotNull JDA jda;
        private final @NotNull Object mainClass;
        private final @NotNull List<Guild> guilds = new ArrayList<>();
        private boolean clearCommands = false;
        private boolean registerAllListeners = true;

        public Builder(@NotNull JDA jda, @NotNull Object mainClass) {
            this.jda = jda;
            this.mainClass = mainClass;
        }

        public @NotNull Builder addGuilds(@NotNull Guild... guilds) {
            this.guilds.addAll(List.of(guilds));
            return this;
        }

        public @NotNull Builder clearCommands(boolean clearCommands) {
            this.clearCommands = clearCommands;
            return this;
        }

        public @NotNull Builder registerAllListeners(boolean registerAllListeners) {
            this.registerAllListeners = registerAllListeners;
            return this;
        }

        public @NotNull Neptune create() {
            return new Neptune(jda, mainClass, guilds, clearCommands, registerAllListeners);
        }
    }
}