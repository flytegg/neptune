package gg.flyte.neptune.command;

import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import gg.flyte.neptune.annotation.Description;
import gg.flyte.neptune.annotation.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static gg.flyte.neptune.Neptune.LOGGER;

public final class CommandMapping {
    private final @NotNull Method method;
    private final @NotNull Object classInstance;

    private final @NotNull NamedParameter[] parameters;

    public CommandMapping(@NotNull Method method, @NotNull Object classInstance) {
        this.method = method;
        this.classInstance = classInstance;

        String[] paramNames = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer())).lookupParameterNames(method);
        parameters = new NamedParameter[paramNames.length - 1];

        for (int i = 1; i < paramNames.length; i++) {
            Parameter param = method.getParameters()[i];
            parameters[i - 1] = new NamedParameter(lowercaseParameterName(paramNames[i]), param.getType(), !param.isAnnotationPresent(Optional.class), obtainDescription(paramNames[i], param));
        }
    }

    record NamedParameter(@NotNull String name, @NotNull Class<?> type, boolean required,
                          @Nullable String description) {
    }

    public @NotNull Method getMethod() {
        return method;
    }

    public @NotNull NamedParameter[] getParameters() {
        return parameters;
    }

    public @NotNull Object getClassInstance() {
        return classInstance;
    }

    private @Nullable String obtainDescription(@NotNull String parameterName, @NotNull Parameter parameter) {
        if (!parameter.isAnnotationPresent(Description.class)) return null;
        String description = parameter.getAnnotation(Description.class).value();

        if (description.length() <= 100) return description;

        description = description.substring(0, 100);
        LOGGER.debug("We trimmed your description for argument/option \"" + parameterName + "\" as it was more than 100 characters (a Discord limitation).");
        return description;
    }

    private @NotNull String lowercaseParameterName(@NotNull String name) {
        if (name.toLowerCase().equals(name)) return name;
        LOGGER.debug("Converted " + name + " command option to lowercase due to Discord limitation.");
        return name.toLowerCase();
    }
}