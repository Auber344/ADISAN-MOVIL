package com.example.adisan_app_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Inicio de sesión con Firebase Auth (correo + contraseña).
 * El rol y los datos del usuario se leen de su perfil en Firestore y se guardan en
 * SharedPreferences("AdisanPrefs") con las mismas claves que ya usan las demás pantallas.
 */
public class LoginActivity extends AppCompatActivity {

    static final String PREFS = "AdisanPrefs";
    private static final String KEY_RECORDAR = "recordar";
    private static final String KEY_CORREO = "correo";

    private TextInputLayout tilCorreo;
    private TextInputLayout tilPassword;
    private TextInputEditText etCorreo;
    private TextInputEditText etPassword;
    private MaterialCheckBox cbRecordarme;
    private MaterialButton btnLogin;
    private LinearProgressIndicator progressLogin;
    private TextView tvLoginError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (restaurarSesion()) return;

        setContentView(R.layout.activity_login);
        initViews();
        setupListeners();
    }

    /**
     * Entra directo si hay una sesión de Firebase activa y el usuario pidió mantenerla.
     * Las pantallas cierran sesión limpiando SharedPreferences, así que si las preferencias
     * ya no dicen "isLoggedIn" también se cierra la sesión de Firebase.
     */
    private boolean restaurarSesion() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        boolean sesionLocal = prefs.getBoolean("isLoggedIn", false);
        boolean recordar = prefs.getBoolean(KEY_RECORDAR, false);

        if (auth.getCurrentUser() != null && sesionLocal && recordar) {
            abrirDashboard();
            return true;
        }
        if (auth.getCurrentUser() != null) {
            auth.signOut();
        }
        if (sesionLocal) {
            String correo = prefs.getString(KEY_CORREO, null);
            prefs.edit().clear().putString(KEY_CORREO, correo).apply();
        }
        return false;
    }

    private void initViews() {
        tilCorreo = findViewById(R.id.tilCorreo);
        tilPassword = findViewById(R.id.tilPassword);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        cbRecordarme = findViewById(R.id.cbRecordarme);
        btnLogin = findViewById(R.id.btnLogin);
        progressLogin = findViewById(R.id.progressLogin);
        tvLoginError = findViewById(R.id.tvLoginError);

        String ultimoCorreo = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_CORREO, null);
        if (!TextUtils.isEmpty(ultimoCorreo)) {
            etCorreo.setText(ultimoCorreo);
            etPassword.requestFocus();
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> validarYIniciarSesion());

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validarYIniciarSesion();
                return true;
            }
            return false;
        });

        findViewById(R.id.tvOlvidastePassword).setOnClickListener(v -> mostrarRecuperarPassword());
        findViewById(R.id.tvIrRegistro).setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void validarYIniciarSesion() {
        tilCorreo.setError(null);
        tilPassword.setError(null);
        mostrarError(null);

        String correo = texto(etCorreo);
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean valido = true;
        if (TextUtils.isEmpty(correo)) {
            tilCorreo.setError(getString(R.string.error_empty_correo));
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError(getString(R.string.error_correo_invalido));
            valido = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_password));
            valido = false;
        }

        if (valido) {
            ejecutarLogin(correo, password);
        }
    }

    private void ejecutarLogin(String correo, String password) {
        setCargando(true);

        ApiClient.getApiService().login(new LoginRequest(correo, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                setCargando(false);
                LoginResponse res = response.body();
                if (response.isSuccessful() && res != null && res.isSuccess()) {
                    guardarSesion(LoginActivity.this, res, correo, cbRecordarme.isChecked());
                    abrirDashboard();
                    return;
                }
                mostrarError(mensajeDeError(response));
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setCargando(false);
                mostrarError(getString(R.string.error_conexion));
            }
        });
    }

    private String mensajeDeError(Response<LoginResponse> response) {
        if (response.errorBody() != null) {
            try {
                LoginResponse error = new Gson().fromJson(response.errorBody().string(), LoginResponse.class);
                if (error != null && !TextUtils.isEmpty(error.getMessage())) return error.getMessage();
            } catch (Exception ignored) {
            }
        }
        return "No se pudo iniciar sesión. Inténtalo de nuevo.";
    }

    private void mostrarRecuperarPassword() {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password_email, null);
        TextInputLayout tilEmail = vista.findViewById(R.id.tilResetCorreo);
        TextInputEditText etEmail = vista.findViewById(R.id.etResetCorreo);
        etEmail.setText(texto(etCorreo));

        androidx.appcompat.app.AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.recuperar_titulo)
                .setMessage(R.string.recuperar_mensaje)
                .setView(vista)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.recuperar_enviar, null)
                .create();

        dialogo.setOnShowListener(d -> dialogo.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String correo = texto(etEmail);
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                tilEmail.setError(getString(R.string.error_correo_invalido));
                return;
            }
            tilEmail.setError(null);
            v.setEnabled(false);
            // El mensaje es el mismo exista o no la cuenta, para no revelar qué correos están registrados
            FirebaseAuth.getInstance().sendPasswordResetEmail(correo).addOnCompleteListener(task -> {
                dialogo.dismiss();
                if (task.getException() instanceof com.google.firebase.FirebaseNetworkException) {
                    mostrarError(getString(R.string.error_conexion));
                    return;
                }
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.recuperar_titulo)
                        .setMessage(getString(R.string.recuperar_enviado, correo))
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            });
        }));
        dialogo.show();
    }

    static void guardarSesion(Context context, LoginResponse response, String correo, boolean recordar) {
        context.getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString("token", response.getToken())
                .putString("usuario", response.getUsuario())
                .putString("rol", response.getRol())
                .putInt("id", response.getId())
                .putString(KEY_CORREO, correo)
                .putBoolean(KEY_RECORDAR, recordar)
                .putBoolean("isLoggedIn", true)
                .apply();
    }

    private void abrirDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void setCargando(boolean cargando) {
        btnLogin.setEnabled(!cargando);
        btnLogin.setText(cargando ? R.string.btn_login_cargando : R.string.btn_login);
        etCorreo.setEnabled(!cargando);
        etPassword.setEnabled(!cargando);
        progressLogin.setVisibility(cargando ? View.VISIBLE : View.INVISIBLE);
    }

    private void mostrarError(String mensaje) {
        tvLoginError.setText(mensaje);
        tvLoginError.setVisibility(TextUtils.isEmpty(mensaje) ? View.GONE : View.VISIBLE);
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }
}
