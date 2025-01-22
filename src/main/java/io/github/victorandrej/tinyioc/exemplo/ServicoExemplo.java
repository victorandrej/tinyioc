package io.github.victorandrej.tinyioc.exemplo;


import io.github.victorandrej.tinyioc.annotation.Inject;
import io.github.victorandrej.tinyioc.steriotypes.Bean;

@Bean
public class ServicoExemplo {
    private  InterfaceBean in;

    public  ServicoExemplo( @Inject(name = "beanExemplo")  InterfaceBean in){
        this.in = in;
    }

    public String getName(){
        return  this.in.getName();
    }
}
