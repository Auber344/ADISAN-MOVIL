package com.example.adisan_app_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProveedoresActivity extends AppCompatActivity {

    private ProgressBar pbLoading;
    private LinearLayout layoutVacio;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvProveedores;
    private ProveedorAdapter adapter;
    private ImageView btnVolver;
    private MaterialButton btnNuevoProveedorHeader;
    private MaterialButton btnCrearPrimerProveedor;
    private EditText etBuscar;

    private List<Proveedor> proveedorListOriginal = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proveedores);

        initViews();
        setupRecyclerView();
        setupListeners();
        cargarProveedores();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProveedores();
    }

    private void initViews() {
        pbLoading = findViewById(R.id.pbLoadingProveedores);
        layoutVacio = findViewById(R.id.layoutProveedoresVacio);
        swipeRefresh = findViewById(R.id.swipeRefreshProveedores);
        rvProveedores = findViewById(R.id.rvProveedores);
        btnVolver = findViewById(R.id.btnVolverProveedores);
        btnNuevoProveedorHeader = findViewById(R.id.btnNuevoProveedorHeader);
        btnCrearPrimerProveedor = findViewById(R.id.btnCrearPrimerProveedor);
        etBuscar = findViewById(R.id.etBuscarProveedor);
    }

    private void setupRecyclerView() {
        rvProveedores.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProveedorAdapter(this, new ArrayList<>());
        rvProveedores.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnNuevoProveedorHeader != null) {
            btnNuevoProveedorHeader.setOnClickListener(v -> mostrarDialogoNuevoProveedor());
        }

        if (btnCrearPrimerProveedor != null) {
            btnCrearPrimerProveedor.setOnClickListener(v -> mostrarDialogoNuevoProveedor());
        }

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.blue_primary);
            swipeRefresh.setOnRefreshListener(this::cargarProveedores);
        }

        if (etBuscar != null) {
            etBuscar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filtrarProveedores(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void cargarProveedores() {
        if (swipeRefresh != null && !swipeRefresh.isRefreshing()) {
            if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService().getProveedores().enqueue(new Callback<ProveedorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProveedorResponse> call, @NonNull Response<ProveedorResponse> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                if (response.code() == 401) {
                    manejarSesionExpirada();
                    return;
                }

                List<Proveedor> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    lista = response.body().getData();
                }

                if (lista.isEmpty()) {
                    cargarProveedoresDirecto();
                } else {
                    proveedorListOriginal = lista;
                    mostrarLista(lista);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProveedorResponse> call, @NonNull Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                cargarProveedoresDirecto();
            }
        });
    }

    private void cargarProveedoresDirecto() {
        ApiClient.getApiService().getProveedoresDirectList().enqueue(new Callback<List<Proveedor>>() {
            @Override
            public void onResponse(@NonNull Call<List<Proveedor>> call, @NonNull Response<List<Proveedor>> response) {
                List<Proveedor> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    lista = response.body();
                }
                proveedorListOriginal = lista;
                mostrarLista(lista);
            }

            @Override
            public void onFailure(@NonNull Call<List<Proveedor>> call, @NonNull Throwable t) {
                proveedorListOriginal = new ArrayList<>();
                mostrarLista(new ArrayList<>());
            }
        });
    }

    private void mostrarLista(List<Proveedor> lista) {
        if (lista.isEmpty()) {
            if (rvProveedores != null) rvProveedores.setVisibility(View.GONE);
            if (layoutVacio != null) layoutVacio.setVisibility(View.VISIBLE);
        } else {
            if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
            if (rvProveedores != null) {
                rvProveedores.setVisibility(View.VISIBLE);
                adapter.setProveedores(lista);
            }
        }
    }

    private void filtrarProveedores(String query) {
        if (TextUtils.isEmpty(query)) {
            mostrarLista(proveedorListOriginal);
            return;
        }

        List<Proveedor> filtrados = new ArrayList<>();
        String q = query.toLowerCase();
        for (Proveedor p : proveedorListOriginal) {
            if ((p.getNombreEmpresa() != null && p.getNombreEmpresa().toLowerCase().contains(q)) ||
                    (p.getRuc() != null && p.getRuc().toLowerCase().contains(q)) ||
                    (p.getCorreo() != null && p.getCorreo().toLowerCase().contains(q))) {
                filtrados.add(p);
            }
        }
        mostrarLista(filtrados);
    }

    private void mostrarDialogoNuevoProveedor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nuevo_proveedor, null);
        builder.setView(dialogView);

        AlertDialog dialogProv = builder.create();
        if (dialogProv.getWindow() != null) {
            dialogProv.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextInputLayout tilNombre = dialogView.findViewById(R.id.tilNombreEmpresa);
        TextInputLayout tilRuc = dialogView.findViewById(R.id.tilRucProveedor);
        TextInputLayout tilTelefono = dialogView.findViewById(R.id.tilTelefonoProveedor);
        TextInputLayout tilCorreo = dialogView.findViewById(R.id.tilCorreoProveedor);

        TextInputEditText etNombre = dialogView.findViewById(R.id.etNombreEmpresa);
        TextInputEditText etRuc = dialogView.findViewById(R.id.etRucProveedor);
        TextInputEditText etTelefono = dialogView.findViewById(R.id.etTelefonoProveedor);
        TextInputEditText etCorreo = dialogView.findViewById(R.id.etCorreoProveedor);
        TextInputEditText etDireccion = dialogView.findViewById(R.id.etDireccionProveedor);

        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarProveedor);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnGuardarProveedor);

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialogProv.dismiss());
        }

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> {
                if (tilNombre != null) tilNombre.setError(null);
                if (tilRuc != null) tilRuc.setError(null);
                if (tilTelefono != null) tilTelefono.setError(null);
                if (tilCorreo != null) tilCorreo.setError(null);

                String nombre = etNombre != null && etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
                String ruc = etRuc != null && etRuc.getText() != null ? etRuc.getText().toString().trim() : "";
                String telefono = etTelefono != null && etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";
                String correo = etCorreo != null && etCorreo.getText() != null ? etCorreo.getText().toString().trim() : "";
                String direccion = etDireccion != null && etDireccion.getText() != null ? etDireccion.getText().toString().trim() : "";

                boolean esValido = true;

                if (TextUtils.isEmpty(nombre)) {
                    if (tilNombre != null) tilNombre.setError("Ingresa el nombre de la empresa");
                    esValido = false;
                }

                if (TextUtils.isEmpty(ruc) || ruc.length() != 11) {
                    if (tilRuc != null) tilRuc.setError("El RUC debe tener 11 dígitos");
                    esValido = false;
                }

                if (TextUtils.isEmpty(telefono) || telefono.length() < 7) {
                    if (tilTelefono != null) tilTelefono.setError("Ingresa un teléfono válido");
                    esValido = false;
                }

                if (TextUtils.isEmpty(correo) || !correo.contains("@")) {
                    if (tilCorreo != null) tilCorreo.setError("Ingresa un correo electrónico válido");
                    esValido = false;
                }

                if (esValido) {
                    Proveedor nuevoProv = new Proveedor();
                    nuevoProv.setNombreEmpresa(nombre);
                    nuevoProv.setRuc(ruc);
                    nuevoProv.setTelefono(telefono);
                    nuevoProv.setCorreo(correo);
                    nuevoProv.setDireccion(direccion);

                    ejecutarCrearProveedor(nuevoProv, dialogProv, btnGuardar);
                }
            });
        }

        dialogProv.show();
    }

    private void ejecutarCrearProveedor(Proveedor proveedor, AlertDialog dialogProv, MaterialButton btnGuardar) {
        if (btnGuardar != null) btnGuardar.setEnabled(false);

        ApiClient.getApiService().crearProveedor(proveedor).enqueue(new Callback<ProveedorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProveedorResponse> call, @NonNull Response<ProveedorResponse> response) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProveedoresActivity.this, "¡Proveedor registrado correctamente!", Toast.LENGTH_SHORT).show();
                    if (dialogProv != null && dialogProv.isShowing()) dialogProv.dismiss();
                    cargarProveedores();
                } else {
                    String msg = "Error al registrar proveedor (" + response.code() + ")";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            String errStr = response.errorBody().string();
                            ProveedorResponse errRes = new Gson().fromJson(errStr, ProveedorResponse.class);
                            if (errRes != null && errRes.getMessage() != null) {
                                msg = errRes.getMessage();
                            }
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(ProveedoresActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProveedorResponse> call, @NonNull Throwable t) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);
                Toast.makeText(ProveedoresActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void manejarSesionExpirada() {
        Toast.makeText(this, "Tu sesión ha expirado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(ProveedoresActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
