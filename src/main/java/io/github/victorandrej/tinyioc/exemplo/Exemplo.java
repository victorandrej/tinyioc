package io.github.victorandrej.tinyioc.exemplo;

import io.github.victorandrej.tinyioc.IOCBuilder;

public class Exemplo {
    public static void main(String[] args) {

       var ioc = IOCBuilder.configure()
                .useScan().build();

       String bean = ioc.getInstance(String.class,BeanFactoryExemplo.BEAN_FACTORY_OBJECTNAME);
       System.out.println(bean);

       var beanExemplo = ioc.getInstance(InterfaceBean.class,"beanExemplo");
       System.out.println(beanExemplo instanceof  BeanExemplo);
       System.out.println(beanExemplo.getName());

       var beanExemplo2 = ioc.getInstance(InterfaceBean.class,"beanExemplo2");
       System.out.println(beanExemplo2 instanceof  BeanExemplo2);
       System.out.println(beanExemplo2.getName());

       var beanService = ioc.getInstance(ServicoExemplo.class, "servicoExemplo");
       System.out.println(beanService.getName());
    }
}
