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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.subzero.app.adapter.SubscriptionAdapter;
import com.subzero.app.adapter.UpcomingAdapter;
import com.subzero.app.db.StorageManager;
import com.subzero.app.model.Subscription;
import com.subzero.app.util.ExportHelper;
import com.subzero.app.util.LocaleHelper;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private View layoutDashboard, layoutSubs, layoutStats, layoutSettings;
    private FloatingActionButton fabAdd;

    // Dashboard views
    private TextView tvMonthlyTotal, tvActiveCount, tvYearlyTotal, tvCurrencySymbol;
    private TextView tvNoUpcoming;
    private RecyclerView rvUpcoming;

    // Subscription list views
    private RecyclerView rvSubscriptions;
    private TextView tvSubsEmpty, tvSubCount;

    // Stats views
    private TextView tvStatsMonthly, tvStatsYearly, tvStatsAvg, tvCurrencySetting;
    private LinearLayout layoutCategoryBars;
    private RecyclerView rvTopSubs;

    // Settings views
    private TextView tvLanguage, tvDarkMode, tvExport, tvAbout;

    private StorageManager store;
    private SubscriptionAdapter subAdapter;
    private UpcomingAdapter upcomingAdapter;
    private List<Subscription> allSubs = new ArrayList<>();
    private List<Subscription> upcomingSubs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.applyLanguage(this);
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

        // Dashboard
        tvMonthlyTotal = findViewById(R.id.tv_monthly_total);
        tvActiveCount = findViewById(R.id.tv_active_count);
        tvYearlyTotal = findViewById(R.id.tv_yearly_total);
        tvCurrencySymbol = findViewById(R.id.tv_currency_symbol);
        tvNoUpcoming = findViewById(R.id.tv_no_upcoming);
        rvUpcoming = findViewById(R.id.rv_upcoming);

        // Subscriptions
        rvSubscriptions = findViewById(R.id.rv_subscriptions);
        tvSubsEmpty = findViewById(R.id.tv_subs_empty);
        tvSubCount = findViewById(R.id.tv_sub_count);

        // Stats
        tvStatsMonthly = findViewById(R.id.tv_stats_monthly);
        tvStatsYearly = findViewById(R.id.tv_stats_yearly);
        tvStatsAvg = findViewById(R.id.tv_stats_avg);
        layoutCategoryBars = findViewById(R.id.layout_category_bars);
        rvTopSubs = findViewById(R.id.rv_top_subs);

        // Settings
        tvLanguage = findViewById(R.id.tv_language);
        tvCurrencySetting = findViewById(R.id.tv_currency_setting);
        tvLanguage = findViewById(R.id.tv_language);
        tvDarkMode = findViewById(R.id.tv_dark_mode);
        tvExport = findViewById(R.id.tv_export);
        tvAbout = findViewById(R.id.tv_about);

        // Setup upcoming RecyclerView
        rvUpcoming.setLayoutManager(new LinearLayoutManager(this));
        upcomingAdapter = new UpcomingAdapter(this, upcomingSubs);
        rvUpcoming.setAdapter(upcomingAdapter);

        // Setup subscription RecyclerView
        rvSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        subAdapter = new SubscriptionAdapter(this, allSubs, new SubscriptionAdapter.OnSubClickListener() {
            @Override
            public void onClick(Subscription sub) {
                Intent intent = new Intent(MainActivity.this, AddSubscriptionActivity.class);
                intent.putExtra("sub_id", sub.getId());
                startActivity(intent);
            }
            @Override
            public void onLongClick(Subscription sub) {
                showSubOptions(sub);
            }
        });
        rvSubscriptions.setAdapter(subAdapter);

        // Top subs RecyclerView (stats tab)
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
            String[] langs = {"中文", "English", "跟随系统"};
            String[] values = {"zh", "en", "auto"};
            new AlertDialog.Builder(this)
                    .setTitle("选择语言 / Language")
                    .setItems(langs, (d, w) -> {
                        LocaleHelper.setLanguage(this, values[w]);
                        tvLanguage.setText(langs[w]);
                        Toast.makeText(this, "重启应用后生效", Toast.LENGTH_SHORT).show();
                    }).show();
        });

        tvCurrencySetting.setOnClickListener(v -> {
            String[] currencies = {"¥ CNY（人民币）", "$ USD（美元）", "€ EUR（欧元）", "¥ JPY（日元）"};
            String[] values = {"CNY", "USD", "EUR", "JPY"};
            String[] symbols = {"¥", "$", "€", "¥"};
            new AlertDialog.Builder(this)
                    .setTitle("选择货币")
                    .setItems(currencies, (d, w) -> {
                        store.setSetting("currency", values[w]);
                        tvCurrencySetting.setText(symbols[w] + " " + values[w]);
                        loadDashboard();
                    }).show();
        });

        tvDarkMode.setOnClickListener(v -> {
            boolean isDark = "true".equals(store.getSetting("dark_mode"));
            store.setSetting("dark_mode", isDark ? "false" : "true");
            Toast.makeText(this, "切换后请重启应用", Toast.LENGTH_SHORT).show();
        });

        tvExport.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("导出数据").setMessage("将所有订阅数据导出为JSON文件？")
                    .setPositiveButton("导出", (d, w) -> ExportHelper.exportToFile(this))
                    .setNegativeButton("取消", null).show();
        });

        tvAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("关于 SubZero").setMessage(R.string.about_content)
                    .setPositiveButton("好的", null).show();
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
        String symbol = getSymbol();
        double monthly = store.getTotalMonthlySpend();
        int activeCount = store.getActiveSubscriptions().size();
        double yearly = store.getTotalYearlySpend();

        tvCurrencySymbol.setText(symbol);
        tvMonthlyTotal.setText(String.format(Locale.getDefault(), "%.2f", monthly));
        tvActiveCount.setText(String.valueOf(activeCount));
        tvYearlyTotal.setText(symbol + String.format(Locale.getDefault(), "%.0f", yearly));

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

        tvSubCount.setText("共 " + allSubs.size() + " 项");
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

        // Category breakdown bars
        layoutCategoryBars.removeAllViews();
        Map<String, Double> breakdown = store.getCategoryBreakdown();
        String[] catKeys = {"entertainment", "productivity", "health", "shopping", "food", "education", "other"};
        String[] catNames = {"影音娱乐", "效率工具", "健康健身", "购物快递", "餐饮美食", "学习教育", "其他"};
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
            header.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView label = new TextView(this);
            label.setText(catNames[i]);
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
            @Override public void onLongClick(Subscription sub) {
                showSubOptions(sub);
            }
        }));
    }

    private void showSubOptions(Subscription sub) {
        String[] options = {"编辑", sub.isActive() ? "暂停" : "恢复", "删除"};
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
                        Toast.makeText(this, sub.isActive() ? "已恢复" : "已暂停", Toast.LENGTH_SHORT).show();
                    } else if (w == 2) {
                        new AlertDialog.Builder(this)
                                .setTitle("确认删除").setMessage("确定要删除「" + sub.getName() + "」吗？")
                                .setPositiveButton("删除", (d2, w2) -> {
                                    store.deleteSubscription(sub.getId());
                                    loadSubscriptionList(); loadDashboard();
                                    Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                                }).setNegativeButton("取消", null).show();
                    }
                }).show();
    }

    private String getSymbol() {
        String c = store.getSetting("currency");
        return c.equals("USD") ? "$" : c.equals("EUR") ? "€" : c.equals("JPY") ? "¥" : "¥";
    }
}
