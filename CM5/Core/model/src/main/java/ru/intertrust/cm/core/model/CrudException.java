package ru.intertrust.cm.core.model;

public class CrudException extends NonRollingBackException {

    public CrudException(String message) {
        super(message);
    }

    public CrudException(String message, Throwable cause) {
        super(message, cause);
    }

    public CrudException(Throwable cause) {
        super(cause);
    }

}
