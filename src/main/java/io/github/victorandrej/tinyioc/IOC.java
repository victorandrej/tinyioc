package io.github.victorandrej.tinyioc;

import io.github.victorandrej.tinyioc.config.*;
import io.github.victorandrej.tinyioc.exception.*;

import io.github.victorandrej.tinyioc.order.Priority;
import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.steriotypes.BeanFactory;
import io.github.victorandrej.tinyioc.util.ClassUtil;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;


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

    public <T> Collection<T> getInstancesCollection(Class<T> clazz) {
        return mainNode.getInstancesCollection(clazz);
    }

    public Collection<BeanMetadado> getInstancesCollectionMetadado(Class<?> clazz) {
        return mainNode.getInstancesCollectionMetadado(clazz);
    }


    private void preLoad(ConfigurationImpl configurationImpl) {
        mainNode.addInstance("ioc", this);
        mainNode.addInstance("configurationImpl", configurationImpl);
    }


    private void checkCircularReference(Class<?> clazz, Set<Class<?>> classes, List<Exception> exceptions) {
        if (clazz.isInterface() || !clazz.isAnnotationPresent(Bean.class))
            return;
        if (classes.contains(clazz)) {
            exceptions.add(CircularReferenceException.newInstance(classes, clazz));
            return;
        }
        classes.add(clazz);

        Constructor c = getClassCostructor(clazz);

        for (var p : c.getParameters())
            checkCircularReference(p.getType(), new LinkedHashSet<>(classes), exceptions);

    }

    private void checkNoSuchBean(BeanInfo beanInfo, LinkedList<BeanInfo> unsolvedQueue, List<Exception> exceptions) {
        for (var param : beanInfo.getUnsolvedParameters()) {
            var has = unsolvedQueue.stream().anyMatch(b ->
                    ("".equals(param.getName().trim()) ||
                            b.getName().equals(param.getName()))
                            &&
                            b.getBeanClass().isAssignableFrom(param.getType())
            );
            if (!has)
                exceptions.add(new NoSuchBeanException("Não ha bean para o parametro " + param.getParameter() + " da classe " + beanInfo.getBeanClass()));

        }
    }

    private void checkErros(LinkedList<BeanInfo> unsolvedQueue) {
        List<Exception> exceptions = new LinkedList<>();
        for (var b : unsolvedQueue) {
            checkCircularReference(b.getBeanClass(), new HashSet<>(), exceptions);
            checkNoSuchBean(b, unsolvedQueue, exceptions);
        }

        if (!exceptions.isEmpty()) {
            throw CheckErroException.create(exceptions);
        }


    }

    enum Step {
        FACTORY, NORMAL_BEAN
    }

    private void resolveBeans(ConfigurationImpl configuration) {

        var beans = configuration.getBeans();
        LinkedList<BeanInfo> unsolvedQueue = new LinkedList<>();
        Boolean hasSolution = false;
        var step = Step.FACTORY;

        Set<Class<?>> classesPermitidas = new HashSet<>();
        Set<Class<?>> factories = new HashSet<>();

        Boolean optionalFactoryParam = false;
        Class<? extends Priority> currentPriority = null;
        for (; ; ) {
            if (beans.isEmpty()) {

                if (!hasSolution) {

                    if (!optionalFactoryParam && Step.FACTORY.equals(step))
                        optionalFactoryParam = true;
                    else {
                        checkErros(unsolvedQueue);
                    }
                }

                if (unsolvedQueue.isEmpty())
                    break;

                if (Step.FACTORY.equals(step) && factories.isEmpty())
                    step = Step.NORMAL_BEAN;

                hasSolution = false;
                beans = unsolvedQueue;
                unsolvedQueue = new LinkedList<>();
            }


            var config = beans.pop();

            if (Priority.class.isAssignableFrom(config.getBeanClass()) && unsolvedQueue.isEmpty()) {
                if (!config.getBeanClass().equals(currentPriority))
                    currentPriority = (Class<? extends Priority>) config.getBeanClass();
                else {
                    continue;
                }
            }

            Boolean isFactory = BeanFactory.class.isAssignableFrom(config.getBeanClass());


            var allowedClass = classesPermitidas.stream().anyMatch(c -> c.isAssignableFrom(config.getBeanClass()));

            if (!allowedClass &&

                    ((Step.FACTORY.equals(step)
                            && !isFactory) || (Objects.nonNull(currentPriority) && !config.getPriority().equals(currentPriority)))
            ) {
                unsolvedQueue.add(config);
                continue;
            }

            final var stepFinal = step;
            final var unsolvedFinal = unsolvedQueue;
            final var finalOptionalFactoryParam = optionalFactoryParam;
            if (!BeanResolveState.SOLVED.equals(config.getState())) {
                resolveBeanInstance(config, (p) ->
                        (!finalOptionalFactoryParam && Step.FACTORY.equals(stepFinal))
                                ||
                                unsolvedFinal.stream().anyMatch(c -> c.getBeanClass().isAssignableFrom(p.isCollection() ? p.getCollectionType() : p.getType()))
                );

            }
            if (BeanResolveState.UNFINISHED.equals(config.getState())) {
                unsolvedQueue.add(config);

                if (isFactory) {
                    factories.add(config.getBeanClass());
                }

                classesPermitidas.addAll(config.getUnsolvedParameters().stream().map(p -> p.isCollection() ? p.getCollectionType() : p.getType()).toList());
            } else if (BeanResolveState.SOLVED.equals(config.getState())) {
                if (optionalFactoryParam)
                    optionalFactoryParam = false;
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

    private void resolveBeanInstance(BeanInfo beanInfo, Function<ParameterInfo, Boolean> canSolveParameter) {
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
                var instance = paramInfo.isCollection() ? getCollection(paramInfo) : "".equals(paramInfo.getName().trim()) ? mainNode.getInstance(paramInfo.getType()) : mainNode.getInstance(paramInfo.getType(), paramInfo.getName());
                beanInfo.getSolvedParameters()[paramInfo.getIndex()] = instance;

            } catch (NoSuchBeanException e) {
                if (!canSolveParameter.apply(paramInfo) && !paramInfo.isRequired()) {
                    beanInfo.getSolvedParameters()[paramInfo.getIndex()] = null;
                    continue;
                }
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

    private Object getCollection(ParameterInfo info) {

        return mainNode.getInstancesCollection(info.getCollectionType());
    }


}
