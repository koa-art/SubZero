package com.subguard.app.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.subguard.app.model.PaymentRecord;
import com.subguard.app.model.Subscription;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class StorageManager {

    private static final String PREF_NAME = "subguard_data";
    private static final String KEY_SUBS = "subscriptions";
    private static final String KEY_PAYMENTS = "payments";
    private static final String KEY_SETTINGS = "settings";

    private SharedPreferences prefs;
    private List<Subscription> subscriptions;
    private List<PaymentRecord> payments;
    private Map<String, String> settings;

    private static StorageManager instance;

    public static StorageManager getInstance(Context context) {
        if (instance == null) instance = new StorageManager(context.getApplicationContext());
        return instance;
    }

    private StorageManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadAll();
        if (!settings.containsKey("currency")) {
            settings.put("currency", "CNY");
            settings.put("dark_mode", "false");
            settings.put("reminder_days", "3");
            saveSettings();
        }
    }

    public void reload() { loadAll(); }

    private void loadAll() {
        subscriptions = loadSubscriptions();
        payments = loadPayments();
        settings = loadSettings();
    }

    // ===== Subscriptions =====

    private List<Subscription> loadSubscriptions() {
        List<Subscription> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString(KEY_SUBS, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Subscription s = new Subscription();
                s.setId(o.getInt("id"));
                s.setName(o.getString("name"));
                s.setAmount(o.getDouble("amount"));
                s.setCategory(o.optString("category", "other"));
                s.setCycle(o.optString("cycle", "monthly"));
                s.setNextPaymentDate(o.optString("nextPaymentDate", ""));
                s.setStartDate(o.optString("startDate", ""));
                s.setNotes(o.optString("notes", ""));
                s.setActive(o.optBoolean("active", true));
                list.add(s);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private void saveSubscriptions() {
        try {
            JSONArray arr = new JSONArray();
            for (Subscription s : subscriptions) {
                JSONObject o = new JSONObject();
                o.put("id", s.getId()); o.put("name", s.getName());
                o.put("amount", s.getAmount()); o.put("category", s.getCategory());
                o.put("cycle", s.getCycle()); o.put("nextPaymentDate", s.getNextPaymentDate());
                o.put("startDate", s.getStartDate()); o.put("notes", s.getNotes());
                o.put("active", s.isActive());
                arr.put(o);
            }
            prefs.edit().putString(KEY_SUBS, arr.toString()).apply();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public long insertSubscription(Subscription s) {
        int maxId = 0;
        for (Subscription sub : subscriptions) maxId = Math.max(maxId, sub.getId());
        s.setId(maxId + 1);
        subscriptions.add(s);
        saveSubscriptions();
        // Auto-create first payment record
        addPaymentRecord(new PaymentRecord(s.getId(), s.getAmount(), s.getStartDate(), s.getName()));
        return s.getId();
    }

    public int updateSubscription(Subscription s) {
        for (int i = 0; i < subscriptions.size(); i++) {
            if (subscriptions.get(i).getId() == s.getId()) {
                subscriptions.set(i, s); break;
            }
        }
        saveSubscriptions();
        return 1;
    }

    public int deleteSubscription(int id) {
        subscriptions.removeIf(s -> s.getId() == id);
        payments.removeIf(p -> p.getSubscriptionId() == id);
        saveSubscriptions();
        savePayments();
        return 1;
    }

    public List<Subscription> getAllSubscriptions() {
        return new ArrayList<>(subscriptions);
    }

    public List<Subscription> getActiveSubscriptions() {
        List<Subscription> active = new ArrayList<>();
        for (Subscription s : subscriptions) { if (s.isActive()) active.add(s); }
        return active;
    }

    public Subscription getSubscriptionById(int id) {
        for (Subscription s : subscriptions) { if (s.getId() == id) return s; }
        return null;
    }

    public double getTotalMonthlySpend() {
        double total = 0;
        for (Subscription s : subscriptions) { if (s.isActive()) total += s.getMonthlyAmount(); }
        return total;
    }

    public double getTotalYearlySpend() {
        double total = 0;
        for (Subscription s : subscriptions) { if (s.isActive()) total += s.getYearlyAmount(); }
        return total;
    }

    public Map<String, Double> getCategoryBreakdown() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Subscription s : subscriptions) {
            if (!s.isActive()) continue;
            String cat = s.getCategory() != null ? s.getCategory() : "other";
            map.put(cat, map.getOrDefault(cat, 0.0) + s.getMonthlyAmount());
        }
        return map;
    }

    public List<Subscription> getUpcomingRenewals(int days) {
        List<Subscription> result = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        Calendar deadline = (Calendar) today.clone();
        deadline.add(Calendar.DAY_OF_MONTH, days);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = sdf.format(today.getTime());
        String deadlineStr = sdf.format(deadline.getTime());

        for (Subscription s : subscriptions) {
            if (!s.isActive() || s.getNextPaymentDate() == null || s.getNextPaymentDate().isEmpty())
                continue;
            if (s.getNextPaymentDate().compareTo(todayStr) >= 0
                    && s.getNextPaymentDate().compareTo(deadlineStr) <= 0) {
                result.add(s);
            }
        }
        result.sort((a, b) -> a.getNextPaymentDate().compareTo(b.getNextPaymentDate()));
        return result;
    }

    // ===== Payment Records =====

    private List<PaymentRecord> loadPayments() {
        List<PaymentRecord> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString(KEY_PAYMENTS, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                PaymentRecord p = new PaymentRecord();
                p.setId(o.getInt("id"));
                p.setSubscriptionId(o.getInt("subscriptionId"));
                p.setAmount(o.getDouble("amount"));
                p.setPaymentDate(o.optString("paymentDate", ""));
                p.setSubscriptionName(o.optString("subscriptionName", ""));
                list.add(p);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private void savePayments() {
        try {
            JSONArray arr = new JSONArray();
            for (PaymentRecord p : payments) {
                JSONObject o = new JSONObject();
                o.put("id", p.getId()); o.put("subscriptionId", p.getSubscriptionId());
                o.put("amount", p.getAmount()); o.put("paymentDate", p.getPaymentDate());
                o.put("subscriptionName", p.getSubscriptionName());
                arr.put(o);
            }
            prefs.edit().putString(KEY_PAYMENTS, arr.toString()).apply();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void addPaymentRecord(PaymentRecord p) {
        int maxId = 0;
        for (PaymentRecord pr : payments) maxId = Math.max(maxId, pr.getId());
        p.setId(maxId + 1);
        payments.add(p);
        savePayments();
    }

    public List<PaymentRecord> getAllPayments() { return new ArrayList<>(payments); }

    // ===== Settings =====

    private Map<String, String> loadSettings() {
        Map<String, String> map = new HashMap<>();
        try {
            JSONObject o = new JSONObject(prefs.getString(KEY_SETTINGS, "{}"));
            Iterator<String> keys = o.keys();
            while (keys.hasNext()) { String k = keys.next(); map.put(k, o.getString(k)); }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    private void saveSettings() {
        try {
            JSONObject o = new JSONObject();
            for (Map.Entry<String, String> e : settings.entrySet()) o.put(e.getKey(), e.getValue());
            prefs.edit().putString(KEY_SETTINGS, o.toString()).apply();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public String getSetting(String key) { return settings.getOrDefault(key, ""); }
    public void setSetting(String key, String value) { settings.put(key, value); saveSettings(); }

    // ===== Export =====

    public String exportToJson() {
        StringBuilder json = new StringBuilder("{\n");
        json.append("  \"exportDate\": \"").append(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()))
                .append("\",\n");
        json.append("  \"summary\": {\n");
        json.append("    \"monthlyTotal\": ").append(getTotalMonthlySpend()).append(",\n");
        json.append("    \"yearlyTotal\": ").append(getTotalYearlySpend()).append(",\n");
        json.append("    \"activeCount\": ").append(getActiveSubscriptions().size()).append("\n");
        json.append("  },\n");
        json.append("  \"subscriptions\": [\n");
        boolean first = true;
        for (Subscription s : subscriptions) {
            if (!first) json.append(",\n"); first = false;
            json.append("    {\"id\":").append(s.getId())
                    .append(",\"name\":\"").append(esc(s.getName()))
                    .append("\",\"amount\":").append(s.getAmount())
                    .append(",\"category\":\"").append(esc(s.getCategory()))
                    .append("\",\"cycle\":\"").append(esc(s.getCycle()))
                    .append("\",\"nextDate\":\"").append(s.getNextPaymentDate())
                    .append("\",\"active\":").append(s.isActive()).append("}");
        }
        json.append("\n  ]\n}");
        return json.toString();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
