package com.github.uziskull.restdbservice.model.exception;

public class DuplicateDeviceException extends DeviceException {
    public DuplicateDeviceException() {
        super("A device with the same name and brand already exists.");
    }
}
