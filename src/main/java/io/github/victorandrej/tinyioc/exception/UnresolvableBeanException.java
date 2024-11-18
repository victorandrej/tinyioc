package io.github.victorandrej.tinyioc.exception;

/**
 * Ocorre quando não é possivel resolver qual bean deve ser retornado
 */
public class UnresolvableBeanException extends RuntimeException{
    public  UnresolvableBeanException(String message){
        super(message);

    }
}
