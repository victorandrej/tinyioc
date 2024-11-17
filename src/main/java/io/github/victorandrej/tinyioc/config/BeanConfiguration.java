package io.github.victorandrej.tinyioc.config;


public class BeanConfiguration {
    private String name;
    private Class<?> beanClass;
    private Object beanInstance;
    private BeanConfigurationType type;

    public String getName() {
        return name;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Object getBeanInstance() {
        return beanInstance;
    }

    public BeanConfigurationType getType() {
        return type;
    }

    public BeanConfiguration(String name, Class<?> beanClass, Object beanInstance,BeanConfigurationType type) {
        this.name = name;
        this.beanClass = beanClass;
        this.beanInstance = beanInstance;
        this.type = type;
    }


    public  BeanConfiguration(String name,Class<?> clazz){
        this(name,clazz,null,BeanConfigurationType.CLASS);
    }
    public  BeanConfiguration(String name,Object instance){
        this(name,instance.getClass(),instance,BeanConfigurationType.INSTANCE);
    }
}
