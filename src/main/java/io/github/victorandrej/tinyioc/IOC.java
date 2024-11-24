package io.github.victorandrej.tinyioc;

import io.github.victorandrej.tinyioc.config.BeanInfo;
import io.github.victorandrej.tinyioc.config.BeanResolveState;
import io.github.victorandrej.tinyioc.config.ConfigurationImpl;
import io.github.victorandrej.tinyioc.config.ParameterInfo;
import io.github.victorandrej.tinyioc.exception.CircularReferenceException;
import io.github.victorandrej.tinyioc.exception.NoSuchBeanException;
import io.github.victorandrej.tinyioc.exception.NoSuchConstructorException;
import io.github.victorandrej.tinyioc.exception.TooManyConstructorsException;

import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.steriotypes.BeanFactory;


import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;

@Bean
public class IOC {
    BeanNode mainNode = BeanNode.newInstance();

    IOC(ConfigurationImpl configurationImpl) {
        preLoad();
        resolveBeans(configurationImpl);
    }


    public <T> T getInstance(Class<T> beanClass) {
        return mainNode.getInstance(beanClass);

    }

    public <T> T getInstance(Class<T> beanClass, String name) {
        return mainNode.getInstance(beanClass, name);

    }

    public <T> List<T> getInstancesCollection(Class<T> clazz){
        return (List<T>) mainNode.getInstancesCollection(clazz);
    }


    private void preLoad() {
        mainNode.addInstance("ioc", this);
    }


    private void checkCircularReference(Class<?> clazz, Set<Class<?>> classes) {
        if (classes.contains(clazz))
            throw CircularReferenceException.newInstance(classes, clazz);
        classes.add(clazz);

        Constructor c = getClassCostructor(clazz);

        for (var p : c.getParameters())
            checkCircularReference(p.getType(), classes);

    }

    private void checkNoSuchBean(BeanInfo beanInfo, LinkedList<BeanInfo> unsolvedQueue) {
        for (var param : beanInfo.getUnsolvedParameters()) {
            var has = unsolvedQueue.stream().anyMatch(b ->
                    b.getName().equals(param.getName())
                            &&
                            b.getBeanClass().equals(param.getType())
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

        for (; ; ) {
            if (beans.isEmpty()) {

                if (!hasSolution)
                    //se nenhum bean foi resolvido na rodada ocorreu algum erro
                    checkErros(unsolvedQueue);

                hasSolution = false;
                beans = unsolvedQueue;
                unsolvedQueue = new LinkedList<>();
            }

            if (beans.isEmpty())
                break;

            var config = beans.pop();

            resolveBeanInstance(config);

            if (BeanResolveState.UNFINISHED.equals(config.getState()))
                unsolvedQueue.add(config);
            else if (BeanResolveState.SOLVED.equals(config.getState())) {
                hasSolution = true;
                mainNode.addInstance(config.getName(), config.getBeanInstance());
                if (config.getBeanInstance() instanceof BeanFactory factory) {
                    ConfigurationImpl configFactory = new ConfigurationImpl();
                    factory.create(configFactory);
                    beans.addAll(configFactory.getBeans());
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
                var instance = paramInfo.isCollection() ? mainNode.getInstancesCollection(paramInfo.getType()) : "".equals(paramInfo.getName().trim()) ? mainNode.getInstance(paramInfo.getType()) : mainNode.getInstance(paramInfo.getType(), paramInfo.getName());
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
        } catch (Exception e) {
            // EM CONDIÇÕES NORMAIS DA JVM, NÃO DEVE OCORRER
            throw new Error(e);
        }
    }


}
