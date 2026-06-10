package com.subguard.app.model;

public class Subscription {
    private int id;
    private String name;
    private double amount;
    private String category;
    private String cycle;
    private String nextPaymentDate;
    private String startDate;
    private String notes;
    private boolean active;

    public Subscription() {
        this.active = true;
        this.cycle = "monthly";
        this.amount = 0;
    }

    public Subscription(String name, double amount, String category, String cycle, String nextPaymentDate) {
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.cycle = cycle;
        this.nextPaymentDate = nextPaymentDate;
        this.active = true;
        this.notes = "";
        this.startDate = new java.text.SimpleDateFormat("yyyy-MM-dd",
                java.util.Locale.getDefault()).format(new java.util.Date());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCycle() { return cycle; }
    public void setCycle(String cycle) { this.cycle = cycle; }
    public String getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(String nextPaymentDate) { this.nextPaymentDate = nextPaymentDate; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /** Returns monthly equivalent amount */
    public double getMonthlyAmount() {
        switch (cycle) {
            case "weekly": return amount * 4.33;
            case "quarterly": return amount / 3;
            case "yearly": return amount / 12;
            default: return amount;
        }
    }

    public double getYearlyAmount() {
        return getMonthlyAmount() * 12;
    }
}
