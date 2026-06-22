package com.r2u.notification.exception;

public class DeviceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DeviceNotFoundException(String message) {
        super(message);
    }

    public DeviceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceNotFoundException(String message, Throwable cause, boolean enableSuppression,boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public static DeviceNotFoundException create() {
        return new DeviceNotFoundException("Device not found");
    }

    public static DeviceNotFoundException create(Long id) {
        return new DeviceNotFoundException("Device not found with id: " + id);
    }

    public static DeviceNotFoundException create(String token) {
        return new DeviceNotFoundException("Device not found with token: " + token);
    }
    
    public static DeviceNotFoundException create(Long id, String token) {
        return new DeviceNotFoundException("Device not found with id: " + id + " and token: " + token);
    }

}
