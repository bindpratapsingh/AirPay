package com.offlineupi.app.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Transaction implements Serializable {

    public enum Type {
        SEND_MONEY,
        REQUEST_MONEY,
        CHECK_BALANCE,
        PENDING_REQUESTS,
        CHANGE_PIN,
        SET_UPI_PIN,
        CHANGE_ACCOUNT,
        MINI_STATEMENT,
        VIEW_PROFILE,
        CHANGE_LANGUAGE,
        IVR_CALL,
        OTHER
    }

    public enum Mode {
        USSD,
        IVR
    }

    private String id;
    private Type type;
    private Mode mode;
    private String recipient;       // phone or UPI ID (nullable)
    private String note;            // optional memo
    private double amount;          // 0 for non-payment actions
    private String rawUssdCode;     // the exact code dialled
    private long timestamp;

    public Transaction() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public Transaction(Type type, Mode mode, String recipient,
                       String note, double amount, String rawUssdCode) {
        this();
        this.type = type;
        this.mode = mode;
        this.recipient = recipient;
        this.note = note;
        this.amount = amount;
        this.rawUssdCode = rawUssdCode;
    }

    // ─── Display helpers ──────────────────────────────────────────────────────

    public String getDisplayTitle() {
        switch (type) {
            case SEND_MONEY:
                return recipient != null ? "Sent to " + recipient : "Money Transfer";
            case REQUEST_MONEY:
                return "Request Money";
            case CHECK_BALANCE:
                return "Balance Check";
            case PENDING_REQUESTS:
                return "Pending Requests";
            case CHANGE_PIN:
                return "Change UPI PIN";
            case SET_UPI_PIN:
                return "Set/Forgot UPI PIN";
            case CHANGE_ACCOUNT:
                return "Change Bank Account";
            case MINI_STATEMENT:
                return "Mini Statement";
            case VIEW_PROFILE:
                return "View Profile";
            case CHANGE_LANGUAGE:
                return "Change Language";
            case IVR_CALL:
                return "Jio IVR Call";
            default:
                return "Action performed";
        }
    }

    public String getDisplaySubtitle() {
        String time = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                .format(new Date(timestamp));
        if ((type == Type.SEND_MONEY || type == Type.REQUEST_MONEY) && amount > 0) {
            return String.format(Locale.getDefault(), "₹%.0f  •  %s", amount, time);
        }
        return time;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getRawUssdCode() { return rawUssdCode; }
    public void setRawUssdCode(String rawUssdCode) { this.rawUssdCode = rawUssdCode; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
