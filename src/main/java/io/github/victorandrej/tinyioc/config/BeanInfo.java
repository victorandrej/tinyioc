package io.github.victorandrej.tinyioc.config;

/**
 * classe de informacoes dos beans usado na criacao do IOC
 */
public class BeanInfo {
    private String name;
    private Class<?> beanClass;
    private Object beanInstance;
    private BeanInfoType type;

    public String getName() {
        return name;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Object getBeanInstance() {
        return beanInstance;
    }

    public BeanInfoType getType() {
        return type;
    }

    public BeanInfo(String name, Class<?> beanClass, Object beanInstance, BeanInfoType type) {
        this.name = name;
        this.beanClass = beanClass;
        this.beanInstance = beanInstance;
        this.type = type;
    }


    public BeanInfo(String name, Class<?> clazz){
        this(name,clazz,null, BeanInfoType.CLASS);
    }
    public BeanInfo(String name, Object instance){
        this(name,instance.getClass(),instance, BeanInfoType.INSTANCE);
    }
}
