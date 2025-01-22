package io.github.victorandrej.tinyioc.config;

import io.github.victorandrej.tinyioc.order.Priority;
import io.github.victorandrej.tinyioc.steriotypes.Bean;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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
    private Class<? extends Priority> priority;

    public BeanInfo(String name, Class<?> beanClass, Object beanInstance, Class<? extends Priority> priority) {
        this.name = name;
        this.beanClass = beanClass;
        this.priority = priority;
        this.beanInstance = beanInstance;

    }


    public BeanInfo(String name, Class<?> clazz, Class<? extends Priority> priority) {
        this(name, clazz, null, priority);
    }

    public BeanInfo(String name, Object instance, Class<? extends Priority> priority) {
        this(name, instance.getClass(), instance, priority);
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

    public Class<? extends Priority> getPriority() {
        return priority;
    }

    public void setState(BeanResolveState state) {
        this.state = state;
    }

    public void setBeanInstance(Object beanInstance) {
        this.beanInstance = beanInstance;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public Object[] getSolvedParameters() {
        return this.solvedParameters;
    }

    public void setSolvedParameters(Object[] solvedParameters) {
        this.solvedParameters = solvedParameters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, beanClass);
    }
}
