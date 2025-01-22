package io.github.victorandrej.tinyioc.order;

import io.github.victorandrej.tinyioc.steriotypes.Bean;
/**
 * Prioridade de um bean (0 > 5)
 */
@Bean
public class Ring3 extends Priority {
    @Override
    public  Class<? extends Priority> next() {
        return Ring2.class;
    }
}
