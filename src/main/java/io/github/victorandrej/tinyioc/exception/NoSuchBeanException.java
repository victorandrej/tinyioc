package io.github.victorandrej.tinyioc.exception;

/**
 * ocorre quando não ha nenhum bean disponivel
 */
public class NoSuchBeanException extends RuntimeException {
    public  NoSuchBeanException(String message){
        super(message);

    }
}
