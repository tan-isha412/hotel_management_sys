package com.hotel.util;

import java.time.LocalDate;

/**
 * Validator - input validation utilities.
 */
public class Validator {

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.length() >= 2;
    }

    public static boolean isValidDateRange(LocalDate checkIn, LocalDate checkOut) {
        return checkIn != null && checkOut != null && checkOut.isAfter(checkIn);
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
