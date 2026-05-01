package com.offlineupi.app.utils;

import android.net.Uri;

/**
 * Builds USSD code strings for the *99# NPCI system.
 */
public class USSDBuilder {

    private static final String HASH = Uri.encode("#");
    private static final String BASE = "*99*";

    /**
     * Check UPI balance: *99*3#
     */
    public static String checkBalance() {
        return BASE + "3" + HASH;
    }

    /**
     * Send money via mobile number: *99*1*1*[mobile]*[amount]*1#
     * Adding *1 at the end to skip the remarks section.
     */
    public static String sendMoneyByMobile(String mobileNumber, String amount) {
        validateMobile(mobileNumber);
        validateAmount(amount);
        return BASE + "1*1*" + mobileNumber.trim() + "*" + amount.trim() + "*1" + HASH;
    }

    /**
     * Request Money Menu: *99*2#
     */
    public static String requestMoney() {
        return BASE + "2" + HASH;
    }

    /**
     * Send money via UPI ID Menu: *99*1*3#
     */
    public static String sendMoneyByUpiIdMenu() {
        return BASE + "1*3" + HASH;
    }

    /**
     * Send money via Account Number + IFSC Menu: *99*1*5#
     */
    public static String sendMoneyByAccountMenu() {
        return BASE + "1*5" + HASH;
    }

    /**
     * View Profile (UPI ID and Name): *99*4*3#
     */
    public static String viewProfile() {
        return BASE + "4*3" + HASH;
    }

    /**
     * Change Language: *99*4*2#
     */
    public static String changeLanguage() {
        return BASE + "4*2" + HASH;
    }

    /**
     * Check pending payment requests: *99*5#
     */
    public static String pendingRequests() {
        return BASE + "5" + HASH;
    }

    /**
     * Mini Statement (last 5 transactions): *99*6*1#
     */
    public static String miniStatement() {
        return BASE + "6*1" + HASH;
    }

    /**
     * Build the full tel: URI for the intent
     */
    public static String buildTelUri(String ussdCode) {
        return "tel:" + ussdCode;
    }

    /**
     * IVR number for Jio users (NPCI IVR)
     */
    public static String ivrNumber() {
        return "tel:08045163666";
    }

    // Validation helpers
    private static void validateMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            throw new IllegalArgumentException("Mobile number cannot be empty");
        }
        String cleaned = mobile.replaceAll("\\s+", "").replaceAll("[^0-9+]", "");
        if (cleaned.length() < 10) {
            throw new IllegalArgumentException("Invalid mobile number");
        }
    }

    private static void validateAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be empty");
        }
        try {
            double val = Double.parseDouble(amount.trim());
            if (val <= 0) throw new IllegalArgumentException("Amount must be positive");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format");
        }
    }
}
