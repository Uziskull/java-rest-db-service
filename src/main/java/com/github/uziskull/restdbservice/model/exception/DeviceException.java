package com.github.uziskull.restdbservice.model.exception;

public abstract class DeviceException extends RuntimeException {
    public DeviceException(String message) {
        super(message);
    }
}
