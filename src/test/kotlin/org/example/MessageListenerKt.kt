package org.example

import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.TimeUnit

class MessageListenerKt : ListenerAdapter() {
    @Inject
    private lateinit var muteRegistry: MuteRegistryKt

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (muteRegistry.isMuted(event.author)) event.message.delete().queueAfter(3L, TimeUnit.SECONDS)
    }
}
