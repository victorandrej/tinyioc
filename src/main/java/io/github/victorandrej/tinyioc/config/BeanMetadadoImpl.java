package io.github.victorandrej.tinyioc.config;


/**
 * Bean de metadados de uma instancia registrado no ioc
 */
public class BeanMetadadoImpl  implements  BeanMetadado{
    private String name;
    private Class<?> beanClass;
    private Object beanInstance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public Object getBeanInstance() {
        return beanInstance;
    }

    public void setBeanInstance(Object beanInstance) {
        this.beanInstance = beanInstance;
    }
}
