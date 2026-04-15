package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Customer model - encapsulates customer data.
 */
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    private String customerId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String idProof;      // Aadhaar / Passport / DL
    private String idNumber;
    private LocalDate dateOfBirth;
    private String nationality;
    private LocalDate registrationDate;

    public Customer(String customerId, String name, String email, String phone,
                    String address, String idProof, String idNumber,
                    LocalDate dateOfBirth, String nationality) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.idProof = idProof;
        this.idNumber = idNumber;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.registrationDate = LocalDate.now();
    }

    // Check if today is customer's birthday
    public boolean isBirthday() {
        LocalDate today = LocalDate.now();
        return dateOfBirth != null &&
               dateOfBirth.getMonth() == today.getMonth() &&
               dateOfBirth.getDayOfMonth() == today.getDayOfMonth();
    }

    // Getters and Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getIdProof() { return idProof; }
    public void setIdProof(String idProof) { this.idProof = idProof; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public LocalDate getRegistrationDate() { return registrationDate; }

    @Override
    public String toString() {
        return String.format("Customer[%s | %s | %s | %s]",
                customerId, name, phone, email);
    }
}
