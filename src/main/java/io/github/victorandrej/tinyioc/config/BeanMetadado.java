package io.github.victorandrej.tinyioc.config;

/**
 * Bean de metadados de uma instancia registrado no ioc
 */
public interface BeanMetadado {

    public String getName();

    public Class<?> getBeanClass();

    public Object getBeanInstance();

}
