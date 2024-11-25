package io.github.victorandrej.tinyioc.steriotypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacao utilizada para marcar uma classe como bean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bean {
       public static final   String DIRECTORY = "io.github.victorandrej.tinyioc.steriotypes.Bean";
       String beanName() default  "" ;
}