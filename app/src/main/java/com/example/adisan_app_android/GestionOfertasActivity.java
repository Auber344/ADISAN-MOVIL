package com.example.adisan_app_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionOfertasActivity extends AppCompatActivity {

    private ProgressBar pbLoading;
    private LinearLayout layoutVacio;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvGestion;
    private GestionOfertaAdapter adapter;
    private ImageView btnVolver;
    private MaterialButton btnNuevaOferta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        String rol = preferences.getString("rol", "cliente");
        if (!"admin".equalsIgnoreCase(rol) && !"vendedor".equalsIgnoreCase(rol)) {
            Toast.makeText(this, "Acceso denegado. Se requiere rol Admin o Vendedor.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_gestion_ofertas);

        initViews();
        setupRecyclerView();
        setupListeners();
        cargarOfertas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarOfertas();
    }

    private void initViews() {
        pbLoading = findViewById(R.id.pbLoadingGestionOfertas);
        layoutVacio = findViewById(R.id.layoutVacioGestionOfertas);
        swipeRefresh = findViewById(R.id.swipeRefreshGestionOfertas);
        rvGestion = findViewById(R.id.rvGestionOfertas);
        btnVolver = findViewById(R.id.btnVolverGestionOfertas);
        btnNuevaOferta = findViewById(R.id.btnNuevaOfertaAdmin);
    }

    private void setupRecyclerView() {
        rvGestion.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GestionOfertaAdapter(this, new ArrayList<>());
        rvGestion.setAdapter(adapter);

        adapter.setOnGestionOfertaActionListener(new GestionOfertaAdapter.OnGestionOfertaActionListener() {
            @Override
            public void onEditar(Oferta oferta) {
                Intent intent = new Intent(GestionOfertasActivity.this, OfertaFormularioActivity.class);
                intent.putExtra("oferta_id", oferta.getId());
                startActivity(intent);
            }

            @Override
            public void onEliminar(Oferta oferta) {
                mostrarDialogoEliminar(oferta);
            }
        });
    }

    private void setupListeners() {
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnNuevaOferta != null) {
            btnNuevaOferta.setOnClickListener(v -> {
                Intent intent = new Intent(GestionOfertasActivity.this, OfertaFormularioActivity.class);
                startActivity(intent);
            });
        }

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.blue_primary);
            swipeRefresh.setOnRefreshListener(this::cargarOfertas);
        }
    }

    private void cargarOfertas() {
        if (swipeRefresh != null && !swipeRefresh.isRefreshing()) {
            if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService().getOfertas().enqueue(new Callback<OfertaResponse>() {
            @Override
            public void onResponse(@NonNull Call<OfertaResponse> call, @NonNull Response<OfertaResponse> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                if (response.code() == 401) {
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Oferta> lista = response.body().getData();
                    if (lista != null && !lista.isEmpty()) {
                        mostrarLista(lista);
                    } else {
                        cargarOfertasDirectList();
                    }
                } else {
                    cargarOfertasDirectList();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OfertaResponse> call, @NonNull Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                cargarOfertasDirectList();
            }
        });
    }

    private void cargarOfertasDirectList() {
        ApiClient.getApiService().getOfertasDirectList().enqueue(new Callback<List<Oferta>>() {
            @Override
            public void onResponse(@NonNull Call<List<Oferta>> call, @NonNull Response<List<Oferta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Oferta> lista = response.body();
                    if (lista != null && !lista.isEmpty()) {
                        mostrarLista(lista);
                    } else {
                        mostrarEstadoVacio();
                    }
                } else {
                    mostrarEstadoVacio();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Oferta>> call, @NonNull Throwable t) {
                mostrarEstadoVacio();
            }
        });
    }

    private void mostrarLista(List<Oferta> lista) {
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
        if (rvGestion != null) rvGestion.setVisibility(View.VISIBLE);
        if (adapter != null) adapter.setOfertaList(lista);
    }

    private void mostrarEstadoVacio() {
        if (rvGestion != null) rvGestion.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.VISIBLE);
    }

    private void mostrarDialogoEliminar(Oferta oferta) {
        new AlertDialog.Builder(this)
                .setTitle("Desactivar Oferta")
                .setMessage("¿Deseas desactivar/eliminar la oferta \"" + oferta.getNombre() + "\"?")
                .setPositiveButton("Sí, desactivar", (dialog, which) -> ejecutarEliminarOferta(oferta.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarEliminarOferta(int id) {
        ApiClient.getApiService().eliminarOferta(id).enqueue(new Callback<OfertaResponse>() {
            @Override
            public void onResponse(@NonNull Call<OfertaResponse> call, @NonNull Response<OfertaResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GestionOfertasActivity.this, "¡Oferta desactivada correctamente!", Toast.LENGTH_SHORT).show();
                    cargarOfertas();
                } else {
                    Toast.makeText(GestionOfertasActivity.this, "Error al desactivar oferta (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OfertaResponse> call, @NonNull Throwable t) {
                Toast.makeText(GestionOfertasActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void manejarSesionExpirada() {
        Toast.makeText(this, "Tu sesión ha expirado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(GestionOfertasActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
