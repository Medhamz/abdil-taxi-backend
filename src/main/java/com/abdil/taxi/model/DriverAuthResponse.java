package com.abdil.taxi.model;

public class DriverAuthResponse {
    private String token;
    private String message;
    private boolean success;
    private Driver driver;

    public DriverAuthResponse() {}

    public DriverAuthResponse(String token, String message, boolean success, Driver driver) {
        this.token = token;
        this.message = message;
        this.success = success;
        this.driver = driver;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }
}