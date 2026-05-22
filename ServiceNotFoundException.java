package com.scheduler.app.exception;

/**
 * Thrown when a required service or resource is not found.
 */
public class ServiceNotFoundException extends RuntimeException {

    private final String serviceName;

    public ServiceNotFoundException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }

    public String getServiceName() { return serviceName; }
}
