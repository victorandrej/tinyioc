package io.github.victorandrej.tinyioc.steriotypes;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import io.github.victorandrej.tinyioc.order.BeanOrder;
import io.github.victorandrej.tinyioc.order.None;
import io.github.victorandrej.tinyioc.order.Priority;
import io.github.victorandrej.tinyioc.order.Ring4;

/**
 * Anotacao utilizada para marcar uma classe como bean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bean {
    public static final String DIRECTORY = "io.github.victorandrej.tinyioc.steriotypes.Bean";

    String beanName() default "";

    BeanOrder order() default BeanOrder.AFTER;
    Class<?> classOrder() default None.class;
    Class<? extends Priority> priority() default Ring4.class;
}