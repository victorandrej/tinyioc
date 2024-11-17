package io.github.victorandrej.tinyioc.exception;

public class TooManyConstructorsException extends RuntimeException {
    public  TooManyConstructorsException(String message){
        super(message);
    }
}
