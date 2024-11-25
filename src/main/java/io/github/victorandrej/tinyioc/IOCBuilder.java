package io.github.victorandrej.tinyioc;

import io.github.victorandrej.tinyioc.config.BeanInfo;
import io.github.victorandrej.tinyioc.config.Configuration;
import io.github.victorandrej.tinyioc.config.ConfigurationImpl;
import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.util.ClassUtil;

import java.util.LinkedList;

public final class IOCBuilder {
    private IOCBuilder() {
    }

    public static IOCConfigurationBuilder configure() {

        return new IOCConfigurationBuilder();
    }

    private static  IOC build(ConfigurationImpl configurationImpl){
        if(configurationImpl.getScanNonUsedBeans())
            scanNonUsedBeans(configurationImpl.getBeans(), configurationImpl.getRootClassPackage());

        return  new IOC(configurationImpl);
    }

    private static void scanNonUsedBeans(LinkedList<BeanInfo> beans, Class<?> rootClassPackage) {
        ClassUtil.findAllClasses(rootClassPackage).forEach(c->{
            if(c.isAnnotationPresent(Bean.class) &&  !beans.stream().anyMatch(b->b.getBeanClass().equals(c)))
                System.err.println("Classe anotada como bean mas nao registrada " + c.getName());
        });

    }


    public static class IOCConfigurationBuilder implements Configuration {

        private final ConfigurationImpl configurationImpl = new ConfigurationImpl();
        private boolean builded = false;

        public IOCConfigurationBuilder bean(Class<?> clazz){
            configurationImpl.bean(clazz);
            return  this;
        }
        public IOCConfigurationBuilder bean(Class<?> clazz,String name){
            configurationImpl.bean(clazz,name);
            return  this;
        }
        public IOCConfigurationBuilder bean(Object instance){
            configurationImpl.bean(instance);
            return  this;
        }

        @Override
        public IOCConfigurationBuilder useScan() {
            configurationImpl.useScan();
            return this;
        }

        public IOCConfigurationBuilder bean(Object instance,String name){
            configurationImpl.bean(instance,name);
            return  this;
        }

        /**
         *  Escaneia por beans anotados mas não registrados **DEVMODE ONLY**
         * @param rootPackageClass
         * @return
         */
        public IOCConfigurationBuilder scanNonUsedBean(Class<?> rootPackageClass){
            configurationImpl.scanNonUsedBean(rootPackageClass);
            return  this;
        }

        public IOC build(){
            return  IOCBuilder.build(configurationImpl);
        }

    }
}
