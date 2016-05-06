package com.longdai.discovery.exception;

/**
 * Created by karl on 2016/5/5.
 */
public class AgentException extends RuntimeException {
    public AgentException() {
        super();
    }

    public AgentException(String message) {
        super(message);
    }

    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }

    public AgentException(Throwable cause) {
        super(cause);
    }
}
