package io.github.victorandrej.tinyioc.config;

import io.github.victorandrej.tinyioc.steriotypes.Bean;
/**
 * segunda classe a ser adicionada na configuracao todos os beans por padrao vem depois desse bean,
 *
 * nao faz nada apenas uma classe de ordenacao
 */
@Bean(classOrder = PrimaryBean.class , order = BeanOrder.AFTER)
public final class NormalBean {
}
