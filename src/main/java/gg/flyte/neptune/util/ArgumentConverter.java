package gg.flyte.neptune.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class ArgumentConverter {

    public static OptionType toOptionType(Class<?> classType) {
        if (classType == Integer.class) {
            return OptionType.INTEGER;
        } else if (classType == String.class) {
            return OptionType.STRING;
        } else if (classType == Boolean.class) {
            return OptionType.BOOLEAN;
        } else if (classType == Double.class) {
            return OptionType.NUMBER;
        } else if (classType == User.class) {
            return OptionType.USER;
        } else if (classType == Role.class) {
            return OptionType.ROLE;
        } else if (classType == Channel.class) {
            return OptionType.CHANNEL;
        } else if (classType == Message.Attachment.class) {
            return OptionType.ATTACHMENT;
        }
        return null;
    }

    public static Object toValue(OptionMapping option) {
        switch (option.getType()) {
            case INTEGER:
                return option.getAsInt();
            case STRING:
                return option.getAsString();
            case BOOLEAN:
                return option.getAsBoolean();
            case NUMBER:
                return option.getAsDouble();
            case USER:
                return option.getAsUser();
            case ROLE:
                return option.getAsRole();
            case CHANNEL:
                return option.getAsChannel();
            case ATTACHMENT:
                return option.getAsAttachment();
            case UNKNOWN:
            default:
                return null;
        }
    }

}