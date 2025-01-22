package io.github.victorandrej.tinyioc.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * agrupador de excoes
 */
public class CheckErroException extends RuntimeException {

    public static CheckErroException create(List<Exception> exceptions) {
        return new CheckErroException(exceptions
                .stream()
                .map(e -> "Exception: " + e.getClass().getCanonicalName() + " Message: " + e.getMessage())
                .reduce("", (s1, s2) -> s1 + "\n" + s2),
                new ArrayList<>(exceptions)
        );

    }

    private final Collection<Exception> exceptions;

    private CheckErroException(String message, Collection<Exception> exceptions) {
        super(message);
        this.exceptions = exceptions;
    }

    public Collection<Exception> getExceptions() {
        return exceptions;
    }
}
