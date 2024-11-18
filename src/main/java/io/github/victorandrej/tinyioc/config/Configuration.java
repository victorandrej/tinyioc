package io.github.victorandrej.tinyioc.config;

import io.github.victorandrej.tinyioc.steriotypes.Bean;

/**
 * Configuracoes do IOC
 */
public interface Configuration {
    /**
     * Escaneia por beans anotados mas não registrados **NAO USAR EM PRODUCAO**
     *
     * @param rootPackageClass classe que se encontra no package root
     * @return
     */
    public Configuration scanNonUsedBean(Class<?> rootPackageClass);

    /**
     * Registra uma classe de bean
     *
     * @param bean classe do bean
     * @return
     */
    public Configuration bean(Class<?> bean);

    /**
     * Registra uma classe de bean
     *
     * @param bean classe do bean
     * @param name nome do bean
     * @return
     */
    public Configuration bean(Class<?> bean, String name);

    /**
     * Registra uma classe de bean
     *
     * @param bean instancia do bean
     * @param name nome do bean
     * @return
     */
    public Configuration bean(Object bean, String name);

    /**
     * Registra uma classe de bean
     *
     * @param bean instancia do bean
     * @return
     */
    public Configuration bean(Object bean);


}
