package io.github.victorandrej.tinyioc.exception;

/**
 * Ocorre quando a beans duplicados
 */
public class DuplicatedBeanException extends RuntimeException{
    public  DuplicatedBeanException(String message){
        super(message);
    }
}
