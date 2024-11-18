package io.github.victorandrej.tinyioc.exception;

/**
 * Ocorre quando não ha nenhum construtor disponivel
 */
public class NoSuchConstructorException extends RuntimeException {
    public  NoSuchConstructorException (String message) {
        super(message);
    }
}
