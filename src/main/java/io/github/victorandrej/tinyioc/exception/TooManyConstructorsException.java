package io.github.victorandrej.tinyioc.exception;

/**
 * Ocorre quando ha mais de um construtor
 */
public class TooManyConstructorsException extends RuntimeException {
    public  TooManyConstructorsException(String message){
        super(message);
    }
}
