package com.subzero.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends BaseActivity {

    private TextInputLayout tilUsername, tilPassword;
    private EditText etUsername, etPassword;
    private CheckBox cbRemember;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        cbRemember = findViewById(R.id.cb_remember);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);

        // Load saved credentials
        SharedPreferences prefs = getSharedPreferences("subzero_login", MODE_PRIVATE);
        etUsername.setText(prefs.getString("saved_username", ""));
        etPassword.setText(prefs.getString("saved_password", ""));
        cbRemember.setChecked(prefs.getBoolean("remember", false));

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty()) {
                tilUsername.setError(getString(R.string.error_username_required));
                return;
            }
            tilUsername.setError(null);

            if (password.isEmpty()) {
                tilPassword.setError(getString(R.string.error_password_required));
                return;
            }
            if (password.length() < 3) {
                tilPassword.setError(getString(R.string.error_password_short));
                return;
            }
            tilPassword.setError(null);

            // Retrieve stored credentials
            String savedUser = prefs.getString("account_username", "");
            String savedPass = prefs.getString("account_password", "");

            if (savedUser.isEmpty() || savedPass.isEmpty()) {
                // First time — save as register
                prefs.edit()
                        .putString("account_username", username)
                        .putString("account_password", password)
                        .apply();
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
            } else if (!savedUser.equals(username) || !savedPass.equals(password)) {
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            // Save credentials if Remember Me checked
            if (cbRemember.isChecked()) {
                prefs.edit()
                        .putString("saved_username", username)
                        .putString("saved_password", password)
                        .putBoolean("remember", true)
                        .putBoolean("logged_in", true)
                        .apply();
            } else {
                prefs.edit()
                        .remove("saved_username")
                        .remove("saved_password")
                        .putBoolean("remember", false)
                        .putBoolean("logged_in", true)
                        .apply();
            }

            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
            goToMain();
        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
