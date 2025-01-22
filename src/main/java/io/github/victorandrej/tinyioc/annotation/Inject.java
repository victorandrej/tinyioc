package io.github.victorandrej.tinyioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configurações para a injecao de beans, não necessario
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Inject {
    /**
     *  nome do bean, padrao camelCase
     * @return
     */
    String name() default "";

    /**
     * parametro opcional?
     * @return
     */
    boolean optional() default  false;
}
