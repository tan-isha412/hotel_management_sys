package com.hotel.service;

/**
 * NotificationService — simulates email and SMS notifications.
 *
 * No real APIs are used. All messages are printed to the console and
 * tracked in memory for display in the UI popups.
 *
 * To integrate a real service later, replace the method bodies with
 * actual API calls (e.g. JavaMail for email, Twilio for SMS).
 */
public class NotificationService {

    private static final String DIVIDER = "─".repeat(50);

    /**
     * Simulate sending an email.
     *
     * @param toEmail   recipient email address
     * @param subject   email subject line
     * @param body      email body text
     */
    public static void simulateEmail(String toEmail, String subject, String body) {
        System.out.println("\n" + DIVIDER);
        System.out.println("[EMAIL NOTIFICATION — SIMULATED]");
        System.out.println("To      : " + toEmail);
        System.out.println("Subject : " + subject);
        System.out.println("Body    :");
        System.out.println(body);
        System.out.println(DIVIDER);
    }

    /**
     * Simulate sending an SMS.
     *
     * @param toPhone phone number of the recipient
     * @param message SMS message text
     */
    public static void simulateSMS(String toPhone, String message) {
        System.out.println("\n" + DIVIDER);
        System.out.println("[SMS NOTIFICATION — SIMULATED]");
        System.out.println("To      : +91 " + toPhone);
        System.out.println("Message : " + message);
        System.out.println(DIVIDER);
    }

    /**
     * Simulate an in-app push notification (console only).
     */
    public static void simulatePush(String title, String message) {
        System.out.println("\n[PUSH] " + title + " — " + message);
    }
}
