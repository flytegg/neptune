package org.example;

import gg.flyte.neptune.Neptune;
import gg.flyte.neptune.annotation.Instantiate;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public final class MyBot {
    @Instantiate
    private final TestClass testClass = new TestClass();

    public MyBot() throws InterruptedException {
        JDA jda = JDABuilder.createDefault("OTgyNjAwNDk2MTg5Njc3NTY4.G4m6J3.fx7htjmaQgMUFxIPBZhI-tYDgLjw4cTUCjtofM")
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build()
                .awaitReady();

        new Neptune.Builder(jda, this)
                .addGuilds(jda.getGuildById("1106292483228450937"))
                .clearCommands(true)
                .registerAllListeners(true)
                .create();
    }

    @Instantiate
    public MuteRegistry muteRegistry() {
        return new MuteRegistry();
    }
}