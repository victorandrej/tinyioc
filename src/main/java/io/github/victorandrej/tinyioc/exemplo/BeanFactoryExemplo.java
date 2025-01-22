package io.github.victorandrej.tinyioc.exemplo;

import io.github.victorandrej.tinyioc.config.Configuration;
import io.github.victorandrej.tinyioc.order.None;
import io.github.victorandrej.tinyioc.order.Ring4;
import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.steriotypes.BeanFactory;

@Bean
public class BeanFactoryExemplo implements BeanFactory {
    public static final String BEAN_FACTORY_OBJECTNAME = "BEAN_FACTORY_OBJECTNAME";
    @Override
    public void create(Configuration configuration) {
        configuration.bean("BEAN CRIADO POR FACTORY",BEAN_FACTORY_OBJECTNAME, Ring4.class);
    }
}
