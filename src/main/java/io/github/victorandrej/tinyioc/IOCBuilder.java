package io.github.victorandrej.tinyioc;

import io.github.victorandrej.tinyioc.config.BeanConfiguration;
import io.github.victorandrej.tinyioc.config.Configuration;
import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.util.ClassUtil;

import java.util.LinkedList;

public final class IOCBuilder {
    private IOCBuilder() {
    }

    public static IOCConfigurationBuilder configure() {

        return new IOCConfigurationBuilder();
    }

    private static  IOC build(Configuration configuration){
        if(configuration.getScanNonUsedBeans())
            scanNonUsedBeans(configuration.getBeans(),configuration.getRootClassPackage());

        return  new IOC(configuration);
    }

    private static void scanNonUsedBeans(LinkedList<BeanConfiguration> beans, Class<?> rootClassPackage) {
        ClassUtil.findAllClasses(rootClassPackage).forEach(c->{
            if(c.isAnnotationPresent(Bean.class) &&  !beans.stream().anyMatch(b->b.getBeanClass().equals(c)))
                System.err.println("Classe anotada como bean mas nao registrada " + c.getName());
        });

    }


    public static class IOCConfigurationBuilder {

        private final Configuration configuration = new Configuration();
        private boolean builded = false;

        public IOCConfigurationBuilder bean(Class<?> clazz){
            configuration.bean(clazz);
            return  this;
        }
        public IOCConfigurationBuilder bean(Class<?> clazz,String name){
            configuration.bean(clazz,name);
            return  this;
        }
        public IOCConfigurationBuilder bean(Object instance){
            configuration.bean(instance);
            return  this;
        }
        public IOCConfigurationBuilder bean(Object instance,String name){
            configuration.bean(instance,name);
            return  this;
        }

        /**
         *  Escaneia por beans anotados mas não registrados **DEVMODE ONLY**
         * @param rootPackageClass
         * @return
         */
        public IOCConfigurationBuilder scanNonUsedBean(Class<?> rootPackageClass){
            configuration.scanNonUsedBean(rootPackageClass);
            return  this;
        }

        public IOC build(){
            return  IOCBuilder.build(configuration);
        }

    }
}
