package org.example;

import gg.flyte.neptune.annotation.Inject;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

public final class MessageListener extends ListenerAdapter {
    @Inject
    private MuteRegistry muteRegistry;

    @Inject
    private TestClass testClass;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        testClass.test();
        if (muteRegistry.isMuted(event.getAuthor())) event.getMessage().delete().queueAfter(3L, TimeUnit.SECONDS);
    }
}