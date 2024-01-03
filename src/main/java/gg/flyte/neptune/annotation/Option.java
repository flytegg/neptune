package gg.flyte.neptune.annotation;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Option {

    OptionType type() default OptionType.STRING;

    String name();

    String description() default "";

    boolean required() default true;

    boolean autocomplete() default true;

}
