package org.example;

import gg.flyte.neptune.annotation.Inject;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    @Inject
    private MuteRegistry muteRegistry;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (muteRegistry.isMuted(event.getAuthor())) {
            event.getMessage().delete().queue();
        }
    }

}
