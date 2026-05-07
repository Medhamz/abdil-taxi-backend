package com.abdil.taxi.model;

public class DriverRegisterRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String vehicleType;
    private String licensePlate;

    public DriverRegisterRequest() {}

    public DriverRegisterRequest(String fullName, String email, String phone, String password, String vehicleType, String licensePlate) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.vehicleType = vehicleType;
        this.licensePlate = licensePlate;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
}