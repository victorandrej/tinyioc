package io.github.victorandrej.tinyioc.order;

import io.github.victorandrej.tinyioc.steriotypes.Bean;

@Bean
public class Ring0 extends Priority {
    @Override
    public Class<? extends Priority> next() {
        return null;
    }
}
