package io.github.victorandrej.tinyioc.steriotypes;

import io.github.victorandrej.tinyioc.config.Configuration;

/**
 * Uma interface para criacao de beans
 */
public interface BeanFactory {
    /**
     *  cria os beans
     * @param configuration
     */
    void create(Configuration configuration);
}
