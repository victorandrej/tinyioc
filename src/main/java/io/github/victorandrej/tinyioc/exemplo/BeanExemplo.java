package io.github.victorandrej.tinyioc.exemplo;

import io.github.victorandrej.tinyioc.steriotypes.Bean;

@Bean
public class BeanExemplo implements InterfaceBean{

    @Override
    public String getName() {
        return "BeanExemplo";
    }
}
