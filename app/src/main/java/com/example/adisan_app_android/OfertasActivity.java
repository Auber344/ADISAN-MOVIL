package com.example.adisan_app_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfertasActivity extends AppCompatActivity {

    private ProgressBar pbLoading;
    private LinearLayout layoutVacio;
    private LinearLayout layoutError;
    private TextView tvMensajeError;
    private MaterialButton btnReintentar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvOfertas;
    private OfertaAdapter adapter;
    private ImageView btnVolver;
    private MaterialButton btnGestionAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ofertas);

        initViews();
        setupRecyclerView();
        setupListeners();
        cargarOfertasCatalogo();
    }

    private void initViews() {
        pbLoading = findViewById(R.id.pbLoadingOfertas);
        layoutVacio = findViewById(R.id.layoutVacioOfertas);
        layoutError = findViewById(R.id.layoutErrorOfertas);
        tvMensajeError = findViewById(R.id.tvMensajeErrorOfertas);
        btnReintentar = findViewById(R.id.btnReintentarOfertas);
        swipeRefresh = findViewById(R.id.swipeRefreshOfertas);
        rvOfertas = findViewById(R.id.rvOfertas);
        btnVolver = findViewById(R.id.btnVolverOfertas);
        btnGestionAdmin = findViewById(R.id.btnGestionOfertasAdmin);

        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        String rol = preferences.getString("rol", "cliente");
        if ("admin".equalsIgnoreCase(rol) || "vendedor".equalsIgnoreCase(rol)) {
            if (btnGestionAdmin != null) {
                btnGestionAdmin.setVisibility(View.VISIBLE);
                btnGestionAdmin.setOnClickListener(v -> {
                    Intent intent = new Intent(OfertasActivity.this, GestionOfertasActivity.class);
                    startActivity(intent);
                });
            }
        }
    }

    private void setupRecyclerView() {
        rvOfertas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OfertaAdapter(this, new ArrayList<>());
        rvOfertas.setAdapter(adapter);

        adapter.setOnOfertaActionListener(oferta -> mostrarDialogoCantidadOferta(oferta));
    }

    private void setupListeners() {
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnReintentar != null) {
            btnReintentar.setOnClickListener(v -> cargarOfertasCatalogo());
        }

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.blue_primary);
            swipeRefresh.setOnRefreshListener(this::cargarOfertasCatalogo);
        }
    }

    private void cargarOfertasCatalogo() {
        if (swipeRefresh != null && !swipeRefresh.isRefreshing()) {
            mostrarEstadoCarga();
        }

        ApiClient.getApiService().getOfertasCatalogo().enqueue(new Callback<OfertaResponse>() {
            @Override
            public void onResponse(@NonNull Call<OfertaResponse> call, @NonNull Response<OfertaResponse> response) {
                ocultarSwipeRefresh();

                if (response.code() == 401) {
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Oferta> lista = response.body().getData();
                    if (lista != null && !lista.isEmpty()) {
                        mostrarListaOfertas(lista);
                    } else {
                        cargarOfertasCatalogoDirectList();
                    }
                } else {
                    cargarOfertasCatalogoDirectList();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OfertaResponse> call, @NonNull Throwable t) {
                ocultarSwipeRefresh();
                cargarOfertasCatalogoDirectList();
            }
        });
    }

    private void cargarOfertasCatalogoDirectList() {
        ApiClient.getApiService().getOfertasCatalogoDirectList().enqueue(new Callback<List<Oferta>>() {
            @Override
            public void onResponse(@NonNull Call<List<Oferta>> call, @NonNull Response<List<Oferta>> response) {
                ocultarSwipeRefresh();

                if (response.isSuccessful() && response.body() != null) {
                    List<Oferta> lista = response.body();
                    if (lista != null && !lista.isEmpty()) {
                        mostrarListaOfertas(lista);
                    } else {
                        mostrarEstadoVacio();
                    }
                } else {
                    mostrarEstadoError("No se pudieron cargar las ofertas.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Oferta>> call, @NonNull Throwable t) {
                ocultarSwipeRefresh();
                mostrarEstadoError("Error de conexión al cargar ofertas.");
            }
        });
    }

    private void mostrarDialogoCantidadOferta(Oferta oferta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cantidad_oferta, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextInputEditText etCantidad = dialogView.findViewById(R.id.etCantidadOferta);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarCantidadOferta);
        MaterialButton btnAgregar = dialogView.findViewById(R.id.btnAgregarOfertaFinal);

        if (btnCancelar != null) btnCancelar.setOnClickListener(v -> dialog.dismiss());

        if (btnAgregar != null) {
            btnAgregar.setOnClickListener(v -> {
                String cantStr = etCantidad != null && etCantidad.getText() != null ? etCantidad.getText().toString().trim() : "1";
                int cantidad = 1;
                try {
                    cantidad = Integer.parseInt(cantStr);
                } catch (Exception ignored) {}

                if (cantidad <= 0) {
                    Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                CarritoManager.getInstance().agregarOferta(oferta, cantidad);
                Toast.makeText(this, "Oferta agregada al carrito.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void mostrarEstadoCarga() {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (rvOfertas != null) rvOfertas.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
    }

    private void ocultarSwipeRefresh() {
        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void mostrarListaOfertas(List<Oferta> lista) {
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);

        if (rvOfertas != null && lista != null) {
            rvOfertas.setVisibility(View.VISIBLE);
            if (adapter == null) {
                adapter = new OfertaAdapter(this, lista);
                rvOfertas.setAdapter(adapter);
            } else {
                adapter.setOfertaList(lista);
            }
        }
    }

    private void mostrarEstadoVacio() {
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (rvOfertas != null) rvOfertas.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.VISIBLE);
    }

    private void mostrarEstadoError(String mensaje) {
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (rvOfertas != null) rvOfertas.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
        if (layoutError != null) {
            layoutError.setVisibility(View.VISIBLE);
            if (tvMensajeError != null) {
                tvMensajeError.setText(mensaje);
            }
        }
    }

    private void manejarSesionExpirada() {
        Toast.makeText(this, "Tu sesión ha expirado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(OfertasActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
