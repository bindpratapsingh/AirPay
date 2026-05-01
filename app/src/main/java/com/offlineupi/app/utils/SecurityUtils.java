package com.offlineupi.app.utils;

import android.content.Context;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    private static final String TAG = "SecurityUtils";

    /**
     * Hash a PIN using SHA-256 for secure local storage.
     */
    public static String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 not available", e);
            return pin; // fallback (should never happen on Android)
        }
    }

    /**
     * Validate PIN format: must be 4–6 numeric digits.
     */
    public static boolean isValidPin(String pin) {
        if (pin == null) return false;
        return pin.matches("\\d{4,6}");
    }

    /**
     * Check if device has biometric hardware and enrolled credentials.
     */
    public static boolean isBiometricAvailable(Context context) {
        try {
            androidx.biometric.BiometricManager biometricManager =
                    androidx.biometric.BiometricManager.from(context);
            int result = biometricManager.canAuthenticate(
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK);
            return result == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;
        } catch (Exception e) {
            Log.e(TAG, "Biometric check failed", e);
            return false;
        }
    }

    /**
     * Mask a phone number for display: 98765XXXXX → ✓
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return phone;
        int len = phone.length();
        return phone.substring(0, len - 5) + "XXXXX";
    }

    /**
     * Mask a UPI ID for display: name@bank → n***@bank
     */
    public static String maskUpiId(String upiId) {
        if (upiId == null || !upiId.contains("@")) return upiId;
        int atIdx = upiId.indexOf('@');
        String name = upiId.substring(0, atIdx);
        String bank = upiId.substring(atIdx);
        if (name.length() <= 2) return upiId;
        return name.charAt(0) + "***" + bank;
    }
}
