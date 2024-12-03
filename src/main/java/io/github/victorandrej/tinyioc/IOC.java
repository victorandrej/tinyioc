package io.github.victorandrej.tinyioc;

import io.github.victorandrej.tinyioc.config.*;
import io.github.victorandrej.tinyioc.exception.CircularReferenceException;
import io.github.victorandrej.tinyioc.exception.NoSuchBeanException;
import io.github.victorandrej.tinyioc.exception.NoSuchConstructorException;
import io.github.victorandrej.tinyioc.exception.TooManyConstructorsException;

import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.steriotypes.BeanFactory;
import io.github.victorandrej.tinyioc.util.ClassUtil;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class IOC {
    BeanNode mainNode = BeanNode.newInstance();

    IOC(ConfigurationImpl configurationImpl) {
        preLoad(configurationImpl);
        resolveBeans(configurationImpl);
    }


    public void register(String name, Object bean) {
        mainNode.addInstance(name, bean);
    }

    public <T> T getInstance(Class<T> beanClass) {
        return mainNode.getInstance(beanClass);

    }

    public <T> T getInstance(Class<T> beanClass, String name) {
        return mainNode.getInstance(beanClass, name);

    }

    public List<BeanMetadado> getInstancesCollection(Class<?> clazz) {
        return mainNode.getInstancesCollection(clazz);
    }


    private void preLoad(ConfigurationImpl configurationImpl) {
        mainNode.addInstance("ioc", this);
        mainNode.addInstance("configurationImpl", configurationImpl);
    }


    private void checkCircularReference(Class<?> clazz, Set<Class<?>> classes) {
        if(clazz.isInterface() || !clazz.isAnnotationPresent(Bean.class))
            return;
        if (classes.contains(clazz))
            throw CircularReferenceException.newInstance(classes, clazz);
        classes.add(clazz);

        Constructor c = getClassCostructor(clazz);

        for (var p : c.getParameters())
            checkCircularReference(p.getType(), new LinkedHashSet<>(classes));

    }

    private void checkNoSuchBean(BeanInfo beanInfo, LinkedList<BeanInfo> unsolvedQueue) {
        for (var param : beanInfo.getUnsolvedParameters()) {
            var has = unsolvedQueue.stream().anyMatch(b ->
                    (  "".equals(param.getName().trim()) ||
                    b.getName().equals(param.getName()))
                            &&
                            b.getBeanClass().isAssignableFrom(param.getType())
            );
            if (!has)
                throw new NoSuchBeanException("Não ha bean para o parametro " + param.getParameter() + " da classe " + beanInfo.getBeanClass());

        }
    }

    private void checkErros(LinkedList<BeanInfo> unsolvedQueue) {

        for (var b : unsolvedQueue) {
            checkCircularReference(b.getBeanClass(), new HashSet<>());
            checkNoSuchBean(b, unsolvedQueue);
        }

    }

    private void resolveBeans(ConfigurationImpl configuration) {
        var beans = configuration.getBeans();

        LinkedList<BeanInfo> unsolvedQueue = new LinkedList<>();
        Boolean hasSolution = false;
        Boolean firstCicle = true;
        Boolean beforeFirst = true;
        Set<Class<?>> classesPermitidas = new HashSet();
        Set<Class<?>> factories = new HashSet<>();
        for (; ; ) {
            if (beans.isEmpty()) {

                if (!hasSolution && !firstCicle)
                    //se nenhum bean foi resolvido na rodada ocorreu algum erro
                    checkErros(unsolvedQueue);

                if (unsolvedQueue.isEmpty())
                    break;

                firstCicle = false;
                hasSolution = false;
                beans = unsolvedQueue;
                unsolvedQueue = new LinkedList<>();
            }


            var config = beans.pop();
            Boolean isFactory = BeanFactory.class.isAssignableFrom(config.getBeanClass());
            beforeFirst =  beforeFirst ?  config.getBeanClass().equals(PrimaryBean.class) ? false : true : false;

            if (LastBean.class.equals(config.getBeanClass()) && !unsolvedQueue.isEmpty()) {
                unsolvedQueue.add(config);
                unsolvedQueue.addAll(beans);
                beans.clear();
                continue;
            }

            if (!beforeFirst && !isFactory && ( firstCicle || !factories.isEmpty()  && !classesPermitidas.contains(config.getBeanClass()))) {
                unsolvedQueue.add(config);
                continue;
            }

            if(!BeanResolveState.SOLVED.equals(config.getState()) )
                resolveBeanInstance(config);

            if (BeanResolveState.UNFINISHED.equals(config.getState())) {

                if (isFactory) {
                    factories.add(config.getBeanClass());
                }
                classesPermitidas.addAll(config.getUnsolvedParameters().stream().map(p -> p.getType()).toList());
                unsolvedQueue.add(config);

            } else if (BeanResolveState.SOLVED.equals(config.getState())) {
                factories.remove(config.getBeanClass());
                classesPermitidas.remove(config.getBeanClass());
                hasSolution = true;
                mainNode.addInstance(config.getName(), config.getBeanInstance());
                if (config.getBeanInstance() instanceof BeanFactory factory) {
                    ConfigurationImpl configFactory = new ConfigurationImpl();
                    ClassUtil.sneakyThrow(() -> factory.create(configFactory));
                    beans.addAll(0, configFactory.getBeans());
                }

            }


        }
    }


    private Constructor<?> getClassCostructor(Class<?> clazz) {
        var constructors = clazz.getConstructors();
        if (constructors.length > 1)
            throw new TooManyConstructorsException("Bean de classe " + clazz + " com mais de 1 construtor");
        else if (constructors.length == 0) {
            throw new NoSuchConstructorException("Bean de classe " + clazz + " deve have pelo menos 1 construtor publico");
        }
        return constructors[0];
    }

    private void resolveBeanInstance(BeanInfo beanInfo) {
        var clazz = beanInfo.getBeanClass();


        var constructor = getClassCostructor(clazz);

        var parameters = constructor.getParameters();

        List<ParameterInfo> unsolvedParameters = new LinkedList<>(beanInfo.getUnsolvedParameters());
        beanInfo.getUnsolvedParameters().clear();

        boolean hasUnsolvedParameters = false;
        boolean newBean = BeanResolveState.NEW.equals(beanInfo.getState());
        var length = newBean ? parameters.length : unsolvedParameters.size();
        if (newBean) {
            beanInfo.setSolvedParameters(new Object[length]);
        }

        for (var i = 0; i < length; i++) {
            var paramInfo = newBean ? new ParameterInfo(parameters[i], i) : unsolvedParameters.get(i);

            try {
                var instance = paramInfo.isCollection() ? getCollection(paramInfo.getType()) : "".equals(paramInfo.getName().trim()) ? mainNode.getInstance(paramInfo.getType()) : mainNode.getInstance(paramInfo.getType(), paramInfo.getName());
                beanInfo.getSolvedParameters()[paramInfo.getIndex()] = instance;

            } catch (NoSuchBeanException e) {
                hasUnsolvedParameters = true;
                beanInfo.getUnsolvedParameters().add(paramInfo);
            }
        }

        if (hasUnsolvedParameters) {
            beanInfo.setState(BeanResolveState.UNFINISHED);
            return;
        }

        try {


            beanInfo.setBeanInstance(constructor.newInstance(beanInfo.getSolvedParameters()));
            beanInfo.setState(BeanResolveState.SOLVED);
        } catch (InvocationTargetException ex) {
            ClassUtil.sneakyThrow(ex.getCause());
        } catch (Exception e) {
            ClassUtil.sneakyThrow(e);
        }
    }

    private Object getCollection(Class<?> type) {
        return mainNode.getInstancesCollection(type).stream().map(b -> b.getBeanInstance()).toList();
    }


}
