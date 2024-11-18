package io.github.victorandrej.tinyioc;

import io.github.victorandrej.tinyioc.config.BeanInfo;
import io.github.victorandrej.tinyioc.config.ConfigurationImpl;
import io.github.victorandrej.tinyioc.exception.CircularReferenceException;
import io.github.victorandrej.tinyioc.exception.NoSuchBeanException;
import io.github.victorandrej.tinyioc.exception.NoSuchConstructorException;
import io.github.victorandrej.tinyioc.exception.TooManyConstructorsException;
import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.steriotypes.BeanFactory;

import java.util.*;

@Bean
public class IOC {
    BeanNode mainNode = BeanNode.newInstance();

    IOC(ConfigurationImpl configurationImpl) {
        mainNode.addInstance("ioc",this);
        resolveBeans(configurationImpl);
    }


    public <T> T getInstance(Class<T> beanClass) {
        return mainNode.getInstance(beanClass);

    }

    public <T> T getInstance(Class<T> beanClass, String name) {
        return mainNode.getInstance(beanClass, name);

    }

    private void resolveBeans(ConfigurationImpl configuration) {
        var beans = configuration.getBeans();
        var beanConfigurationMap = configuration.getClassBeanMap();
        for (var i = 0; i < beans.size(); i++) {
            var config = beans.get(i);
            switch (config.getType()) {

                case INSTANCE -> {
                    mainNode.addInstance(config.getName(), config.getBeanInstance());
                    if (config.getBeanInstance() instanceof BeanFactory factory) {
                        factory.create(configuration);
                    }

                }
                case CLASS -> {
                    var instance = resolveBeanInstance(config.getBeanClass(), beanConfigurationMap, new Stack<>());
                    if (instance instanceof BeanFactory factory) {
                        factory.create(configuration);
                    }
                }

            }
        }
    }

    private Object resolveBeanInstance(Class<?> currClass, Map<Class<?>, BeanInfo> beanConfigurationMap, Stack<Class<?>> configStack) {
        Object instance = null;

        try {
            instance = mainNode.getInstance(currClass);
        } catch (NoSuchBeanException e) {
        }
        if (Objects.nonNull(instance))
            return instance;

        if (configStack.contains(currClass))
            throw CircularReferenceException.newInstance(configStack, currClass);


        var config = beanConfigurationMap.get(currClass);

        configStack.push(currClass);


        if (currClass.getConstructors().length > 1)
            throw new TooManyConstructorsException("Bean de classe " + currClass + " com mais de 1 construtor");
        else if (currClass.getConstructors().length == 0) {
            throw new NoSuchConstructorException("Bean de classe " + currClass + " deve have pelo menos 1 construtor publico");
        }
        var constructor = currClass.getConstructors()[0];

        var parameters = constructor.getParameters();
        List<Object> instances = new LinkedList<>();

        for (var param : parameters) {
            instances.add(resolveBeanInstance(param.getType(), beanConfigurationMap, configStack));
        }

        try {
            instance = constructor.newInstance(instances.toArray());
        } catch (Exception e) {
            // EM CONDIÇÕES NORMAIS DA JVM, NÃO DEVE OCORRER
            throw new Error(e);
        }

        mainNode.addInstance(config.getName(), instance);
        configStack.pop();
        return instance;

    }


}
