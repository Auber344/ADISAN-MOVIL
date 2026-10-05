package com.example.adisan_app_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComprasActivity extends AppCompatActivity {

    private ProgressBar pbLoading;
    private LinearLayout layoutVacio;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvCompras;
    private CompraAdapter adapter;
    private ImageView btnVolver;
    private MaterialButton btnNuevaCompra;
    private EditText etBuscar;

    private TextView tvTotalComprasCount;
    private TextView tvGastoTotalCompras;
    private TextView tvUltimaCompraFecha;

    private MaterialButton chipTodas;
    private MaterialButton chipEsteMes;
    private MaterialButton chipUltimos7Dias;
    private MaterialButton chipEsteAnio;

    private List<Compra> compraListOriginal = new ArrayList<>();
    private String filtroActual = "todas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compras);

        initViews();
        setupRecyclerView();
        setupListeners();
        cargarCompras();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCompras();
    }

    private void initViews() {
        pbLoading = findViewById(R.id.pbLoadingCompras);
        layoutVacio = findViewById(R.id.layoutVacioCompras);
        swipeRefresh = findViewById(R.id.swipeRefreshCompras);
        rvCompras = findViewById(R.id.rvCompras);
        btnVolver = findViewById(R.id.btnVolverCompras);
        btnNuevaCompra = findViewById(R.id.btnNuevaCompra);
        etBuscar = findViewById(R.id.etBuscarCompra);

        tvTotalComprasCount = findViewById(R.id.tvTotalComprasCount);
        tvGastoTotalCompras = findViewById(R.id.tvGastoTotalCompras);
        tvUltimaCompraFecha = findViewById(R.id.tvUltimaCompraFecha);

        chipTodas = findViewById(R.id.chipTodas);
        chipEsteMes = findViewById(R.id.chipEsteMes);
        chipUltimos7Dias = findViewById(R.id.chipUltimos7Dias);
        chipEsteAnio = findViewById(R.id.chipEsteAnio);
    }

    private void setupRecyclerView() {
        rvCompras.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CompraAdapter(this, new ArrayList<>());
        rvCompras.setAdapter(adapter);

        adapter.setOnCompraActionListener(this::mostrarDialogoDetalleCompra);
    }

    private void setupListeners() {
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnNuevaCompra != null) {
            btnNuevaCompra.setOnClickListener(v -> {
                Intent intent = new Intent(ComprasActivity.this, CompraFormularioActivity.class);
                startActivity(intent);
            });
        }

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.blue_primary);
            swipeRefresh.setOnRefreshListener(this::cargarCompras);
        }

        if (etBuscar != null) {
            etBuscar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    aplicarFiltrosYBusqueda(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (chipTodas != null) chipTodas.setOnClickListener(v -> activarFiltro("todas", chipTodas));
        if (chipEsteMes != null) chipEsteMes.setOnClickListener(v -> activarFiltro("mes", chipEsteMes));
        if (chipUltimos7Dias != null) chipUltimos7Dias.setOnClickListener(v -> activarFiltro("7dias", chipUltimos7Dias));
        if (chipEsteAnio != null) chipEsteAnio.setOnClickListener(v -> activarFiltro("anio", chipEsteAnio));
    }

    private void activarFiltro(String filtro, MaterialButton chipSeleccionado) {
        filtroActual = filtro;
        actualizarEstiloChips(chipSeleccionado);
        aplicarFiltrosYBusqueda(etBuscar != null && etBuscar.getText() != null ? etBuscar.getText().toString().trim() : "");
    }

    private void actualizarEstiloChips(MaterialButton seleccionado) {
        MaterialButton[] chips = {chipTodas, chipEsteMes, chipUltimos7Dias, chipEsteAnio};
        for (MaterialButton c : chips) {
            if (c != null) {
                if (c == seleccionado) {
                    c.setBackgroundColor(Color.parseColor("#F97316"));
                    c.setTextColor(Color.WHITE);
                } else {
                    c.setBackgroundColor(Color.parseColor("#101725"));
                    c.setTextColor(Color.parseColor("#94A3B8"));
                }
            }
        }
    }

    private void cargarCompras() {
        if (swipeRefresh != null && !swipeRefresh.isRefreshing()) {
            if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService().getCompras().enqueue(new Callback<CompraResponse>() {
            @Override
            public void onResponse(@NonNull Call<CompraResponse> call, @NonNull Response<CompraResponse> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                if (response.code() == 401) {
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Compra> lista = response.body().getData();
                    if (lista != null && !lista.isEmpty()) {
                        procesarComprasCargadas(lista);
                    } else {
                        cargarComprasDirectList();
                    }
                } else {
                    cargarComprasDirectList();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CompraResponse> call, @NonNull Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                cargarComprasDirectList();
            }
        });
    }

    private void cargarComprasDirectList() {
        ApiClient.getApiService().getComprasDirectList().enqueue(new Callback<List<Compra>>() {
            @Override
            public void onResponse(@NonNull Call<List<Compra>> call, @NonNull Response<List<Compra>> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Compra> lista = response.body();
                    if (lista != null && !lista.isEmpty()) {
                        procesarComprasCargadas(lista);
                    } else {
                        mostrarEstadoVacio();
                    }
                } else {
                    mostrarEstadoVacio();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Compra>> call, @NonNull Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                mostrarEstadoVacio();
            }
        });
    }

    private void procesarComprasCargadas(List<Compra> lista) {
        compraListOriginal = lista;
        calcularResumen(lista);
        aplicarFiltrosYBusqueda(etBuscar != null && etBuscar.getText() != null ? etBuscar.getText().toString().trim() : "");
    }

    private void calcularResumen(List<Compra> lista) {
        int totalCount = lista.size();
        double gastoTotal = 0.0;
        String ultimaFecha = "---";

        if (!lista.isEmpty()) {
            ultimaFecha = lista.get(0).getFecha();
            for (Compra c : lista) {
                try {
                    gastoTotal += Double.parseDouble(c.getTotal());
                } catch (Exception ignored) {}
            }
        }

        if (tvTotalComprasCount != null) tvTotalComprasCount.setText(String.valueOf(totalCount));
        if (tvGastoTotalCompras != null) tvGastoTotalCompras.setText(String.format(Locale.US, "S/ %.2f", gastoTotal));
        if (tvUltimaCompraFecha != null) tvUltimaCompraFecha.setText(!TextUtils.isEmpty(ultimaFecha) ? ultimaFecha : "---");
    }

    private void aplicarFiltrosYBusqueda(String query) {
        List<Compra> filtradas = new ArrayList<>();
        String q = query.toLowerCase();

        for (Compra c : compraListOriginal) {
            boolean coincideBusqueda = TextUtils.isEmpty(q);
            if (!coincideBusqueda) {
                String prov = c.getProveedorNombre() != null ? c.getProveedorNombre().toLowerCase() : "";
                if (prov.contains(q)) {
                    coincideBusqueda = true;
                } else if (c.getDetalles() != null) {
                    for (DetalleCompra det : c.getDetalles()) {
                        if (det.getProductoNombre() != null && det.getProductoNombre().toLowerCase().contains(q)) {
                            coincideBusqueda = true;
                            break;
                        }
                    }
                }
            }

            if (coincideBusqueda) {
                filtradas.add(c);
            }
        }

        if (filtradas.isEmpty()) {
            mostrarEstadoVacio();
        } else {
            mostrarLista(filtradas);
        }
    }

    private void mostrarLista(List<Compra> lista) {
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
        if (rvCompras != null) rvCompras.setVisibility(View.VISIBLE);
        if (adapter != null) adapter.setCompraList(lista);
    }

    private void mostrarEstadoVacio() {
        if (rvCompras != null) rvCompras.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.VISIBLE);
    }

    private void mostrarDialogoDetalleCompra(Compra compra) {
        Intent intent = new Intent(ComprasActivity.this, CompraDetalleActivity.class);
        intent.putExtra("compra_json", new Gson().toJson(compra));
        startActivity(intent);
    }

    private void manejarSesionExpirada() {
        Toast.makeText(this, "Tu sesión ha expirado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(ComprasActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}
