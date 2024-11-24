package io.github.victorandrej.tinyioc.config;

import io.github.victorandrej.tinyioc.exception.InvalidClassException;
import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.util.BeanUtil;

import java.lang.reflect.Modifier;
import java.util.*;

public class ConfigurationImpl implements Configuration {
    private LinkedList<BeanInfo> beans = new LinkedList<>();
    private Boolean scanNonUsedBeans = false;
    private Class<?> rootClassPackage;

    private Bean testClass(Class<?> clazz) {
        var isMember = clazz.isMemberClass();
        var isStatic = Modifier.isStatic(clazz.getModifiers());
        var isPublic = Modifier.isPublic(clazz.getModifiers());
        var isAbstract = Modifier.isAbstract(clazz.getModifiers());
        if(isAbstract)
            throw  new InvalidClassException("Classe abstrata " + clazz);

        if (   isMember && (!isStatic || !isPublic))
            throw new InvalidClassException("Classes membros devem ser publicas e estaticas " + clazz);

        var b = getBeanAnnotation(clazz);

        return b.orElseThrow(() -> new InvalidClassException("Classe deve ser anotado com anotacao @Bean " + clazz));

    }

    private Optional<Bean> getBeanAnnotation(Class<?> clazz) {
        return Optional.ofNullable(clazz.getDeclaredAnnotation(Bean.class));
    }


    public Configuration scanNonUsedBean(Class<?> rootPackageClass) {
        this.scanNonUsedBeans = true;
        this.rootClassPackage = rootPackageClass;
        return this;
    }

    public Configuration bean(Class<?> bean) {
        var b = getBeanAnnotation(bean);
        var name = b.isPresent()? b.get().beanName() : "";
        return bean(bean,name);
    }

    private Configuration bean(BeanInfo configuration) {
        beans.add(configuration);
        return this;
    }

    public Configuration bean(Class<?> bean, String name) {
        testClass(bean);
        return bean(new BeanInfo(BeanUtil.resolveBeanName(name, bean), bean) {{
            this.setState(BeanResolveState.NEW);
        }});
    }

    public Configuration bean(Object bean, String name) {

        return bean(new BeanInfo(BeanUtil.resolveBeanName(name, bean.getClass()), bean) {{
            setState(BeanResolveState.SOLVED);
        }});
    }

    public Configuration bean(Object bean) {
        Bean b = testClass(bean.getClass());
        return bean(bean, b.beanName());
    }

    public LinkedList<BeanInfo> getBeans() {
        return beans;
    }

    public Boolean getScanNonUsedBeans() {
        return scanNonUsedBeans;
    }

    public Class<?> getRootClassPackage() {
        return rootClassPackage;
    }
}
