package com.lexoff.animediary;

public class ServiceUnavailableException extends Exception {

    public ServiceUnavailableException(final String message) {
        super("Service unavailable");
    }

}
