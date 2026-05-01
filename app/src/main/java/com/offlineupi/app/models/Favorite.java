package com.offlineupi.app.models;

import java.io.Serializable;
import java.util.UUID;

public class Favorite implements Serializable {
    private String id;
    private String name;
    private String identifier;   // phone number or UPI ID
    private boolean isUpiId;
    private String emoji;        // for avatar

    public Favorite() {
        this.id = UUID.randomUUID().toString();
    }

    public Favorite(String name, String identifier, boolean isUpiId) {
        this();
        this.name = name;
        this.identifier = identifier;
        this.isUpiId = isUpiId;
        this.emoji = getInitials(name);
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)).toUpperCase()
                    + String.valueOf(parts[1].charAt(0)).toUpperCase();
        }
        return String.valueOf(parts[0].charAt(0)).toUpperCase();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public boolean isUpiId() { return isUpiId; }
    public void setUpiId(boolean upiId) { isUpiId = upiId; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getInitialsDisplay() {
        return getInitials(name);
    }
}
