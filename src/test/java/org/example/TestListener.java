package org.example;

import gg.flyte.neptune.annotation.Exclude;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Exclude
public final class TestListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) event.getMessage().reply("hello").queue();
    }
}