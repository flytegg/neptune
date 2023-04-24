package org.example;

import gg.flyte.neptune.Neptune;
import gg.flyte.neptune.annotation.Instantiate;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class MyBot {

    public MyBot() throws InterruptedException {
        JDA jda = JDABuilder.createDefault("TOKEN")
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build()
                .awaitReady();

        Neptune.start(jda, this, jda.getGuildById("GUILD ID"));
    }

    @Instantiate
    public MuteRegistry muteRegistry() { return new MuteRegistry(); }

}