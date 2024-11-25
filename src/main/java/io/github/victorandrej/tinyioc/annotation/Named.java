package io.github.victorandrej.tinyioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Named {
    public static final   String DIRECTORY = "io.github.victorandrej.tinyioc.annotation.Named";
    String value();
}
