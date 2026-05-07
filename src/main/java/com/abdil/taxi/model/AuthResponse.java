package com.abdil.taxi.model;

public class AuthResponse {
    private String token;
    private String message;
    private boolean success;
    private User user;

    public AuthResponse() {}

    public AuthResponse(String token, String message, boolean success, User user) {
        this.token = token;
        this.message = message;
        this.success = success;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}