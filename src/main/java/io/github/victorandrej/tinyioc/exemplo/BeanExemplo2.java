package io.github.victorandrej.tinyioc.exemplo;

import io.github.victorandrej.tinyioc.steriotypes.Bean;

@Bean(beanName = "beanExemplo2")
public class BeanExemplo2 implements InterfaceBean{
    @Override
    public String getName() {
        return "BeanExemplo2";
    }
}
