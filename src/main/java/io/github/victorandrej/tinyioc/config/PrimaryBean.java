package io.github.victorandrej.tinyioc.config;

import io.github.victorandrej.tinyioc.steriotypes.Bean;

/**
 * Primeira classe a ser adicionada na configuracao, nao faz nada apenas uma classe de ordenacao
 */
@Bean(order = BeanOrder.BEFORE,classOrder = NormalBean.class)
public final class PrimaryBean {


}
