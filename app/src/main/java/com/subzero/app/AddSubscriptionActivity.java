package com.subzero.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import com.subzero.app.db.StorageManager;
import com.subzero.app.model.Subscription;
import com.subzero.app.util.LocaleHelper;
import com.subzero.app.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddSubscriptionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private EditText etName, etAmount, etNotes;
    private Spinner spinnerCategory, spinnerCycle;
    private TextView tvNextDate;
    private android.widget.Button btnSave, btnDelete;

    private StorageManager store;
    private int editId = -1;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.applyLanguage(this);
        setContentView(R.layout.activity_add_subscription);

        store = StorageManager.getInstance(this);
        NotificationHelper.createChannel(this);

        toolbar = findViewById(R.id.toolbar);
        etName = findViewById(R.id.et_name);
        etAmount = findViewById(R.id.et_amount);
        etNotes = findViewById(R.id.et_notes);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerCycle = findViewById(R.id.spinner_cycle);
        tvNextDate = findViewById(R.id.tv_next_date);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);

        toolbar.setNavigationOnClickListener(v -> finish());

        // Category spinner
        String[] categories = {"影音娱乐", "效率工具", "健康健身", "购物快递", "餐饮美食", "学习教育", "其他"};
        String[] catValues = {"entertainment", "productivity", "health", "shopping", "food", "education", "other"};
        android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // Cycle spinner
        String[] cycles = {"每月", "每年", "每周", "每季"};
        String[] cycleValues = {"monthly", "yearly", "weekly", "quarterly"};
        android.widget.ArrayAdapter<String> cycleAdapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cycles);
        cycleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCycle.setAdapter(cycleAdapter);

        // Default next date = today
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        updateDateDisplay();
        tvNextDate.setOnClickListener(v -> showDatePicker());

        editId = getIntent().getIntExtra("sub_id", -1);
        if (editId > 0) {
            loadSubscription(editId);
            toolbar.setTitle("编辑订阅");
            btnDelete.setVisibility(View.VISIBLE);
        }

        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadSubscription(int id) {
        Subscription s = store.getSubscriptionById(id);
        if (s == null) return;
        etName.setText(s.getName());
        etAmount.setText(String.valueOf(s.getAmount()));
        etNotes.setText(s.getNotes());
        selectedDate = s.getNextPaymentDate();
        updateDateDisplay();

        String[] catValues = {"entertainment", "productivity", "health", "shopping", "food", "education", "other"};
        String[] cycleValues = {"monthly", "yearly", "weekly", "quarterly"};
        for (int i = 0; i < catValues.length; i++) {
            if (catValues[i].equals(s.getCategory())) { spinnerCategory.setSelection(i); break; }
        }
        for (int i = 0; i < cycleValues.length; i++) {
            if (cycleValues[i].equals(s.getCycle())) { spinnerCycle.setSelection(i); break; }
        }
    }

    private void updateDateDisplay() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(selectedDate);
            String display = new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINESE).format(date);
            tvNextDate.setText(display);
        } catch (Exception e) { tvNextDate.setText(selectedDate); }
    }

    private void showDatePicker() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(selectedDate));
            new android.app.DatePickerDialog(this, (v, y, m, d) -> {
                Calendar sc = Calendar.getInstance(); sc.set(y, m, d);
                selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(sc.getTime());
                updateDateDisplay();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        } catch (Exception e) {
            Calendar cal = Calendar.getInstance();
            new android.app.DatePickerDialog(this, (v, y, m, d) -> {
                Calendar sc = Calendar.getInstance(); sc.set(y, m, d);
                selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(sc.getTime());
                updateDateDisplay();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        }
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        if (name.isEmpty()) { Toast.makeText(this, "请输入订阅名称", Toast.LENGTH_SHORT).show(); return; }
        if (amountStr.isEmpty()) { Toast.makeText(this, "请输入费用金额", Toast.LENGTH_SHORT).show(); return; }

        double amount = Double.parseDouble(amountStr);
        String[] catValues = {"entertainment", "productivity", "health", "shopping", "food", "education", "other"};
        String[] cycleValues = {"monthly", "yearly", "weekly", "quarterly"};
        String category = catValues[spinnerCategory.getSelectedItemPosition()];
        String cycle = cycleValues[spinnerCycle.getSelectedItemPosition()];
        String notes = etNotes.getText().toString().trim();

        if (editId > 0) {
            Subscription s = store.getSubscriptionById(editId);
            if (s != null) {
                s.setName(name); s.setAmount(amount); s.setCategory(category);
                s.setCycle(cycle); s.setNextPaymentDate(selectedDate); s.setNotes(notes);
                store.updateSubscription(s);
                Toast.makeText(this, "订阅已更新", Toast.LENGTH_SHORT).show();
            }
        } else {
            Subscription s = new Subscription(name, amount, category, cycle, selectedDate);
            s.setNotes(notes);
            store.insertSubscription(s);
            Toast.makeText(this, "订阅添加成功", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("确认删除").setMessage("确定要删除该订阅吗？")
                .setPositiveButton("删除", (d, w) -> {
                    if (editId > 0) { store.deleteSubscription(editId);
                        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show(); }
                    finish();
                }).setNegativeButton("取消", null).show();
    }
}
