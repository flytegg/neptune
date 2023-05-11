package gg.flyte.neptune.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

public final class ArgumentConverter {
    private ArgumentConverter() {

    }

    public static OptionType toOptionType(@NotNull Class<?> classType) {
        if (classType.getName().equalsIgnoreCase("int") || classType == Integer.class) return OptionType.INTEGER;
        if (classType == String.class) return OptionType.STRING;
        if (classType == Boolean.class) return OptionType.BOOLEAN;
        if (classType == Double.class) return OptionType.NUMBER;
        if (classType == User.class) return OptionType.USER;
        if (classType == Role.class) return OptionType.ROLE;
        if (classType == Channel.class) return OptionType.CHANNEL;
        if (classType == Message.Attachment.class) return OptionType.ATTACHMENT;
        return null;
    }

    public static Object toValue(OptionMapping option) {
        return switch (option.getType()) {
            case INTEGER -> option.getAsInt();
            case STRING -> option.getAsString();
            case BOOLEAN -> option.getAsBoolean();
            case NUMBER -> option.getAsDouble();
            case USER -> option.getAsUser();
            case ROLE -> option.getAsRole();
            case CHANNEL -> option.getAsChannel();
            case ATTACHMENT -> option.getAsAttachment();
            default -> null;
        };
    }

}