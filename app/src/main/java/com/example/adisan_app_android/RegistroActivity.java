package com.example.adisan_app_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Registro de clientes desde el login. Solo crea cuentas con rol "cliente";
 * el personal (admin, vendedor, chofer) lo sigue creando el administrador.
 */
public class RegistroActivity extends AppCompatActivity {

    private TextInputLayout tilNombres, tilApellidos, tilDni, tilTelefono, tilCorreo, tilPassword, tilConfirmar;
    private TextInputEditText etNombres, etApellidos, etDni, etTelefono, etDireccion, etCorreo, etPassword, etConfirmar;
    private MaterialButton btnCrearCuenta;
    private LinearProgressIndicator progreso;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        tilNombres = findViewById(R.id.tilNombresReg);
        tilApellidos = findViewById(R.id.tilApellidosReg);
        tilDni = findViewById(R.id.tilDniReg);
        tilTelefono = findViewById(R.id.tilTelefonoReg);
        tilCorreo = findViewById(R.id.tilCorreoReg);
        tilPassword = findViewById(R.id.tilPasswordReg);
        tilConfirmar = findViewById(R.id.tilConfirmarReg);
        etNombres = findViewById(R.id.etNombresReg);
        etApellidos = findViewById(R.id.etApellidosReg);
        etDni = findViewById(R.id.etDniReg);
        etTelefono = findViewById(R.id.etTelefonoReg);
        etDireccion = findViewById(R.id.etDireccionReg);
        etCorreo = findViewById(R.id.etCorreoReg);
        etPassword = findViewById(R.id.etPasswordReg);
        etConfirmar = findViewById(R.id.etConfirmarReg);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        progreso = findViewById(R.id.progressRegistro);
        tvError = findViewById(R.id.tvRegistroError);

        findViewById(R.id.btnVolverRegistro).setOnClickListener(v -> finish());
        findViewById(R.id.tvIrLogin).setOnClickListener(v -> finish());
        btnCrearCuenta.setOnClickListener(v -> validarYRegistrar());
        etConfirmar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validarYRegistrar();
                return true;
            }
            return false;
        });
    }

    private void validarYRegistrar() {
        TextInputLayout[] campos = {tilNombres, tilApellidos, tilDni, tilTelefono, tilCorreo, tilPassword, tilConfirmar};
        for (TextInputLayout t : campos) t.setError(null);
        mostrarError(null);

        String nombres = texto(etNombres);
        String apellidos = texto(etApellidos);
        String dni = texto(etDni);
        String telefono = texto(etTelefono);
        String correo = texto(etCorreo);
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmar = etConfirmar.getText() != null ? etConfirmar.getText().toString() : "";

        boolean valido = true;
        if (nombres.isEmpty()) valido = error(tilNombres, R.string.registro_campo_obligatorio);
        if (apellidos.isEmpty()) valido = error(tilApellidos, R.string.registro_campo_obligatorio);
        if (!dni.matches("\\d{8}")) valido = error(tilDni, R.string.registro_dni_invalido);
        if (!telefono.replaceAll("[\\s+-]", "").matches("\\d{6,15}")) valido = error(tilTelefono, R.string.registro_telefono_invalido);
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) valido = error(tilCorreo, R.string.error_correo_invalido);
        if (password.length() < 6) valido = error(tilPassword, R.string.registro_password_corta);
        else if (!password.equals(confirmar)) valido = error(tilConfirmar, R.string.registro_password_distinta);
        if (!valido) return;

        Usuario cliente = new Usuario();
        cliente.setNombres(nombres);
        cliente.setApellidos(apellidos);
        cliente.setDni(dni);
        cliente.setTelefono(telefono);
        cliente.setDireccion(texto(etDireccion));
        cliente.setCorreo(correo);
        cliente.setUsuario(correo);
        cliente.setPassword(password);
        cliente.setRol("cliente");
        registrar(cliente, correo);
    }

    private void registrar(Usuario cliente, String correo) {
        setCargando(true);
        ApiClient.getApiService().registrarCliente(cliente).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                setCargando(false);
                LoginResponse res = response.body();
                if (response.isSuccessful() && res != null && res.isSuccess()) {
                    LoginActivity.guardarSesion(RegistroActivity.this, res, correo, true);
                    Intent intent = new Intent(RegistroActivity.this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return;
                }
                mostrarError(RespuestaApi.mensajeError(response, "No se pudo crear la cuenta. Inténtalo de nuevo."));
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setCargando(false);
                mostrarError(getString(R.string.error_conexion));
            }
        });
    }

    private boolean error(TextInputLayout campo, int mensaje) {
        campo.setError(getString(mensaje));
        return false;
    }

    private void setCargando(boolean cargando) {
        btnCrearCuenta.setEnabled(!cargando);
        btnCrearCuenta.setText(cargando ? R.string.registro_cargando : R.string.registro_boton);
        progreso.setVisibility(cargando ? View.VISIBLE : View.INVISIBLE);
    }

    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(TextUtils.isEmpty(mensaje) ? View.GONE : View.VISIBLE);
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }
}
