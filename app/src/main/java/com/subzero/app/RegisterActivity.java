package com.subzero.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends BaseActivity {

    private ImageView ivBack;
    private TextInputLayout tilUsername, tilPassword, tilConfirmPassword, tilNickname;
    private EditText etUsername, etPassword, etConfirmPassword, etNickname;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ivBack = findViewById(R.id.iv_back);
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        tilNickname = findViewById(R.id.til_nickname);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etNickname = findViewById(R.id.et_nickname);
        btnRegister = findViewById(R.id.btn_register);

        ivBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();
            String nickname = etNickname.getText().toString().trim();

            if (username.isEmpty()) { tilUsername.setError(getString(R.string.error_username_required)); return; }
            tilUsername.setError(null);
            if (username.length() < 3) { tilUsername.setError(getString(R.string.error_username_short)); return; }
            tilUsername.setError(null);
            if (password.isEmpty()) { tilPassword.setError(getString(R.string.error_password_required)); return; }
            tilPassword.setError(null);
            if (password.length() < 3) { tilPassword.setError(getString(R.string.error_password_short)); return; }
            tilPassword.setError(null);
            if (!password.equals(confirm)) { tilConfirmPassword.setError(getString(R.string.error_password_mismatch)); return; }
            tilConfirmPassword.setError(null);
            if (nickname.isEmpty()) nickname = username;

            // Check username uniqueness
            SharedPreferences prefs = getSharedPreferences("subzero_login", MODE_PRIVATE);
            String existingUser = prefs.getString("account_username", "");

            if (username.equals(existingUser)) {
                tilUsername.setError(getString(R.string.error_username_taken));
                return;
            }

            prefs.edit()
                    .putString("account_username", username)
                    .putString("account_password", password)
                    .putString("account_nickname", nickname)
                    .putBoolean("logged_in", true)
                    .apply();

            Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
