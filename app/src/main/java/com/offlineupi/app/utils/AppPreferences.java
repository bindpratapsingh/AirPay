package com.offlineupi.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.offlineupi.app.models.Favorite;
import com.offlineupi.app.models.Transaction;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AppPreferences {

    private static final String PREF_NAME = "offline_upi_prefs";
    private static final String KEY_TRANSACTIONS = "transactions";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_PIN = "app_pin";
    private static final String KEY_PIN_ENABLED = "pin_enabled";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_DEFAULT_SIM = "default_sim";  // 0=auto, 1=SIM1, 2=SIM2
    private static final String KEY_IS_JIO = "is_jio_mode";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_THEME = "app_theme";          // "light","dark","system"
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_JIO_SETUP_DONE = "jio_setup_done";
    private static final String KEY_SHOW_ACCOUNT_NOTICE = "show_account_notice";
    private static final int MAX_TRANSACTIONS = 50;

    private final SharedPreferences prefs;
    private final Gson gson;
    private static AppPreferences instance;

    private AppPreferences(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized AppPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new AppPreferences(context);
        }
        return instance;
    }

    // ─── Transactions ─────────────────────────────────────────────────────────

    public List<Transaction> getTransactions() {
        String json = prefs.getString(KEY_TRANSACTIONS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Transaction>>() {}.getType();
        List<Transaction> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        List<Transaction> list = getTransactions();
        list.add(0, transaction); // newest first
        if (list.size() > MAX_TRANSACTIONS) {
            list = list.subList(0, MAX_TRANSACTIONS);
        }
        prefs.edit().putString(KEY_TRANSACTIONS, gson.toJson(list)).apply();
    }

    public void clearTransactions() {
        prefs.edit().remove(KEY_TRANSACTIONS).apply();
    }

    // ─── Favorites ────────────────────────────────────────────────────────────

    public List<Favorite> getFavorites() {
        String json = prefs.getString(KEY_FAVORITES, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Favorite>>() {}.getType();
        List<Favorite> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public void saveFavorites(List<Favorite> favorites) {
        prefs.edit().putString(KEY_FAVORITES, gson.toJson(favorites)).apply();
    }

    public void addFavorite(Favorite favorite) {
        List<Favorite> list = getFavorites();
        list.add(favorite);
        saveFavorites(list);
    }

    public void removeFavorite(String id) {
        List<Favorite> list = getFavorites();
        list.removeIf(f -> f.getId().equals(id));
        saveFavorites(list);
    }

    // ─── Security ─────────────────────────────────────────────────────────────

    public void setPin(String pin) {
        prefs.edit().putString(KEY_PIN, SecurityUtils.hashPin(pin)).apply();
    }

    public boolean verifyPin(String pin) {
        String storedHash = prefs.getString(KEY_PIN, null);
        if (storedHash == null) return false;
        return storedHash.equals(SecurityUtils.hashPin(pin));
    }

    public boolean isPinSet() {
        return prefs.getString(KEY_PIN, null) != null;
    }

    public void clearPin() {
        prefs.edit().remove(KEY_PIN).remove(KEY_PIN_ENABLED).apply();
    }

    public boolean isPinEnabled() {
        return prefs.getBoolean(KEY_PIN_ENABLED, false);
    }

    public void setPinEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PIN_ENABLED, enabled).apply();
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    // ─── Settings ─────────────────────────────────────────────────────────────

    public int getDefaultSim() {
        return prefs.getInt(KEY_DEFAULT_SIM, 0);
    }

    public void setDefaultSim(int sim) {
        prefs.edit().putInt(KEY_DEFAULT_SIM, sim).apply();
    }

    public boolean isJioMode() {
        return prefs.getBoolean(KEY_IS_JIO, false);
    }

    public void setJioMode(boolean jio) {
        prefs.edit().putBoolean(KEY_IS_JIO, jio).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getTheme() {
        return prefs.getString(KEY_THEME, "light");
    }

    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    public boolean isFirstLaunch() {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean firstLaunch) {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, firstLaunch).apply();
    }

    public boolean isJioSetupDone() {
        return prefs.getBoolean(KEY_JIO_SETUP_DONE, false);
    }

    public void setJioSetupDone(boolean done) {
        prefs.edit().putBoolean(KEY_JIO_SETUP_DONE, done).apply();
    }

    public boolean shouldShowAccountNotice() {
        return prefs.getBoolean(KEY_SHOW_ACCOUNT_NOTICE, true);
    }

    public void setAccountNoticeDismissed() {
        prefs.edit().putBoolean(KEY_SHOW_ACCOUNT_NOTICE, false).apply();
    }
}
