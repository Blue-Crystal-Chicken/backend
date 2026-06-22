package com.r2u.notification.exception;

public class UserDeviceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserDeviceNotFoundException(String message) {
        super(message);
    }

    public UserDeviceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserDeviceNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static UserDeviceNotFoundException create() {
        return new UserDeviceNotFoundException("UserDevice not found");
    }

    public static UserDeviceNotFoundException create(Long id) {
        return new UserDeviceNotFoundException("UserDevice not found with id: " + id);
    }

    public static UserDeviceNotFoundException create(Long userId, Long deviceId) {
        return new UserDeviceNotFoundException("UserDevice not found with userId: " + userId + " and deviceId: " + deviceId);
    }
}
