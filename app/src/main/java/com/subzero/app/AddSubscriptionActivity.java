package com.subzero.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import com.subzero.app.db.StorageManager;
import com.subzero.app.model.Subscription;
import com.subzero.app.util.DisplayHelper;
import com.subzero.app.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddSubscriptionActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private EditText etName, etAmount, etNotes;
    private Spinner spinnerCategory, spinnerCycle;
    private TextView tvNextDate;
    private android.widget.Button btnSave, btnDelete;

    private StorageManager store;
    private int editId = -1;
    private String selectedDate;
    private String[] catKeys, cycleKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // Build localized category and cycle lists
        catKeys = DisplayHelper.getCategoryKeys();
        String[] catNames = new String[catKeys.length];
        for (int i = 0; i < catKeys.length; i++) catNames[i] = DisplayHelper.getCategoryName(this, catKeys[i]);

        android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, catNames);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        cycleKeys = DisplayHelper.getCycleKeys();
        String[] cycleNames = new String[cycleKeys.length];
        for (int i = 0; i < cycleKeys.length; i++) cycleNames[i] = DisplayHelper.getCycleName(this, cycleKeys[i]);

        android.widget.ArrayAdapter<String> cycleAdapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cycleNames);
        cycleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCycle.setAdapter(cycleAdapter);

        // Default next date = today
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        updateDateDisplay();
        tvNextDate.setOnClickListener(v -> showDatePicker());

        editId = getIntent().getIntExtra("sub_id", -1);
        if (editId > 0) {
            loadSubscription(editId);
            toolbar.setTitle(getString(R.string.edit_subscription));
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

        for (int i = 0; i < catKeys.length; i++) {
            if (catKeys[i].equals(s.getCategory())) { spinnerCategory.setSelection(i); break; }
        }
        for (int i = 0; i < cycleKeys.length; i++) {
            if (cycleKeys[i].equals(s.getCycle())) { spinnerCycle.setSelection(i); break; }
        }
    }

    private void updateDateDisplay() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(selectedDate);
            java.util.Locale curLoc = getResources().getConfiguration().getLocales().get(0);
            String display;
            if (curLoc.getLanguage().equals(java.util.Locale.ENGLISH.getLanguage())) {
                display = new SimpleDateFormat("yyyy-MM-dd EEEE", java.util.Locale.ENGLISH).format(date);
            } else {
                display = new SimpleDateFormat("yyyy年MM月dd日 EEEE", java.util.Locale.CHINESE).format(date);
            }
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
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.add_sub_enter_name, Toast.LENGTH_SHORT).show(); return;
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(this, R.string.add_sub_enter_amount, Toast.LENGTH_SHORT).show(); return;
        }

        double amount = Double.parseDouble(amountStr);
        String category = catKeys[spinnerCategory.getSelectedItemPosition()];
        String cycle = cycleKeys[spinnerCycle.getSelectedItemPosition()];
        String notes = etNotes.getText().toString().trim();

        if (editId > 0) {
            Subscription s = store.getSubscriptionById(editId);
            if (s != null) {
                s.setName(name); s.setAmount(amount); s.setCategory(category);
                s.setCycle(cycle); s.setNextPaymentDate(selectedDate); s.setNotes(notes);
                store.updateSubscription(s);
                Toast.makeText(this, R.string.subscription_saved, Toast.LENGTH_SHORT).show();
            }
        } else {
            Subscription s = new Subscription(name, amount, category, cycle, selectedDate);
            s.setNotes(notes);
            store.insertSubscription(s);
            Toast.makeText(this, R.string.subscription_added, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.confirm_delete_msg))
                .setPositiveButton(getString(R.string.delete), (d, w) -> {
                    if (editId > 0) { store.deleteSubscription(editId);
                        Toast.makeText(this, R.string.subscription_deleted, Toast.LENGTH_SHORT).show(); }
                    finish();
                }).setNegativeButton(getString(R.string.cancel), null).show();
    }
}
