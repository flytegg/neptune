package gg.stephen.neptune.command;

import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import gg.stephen.neptune.annotation.Optional;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class CommandMapping {

    class NamedParameter {

        private String name;
        private Class<?> type;
        private boolean required;

        protected NamedParameter(String name, Class<?> type, boolean required) {
            this.name = name;
            this.type = type;
            this.required = required;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }

        public boolean isRequired() {
            return required;
        }

    }

    private Method method;
    private NamedParameter[] parameters;
    private Object classInstance;

    public CommandMapping(Method method, Object classInstance) {
        this.method = method;
        this.classInstance = classInstance;
        String[] paramNames = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer())).lookupParameterNames(method);
        parameters = new NamedParameter[paramNames.length - 1];
        for (int i = 1; i < paramNames.length; i++) {
            Parameter param = method.getParameters()[i];
            parameters[i - 1] = new NamedParameter(paramNames[i], param.getType(), !param.isAnnotationPresent(Optional.class));
        }
    }

    public Method getMethod() {
        return method;
    }

    public NamedParameter[] getParameters() {
        return parameters;
    }

    public Object getClassInstance() {
        return classInstance;
    }

}