package com.subzero.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.subzero.app.adapter.SubscriptionAdapter;
import com.subzero.app.adapter.UpcomingAdapter;
import com.subzero.app.db.StorageManager;
import com.subzero.app.model.Subscription;
import com.subzero.app.util.DisplayHelper;
import com.subzero.app.util.ExportHelper;
import com.subzero.app.util.LocaleHelper;

import java.util.*;

public class MainActivity extends BaseActivity {

    private BottomNavigationView bottomNav;
    private View layoutDashboard, layoutSubs, layoutStats, layoutSettings;
    private FloatingActionButton fabAdd;

    private TextView tvMonthlyTotal, tvActiveCount, tvYearlyTotal, tvCurrencySymbol;
    private TextView tvNoUpcoming;
    private RecyclerView rvUpcoming;

    private RecyclerView rvSubscriptions;
    private TextView tvSubsEmpty, tvSubCount;

    private TextView tvStatsMonthly, tvStatsYearly, tvStatsAvg, tvCurrencySetting;
    private LinearLayout layoutCategoryBars;
    private RecyclerView rvTopSubs;

    private TextView tvLanguage, tvDarkMode, tvExport, tvAbout;

    private StorageManager store;
    private SubscriptionAdapter subAdapter;
    private UpcomingAdapter upcomingAdapter;
    private List<Subscription> allSubs = new ArrayList<>();
    private List<Subscription> upcomingSubs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        store = StorageManager.getInstance(this);
        initViews();
        setupBottomNav();
        loadDashboard();
        setupSettings();
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bottom_nav);
        fabAdd = findViewById(R.id.fab_add);

        layoutDashboard = findViewById(R.id.layout_dashboard);
        layoutSubs = findViewById(R.id.layout_subscriptions);
        layoutStats = findViewById(R.id.layout_stats);
        layoutSettings = findViewById(R.id.layout_settings);

        tvMonthlyTotal = findViewById(R.id.tv_monthly_total);
        tvActiveCount = findViewById(R.id.tv_active_count);
        tvYearlyTotal = findViewById(R.id.tv_yearly_total);
        tvCurrencySymbol = findViewById(R.id.tv_currency_symbol);
        tvNoUpcoming = findViewById(R.id.tv_no_upcoming);
        rvUpcoming = findViewById(R.id.rv_upcoming);

        rvSubscriptions = findViewById(R.id.rv_subscriptions);
        tvSubsEmpty = findViewById(R.id.tv_subs_empty);
        tvSubCount = findViewById(R.id.tv_sub_count);

        tvStatsMonthly = findViewById(R.id.tv_stats_monthly);
        tvStatsYearly = findViewById(R.id.tv_stats_yearly);
        tvStatsAvg = findViewById(R.id.tv_stats_avg);
        layoutCategoryBars = findViewById(R.id.layout_category_bars);
        rvTopSubs = findViewById(R.id.rv_top_subs);

        tvLanguage = findViewById(R.id.tv_language);
        tvCurrencySetting = findViewById(R.id.tv_currency_setting);
        tvDarkMode = findViewById(R.id.tv_dark_mode);
        tvExport = findViewById(R.id.tv_export);
        tvAbout = findViewById(R.id.tv_about);

        rvUpcoming.setLayoutManager(new LinearLayoutManager(this));
        upcomingAdapter = new UpcomingAdapter(this, upcomingSubs);
        rvUpcoming.setAdapter(upcomingAdapter);

        rvSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        subAdapter = new SubscriptionAdapter(this, allSubs, new SubscriptionAdapter.OnSubClickListener() {
            @Override public void onClick(Subscription sub) {
                Intent intent = new Intent(MainActivity.this, AddSubscriptionActivity.class);
                intent.putExtra("sub_id", sub.getId());
                startActivity(intent);
            }
            @Override public void onLongClick(Subscription sub) { showSubOptions(sub); }
        });
        rvSubscriptions.setAdapter(subAdapter);

        rvTopSubs.setLayoutManager(new LinearLayoutManager(this));

        fabAdd.setOnClickListener(v -> startActivity(
                new Intent(MainActivity.this, AddSubscriptionActivity.class)));
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            layoutDashboard.setVisibility(id == R.id.nav_dashboard ? View.VISIBLE : View.GONE);
            layoutSubs.setVisibility(id == R.id.nav_subscriptions ? View.VISIBLE : View.GONE);
            layoutStats.setVisibility(id == R.id.nav_stats ? View.VISIBLE : View.GONE);
            layoutSettings.setVisibility(id == R.id.nav_settings ? View.VISIBLE : View.GONE);

            if (id == R.id.nav_dashboard) loadDashboard();
            else if (id == R.id.nav_subscriptions) loadSubscriptionList();
            else if (id == R.id.nav_stats) loadStats();
            return true;
        });
    }

    private void setupSettings() {
        tvLanguage.setText(LocaleHelper.getLanguageDisplayName(this));
        tvLanguage.setOnClickListener(v -> {
            String[] langs = {getString(R.string.language_zh), getString(R.string.language_en), getString(R.string.language_auto)};
            String[] values = {"zh", "en", "auto"};
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.select_language_title))
                    .setItems(langs, (d, w) -> {
                        if (!values[w].equals(LocaleHelper.getLanguage(this))) {
                            switchLanguage(values[w]);
                        }
                    }).show();
        });

        tvCurrencySetting.setOnClickListener(v -> {
            String cny = getString(R.string.currency_cny);
            String usd = getString(R.string.currency_usd);
            String eur = getString(R.string.currency_eur);
            String jpy = getString(R.string.currency_jpy);
            String[] currencies = {cny, usd, eur, jpy};
            String[] values = {"CNY", "USD", "EUR", "JPY"};
            String[] symbols = {"¥", "$", "€", "¥"};
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.select_currency_title))
                    .setItems(currencies, (d, w) -> {
                        store.setSetting("currency", values[w]);
                        tvCurrencySetting.setText(symbols[w] + " " + values[w]);
                        loadDashboard();
                    }).show();
        });

        tvDarkMode.setOnClickListener(v -> {
            boolean isDark = "true".equals(store.getSetting("dark_mode"));
            store.setSetting("dark_mode", isDark ? "false" : "true");
            Toast.makeText(this, R.string.dark_mode_restart, Toast.LENGTH_SHORT).show();
        });

        tvExport.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.export_dialog_title))
                    .setMessage(getString(R.string.export_dialog_msg))
                    .setPositiveButton(getString(R.string.export_ok), (d, w) -> ExportHelper.exportToFile(this))
                    .setNegativeButton(getString(R.string.cancel), null).show();
        });

        tvAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.about_app))
                    .setMessage(R.string.about_content)
                    .setPositiveButton(getString(R.string.ok), null).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        store.reload();
        if (layoutDashboard.getVisibility() == View.VISIBLE) loadDashboard();
        if (layoutSubs.getVisibility() == View.VISIBLE) loadSubscriptionList();
        if (layoutStats.getVisibility() == View.VISIBLE) loadStats();
    }

    private void loadDashboard() {
        double monthly = store.getTotalMonthlySpend();
        int activeCount = store.getActiveSubscriptions().size();
        double yearly = store.getTotalYearlySpend();

        tvCurrencySymbol.setText(getSymbol());
        tvMonthlyTotal.setText(String.format(Locale.getDefault(), "%.2f", monthly));
        tvActiveCount.setText(String.valueOf(activeCount));
        tvYearlyTotal.setText(getSymbol() + String.format(Locale.getDefault(), "%.0f", yearly));

        int reminderDays = Integer.parseInt(store.getSetting("reminder_days"));
        upcomingSubs.clear();
        upcomingSubs.addAll(store.getUpcomingRenewals(reminderDays));
        upcomingAdapter.notifyDataSetChanged();

        tvNoUpcoming.setVisibility(upcomingSubs.isEmpty() ? View.VISIBLE : View.GONE);
        rvUpcoming.setVisibility(upcomingSubs.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void loadSubscriptionList() {
        allSubs.clear();
        allSubs.addAll(store.getAllSubscriptions());
        subAdapter.notifyDataSetChanged();

        tvSubCount.setText(String.format(getString(R.string.total_count_format), allSubs.size()));
        tvSubsEmpty.setVisibility(allSubs.isEmpty() ? View.VISIBLE : View.GONE);
        rvSubscriptions.setVisibility(allSubs.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void loadStats() {
        String symbol = getSymbol();
        double monthly = store.getTotalMonthlySpend();
        double yearly = store.getTotalYearlySpend();
        double avg = store.getAllSubscriptions().isEmpty() ? 0 :
                yearly / Math.max(store.getAllSubscriptions().size(), 1);

        tvStatsMonthly.setText(symbol + String.format(Locale.getDefault(), "%.0f", monthly));
        tvStatsYearly.setText(symbol + String.format(Locale.getDefault(), "%.0f", yearly));
        tvStatsAvg.setText(symbol + String.format(Locale.getDefault(), "%.0f", avg));

        // Category breakdown bars — use DisplayHelper for localized names
        layoutCategoryBars.removeAllViews();
        Map<String, Double> breakdown = store.getCategoryBreakdown();
        String[] catKeys = DisplayHelper.getCategoryKeys();
        int[] catColors = {Color.parseColor("#FF5722"), Color.parseColor("#2196F3"),
                Color.parseColor("#4CAF50"), Color.parseColor("#FF9800"),
                Color.parseColor("#E91E63"), Color.parseColor("#9C27B0"),
                Color.parseColor("#607D8B")};

        double maxVal = 0;
        for (Map.Entry<String, Double> e : breakdown.entrySet()) {
            maxVal = Math.max(maxVal, e.getValue());
        }

        for (int i = 0; i < catKeys.length; i++) {
            Double val = breakdown.getOrDefault(catKeys[i], 0.0);
            if (val == 0) continue;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, 6, 0, 6);

            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);

            TextView label = new TextView(this);
            label.setText(DisplayHelper.getCategoryName(this, catKeys[i]));
            label.setTextSize(12);
            label.setTextColor(Color.parseColor("#49454F"));
            label.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            header.addView(label);

            TextView value = new TextView(this);
            value.setText(symbol + String.format(Locale.getDefault(), "%.0f", val));
            value.setTextSize(12);
            value.setTextColor(Color.parseColor("#1C1B1F"));
            header.addView(value);

            row.addView(header);

            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            bar.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 12));
            bar.setProgress((int) (maxVal > 0 ? (val / maxVal * 100) : 0));
            bar.setProgressTintList(android.content.res.ColorStateList.valueOf(catColors[i]));
            row.addView(bar);

            layoutCategoryBars.addView(row);
        }

        // Top spending subs
        List<Subscription> sorted = new ArrayList<>(store.getActiveSubscriptions());
        sorted.sort((a, b) -> Double.compare(b.getMonthlyAmount(), a.getMonthlyAmount()));
        rvTopSubs.setAdapter(new SubscriptionAdapter(this, sorted, new SubscriptionAdapter.OnSubClickListener() {
            @Override public void onClick(Subscription sub) {
                Intent intent = new Intent(MainActivity.this, AddSubscriptionActivity.class);
                intent.putExtra("sub_id", sub.getId());
                startActivity(intent);
            }
            @Override public void onLongClick(Subscription sub) { showSubOptions(sub); }
        }));
    }

    private void showSubOptions(Subscription sub) {
        String[] options = {getString(R.string.edit_option),
                sub.isActive() ? getString(R.string.pause_option) : getString(R.string.resume_option),
                getString(R.string.delete)};
        new AlertDialog.Builder(this)
                .setTitle(sub.getName())
                .setItems(options, (d, w) -> {
                    if (w == 0) {
                        Intent intent = new Intent(MainActivity.this, AddSubscriptionActivity.class);
                        intent.putExtra("sub_id", sub.getId());
                        startActivity(intent);
                    } else if (w == 1) {
                        sub.setActive(!sub.isActive());
                        store.updateSubscription(sub);
                        loadSubscriptionList();
                        Toast.makeText(this, sub.isActive()
                                ? R.string.subscription_resumed
                                : R.string.subscription_paused, Toast.LENGTH_SHORT).show();
                    } else if (w == 2) {
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.confirm_delete))
                                .setMessage(String.format(getString(R.string.confirm_delete_sub_msg), sub.getName()))
                                .setPositiveButton(getString(R.string.delete), (d2, w2) -> {
                                    store.deleteSubscription(sub.getId());
                                    loadSubscriptionList(); loadDashboard();
                                    Toast.makeText(this, R.string.subscription_deleted, Toast.LENGTH_SHORT).show();
                                }).setNegativeButton(getString(R.string.cancel), null).show();
                    }
                }).show();
    }

    private String getSymbol() {
        String c = store.getSetting("currency");
        if (c.equals("USD")) return "$";
        if (c.equals("EUR")) return "€";
        if (c.equals("JPY")) return "¥";
        return "¥";
    }
}
