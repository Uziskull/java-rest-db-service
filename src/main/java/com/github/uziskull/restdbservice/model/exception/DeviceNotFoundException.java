package com.github.uziskull.restdbservice.model.exception;

public class DeviceNotFoundException extends DeviceException {
    public DeviceNotFoundException() {
        super("The requested device was not found.");
    }
}
