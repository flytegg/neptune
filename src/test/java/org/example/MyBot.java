package org.example;

import gg.stephen.neptune.Neptune;
import gg.stephen.neptune.annotation.Instantiate;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class MyBot {

    public MyBot() throws InterruptedException {
        JDA jda = JDABuilder.createDefault("ODg0NDM3ODk4OTQ0OTgzMDcy.G8sXrj.VRY41lWxuDK9zeC9AuUIUILeJmU6r-0lDwJ6nk")
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build()
                .awaitReady();

        Neptune.start(jda, this, jda.getGuildById("734105416069545994"));
    }

    @Instantiate
    public MuteRegistry muteRegistry() { return new MuteRegistry(); }

}