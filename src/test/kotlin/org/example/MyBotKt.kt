package org.example

import gg.flyte.neptune.Neptune
import gg.flyte.neptune.annotation.Instantiate
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

class MyBotKt {
    init {
        val jda = JDABuilder.createDefault("TOKEN")
            .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
            .build()
            .awaitReady()

        Neptune.Builder(jda, this)
            .addGuilds(jda.getGuildById("GUILD_ID"))
            .clearCommands(true)
            .registerAllListeners(true)
            .create()
    }

    @Instantiate
    fun muteRegistry(): MuteRegistryKt {
        return MuteRegistryKt()
    }
}