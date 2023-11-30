package com.github.uziskull.restdbservice.model.exception;

public class MissingDeviceFieldsException extends DeviceException {
    public MissingDeviceFieldsException() {
        super("One or more mandatory fields are missing.");
    }
}
