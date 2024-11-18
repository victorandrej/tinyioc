package io.github.victorandrej.tinyioc.exception;

/**
 * Ocorre quando uma classe e invalida
 */
public class InvalidClassException extends RuntimeException{
    public  InvalidClassException(String message){
        super(message);
    }
}

