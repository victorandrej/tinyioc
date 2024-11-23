package io.github.victorandrej.tinyioc.config;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * classe de informacoes dos beans usado na criacao do IOC
 */
public class BeanInfo {
    private String name;
    private Class<?> beanClass;
    private Object beanInstance;
    private BeanResolveState state;
    private List<ParameterInfo> unsolvedParameters = new LinkedList<>();
    private Object[] solvedParameters;

    public BeanInfo(String name, Class<?> beanClass, Object beanInstance) {
        this.name = name;
        this.beanClass = beanClass;
        this.beanInstance = beanInstance;
    }


    public BeanInfo(String name, Class<?> clazz) {
        this(name, clazz, null);
    }

    public BeanInfo(String name, Object instance) {
        this(name, instance.getClass(), instance);
    }

    public String getName() {
        return name;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Object getBeanInstance() {
        return beanInstance;
    }

    public List<ParameterInfo> getUnsolvedParameters() {
        return unsolvedParameters;
    }

    public BeanResolveState getState() {
        return state;
    }

    public void setState(BeanResolveState state) {
        this.state = state;
    }

    public void setBeanInstance(Object beanInstance) {
        this.beanInstance = beanInstance;
    }

    public Object[] getSolvedParameters() {
        return this.solvedParameters;
    }

    public void setSolvedParameters(Object[] solvedParameters) {
        this.solvedParameters = solvedParameters;
    }
}
