package io.github.victorandrej.tinyioc.config;

import io.github.victorandrej.tinyioc.exception.InvalidClassException;
import io.github.victorandrej.tinyioc.steriotypes.Bean;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class ConfigurationImpl implements Configuration {
    private LinkedList<BeanInfo> beans = new LinkedList<>();
    private Map<Class<?>, BeanInfo> classBeanMap = new HashMap<>();
    private Boolean scanNonUsedBeans = false;
    private Class<?> rootClassPackage;

    private Bean testClass(Class<?> clazz) {
        var isMember = clazz.isMemberClass();
        var isStatic = Modifier.isStatic(clazz.getModifiers());
        var isPublic = Modifier.isPublic(clazz.getModifiers());
        if (isMember && (!isStatic || !isPublic))
            throw new InvalidClassException("Classes membros devem ser publicas e estaticas " + clazz);

        Bean b = clazz.getDeclaredAnnotation(Bean.class);

        if (Objects.isNull(b))
            throw new InvalidClassException("Bean deve ser anotado com anotacao @Bean " + clazz);
        return b;
    }

    private String resolveBeanName(Bean b, Class<?> clazz) {

        if (!"".equals(b.beanName().trim()))
            return b.beanName();

        var className = clazz.getSimpleName();
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    public Configuration scanNonUsedBean(Class<?> rootPackageClass){
        this.scanNonUsedBeans = true;
        this.rootClassPackage = rootPackageClass;
        return  this;
    }

    public Configuration bean(Class<?> bean) {
        var b = testClass(bean);

        var configuration = new BeanInfo(resolveBeanName(b, bean), bean);
        classBeanMap.put(bean, configuration);

        return bean(configuration);
    }

    private Configuration bean(BeanInfo configuration) {
        beans.add(configuration);
        return this;
    }
    public Configuration bean(Class<?> bean, String name) {
        testClass(bean);
        return bean(new BeanInfo(name, bean));
    }
    public Configuration  bean(Object bean, String name) {
        testClass(bean.getClass());
        return bean(new BeanInfo(name, bean));
    }

    public Configuration bean(Object bean) {
        Bean b = testClass(bean.getClass());
        return bean(new BeanInfo(b.beanName(), bean));
    }

    public LinkedList<BeanInfo> getBeans() {
        return beans;
    }

    public Boolean getScanNonUsedBeans() {
        return scanNonUsedBeans;
    }

    public Map<Class<?>, BeanInfo> getClassBeanMap() {
        return classBeanMap;
    }

    public Class<?> getRootClassPackage() {
        return rootClassPackage;
    }
}
