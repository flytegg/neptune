package gg.flyte.neptune.command;

import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import gg.flyte.neptune.annotation.Optional;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
            parameters[i - 1] = new NamedParameter(paramNames[i], param.getType(), !param.isAnnotationPresent(Optional.class));
        }
    }

    record NamedParameter(@NotNull String name, @NotNull Class<?> type, boolean required) {

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
}