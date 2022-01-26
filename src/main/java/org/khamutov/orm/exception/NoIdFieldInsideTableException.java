package org.khamutov.orm.exception;

public class NoIdFieldInsideTableException extends RuntimeException{
    public NoIdFieldInsideTableException(String message) {
        super(message);
    }
}
