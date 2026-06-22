package com.r2u.notification.exception;

public class NotificationNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public NotificationNotFoundException(String message) {
        super(message);
    }

    public NotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotificationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public static NotificationNotFoundException create() {
        return new NotificationNotFoundException("Notification not found");
    }

    public static NotificationNotFoundException create(Long id) {
        return new NotificationNotFoundException("Notification not found with id: " + id);
    }

    public static NotificationNotFoundException create(String code) {
        return new NotificationNotFoundException("Notification not found with code: " + code);
    }
}
