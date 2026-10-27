package com.example.adisan_app_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.RadioButton;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventarioActivity extends AppCompatActivity {

    private ProgressBar pbLoading;
    private LinearLayout layoutVacio;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvInventario;
    private InventarioAdapter adapter;
    private ImageView btnVolver;
    private MaterialButton btnVerCompras;
    private EditText etBuscar;

    private FiltroCategorias filtroCategorias;

    private List<Inventario> inventarioListOriginal = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        initViews();
        setupRecyclerView();
        setupListeners();
        cargarInventario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarInventario();
    }

    private void initViews() {
        pbLoading = findViewById(R.id.pbLoadingInventario);
        layoutVacio = findViewById(R.id.layoutVacioInventario);
        swipeRefresh = findViewById(R.id.swipeRefreshInventario);
        rvInventario = findViewById(R.id.rvInventario);
        btnVolver = findViewById(R.id.btnVolverInventario);
        btnVerCompras = findViewById(R.id.btnVerCompras);
        etBuscar = findViewById(R.id.etBuscarInventario);

        filtroCategorias = new FiltroCategorias(findViewById(R.id.layoutChipsCategoriasInv), categoria -> aplicarFiltrosYBusqueda(textoBusqueda()));
        filtroCategorias.cargar();
    }

    private String textoBusqueda() {
        return etBuscar != null && etBuscar.getText() != null ? etBuscar.getText().toString().trim() : "";
    }

    private void setupRecyclerView() {
        rvInventario.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventarioAdapter(this, new ArrayList<>());
        rvInventario.setAdapter(adapter);

        adapter.setOnInventarioActionListener(new InventarioAdapter.OnInventarioActionListener() {
            @Override
            public void onAjustarStock(Inventario inventario) {
                mostrarDialogoDetalle(inventario);
            }

            @Override
            public void onVerMovimientos(Inventario inventario) {
                mostrarDialogoMovimientos(inventario);
            }
        });
    }

    private void setupListeners() {
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnVerCompras != null) {
            btnVerCompras.setOnClickListener(v -> {
                Intent intent = new Intent(InventarioActivity.this, ComprasActivity.class);
                startActivity(intent);
            });
        }

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.blue_primary);
            swipeRefresh.setOnRefreshListener(this::cargarInventario);
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
    }

    private void cargarInventario() {
        if (swipeRefresh != null && !swipeRefresh.isRefreshing()) {
            if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService().getInventario().enqueue(new Callback<InventarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<InventarioResponse> call, @NonNull Response<InventarioResponse> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                if (response.code() == 401) {
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Inventario> lista = response.body().getData();
                    if (lista != null && !lista.isEmpty()) {
                        procesarInventarioCargado(lista);
                    } else {
                        cargarInventarioDirectList();
                    }
                } else {
                    cargarInventarioDirectList();
                }
            }

            @Override
            public void onFailure(@NonNull Call<InventarioResponse> call, @NonNull Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                cargarInventarioDirectList();
            }
        });
    }

    private void cargarInventarioDirectList() {
        ApiClient.getApiService().getInventarioDirectList().enqueue(new Callback<List<Inventario>>() {
            @Override
            public void onResponse(@NonNull Call<List<Inventario>> call, @NonNull Response<List<Inventario>> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Inventario> lista = response.body();
                    if (lista != null && !lista.isEmpty()) {
                        procesarInventarioCargado(lista);
                    } else {
                        cargarInventarioDesdeProductos();
                    }
                } else {
                    cargarInventarioDesdeProductos();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Inventario>> call, @NonNull Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                cargarInventarioDesdeProductos();
            }
        });
    }

    private void cargarInventarioDesdeProductos() {
        ApiClient.getApiService().getProductos().enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductoResponse> call, @NonNull Response<ProductoResponse> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Producto> prods = response.body().getData();
                    List<Inventario> listInv = new ArrayList<>();
                    for (Producto p : prods) {
                        if (p == null) continue;
                        Inventario inv = new Inventario();
                        inv.setId(p.getProductoId());
                        inv.setProductoId(p.getProductoId());
                        inv.setProductoNombre(p.getProductoNombre());
                        inv.setStockUnidades(p.getStockUnidadesVal());
                        inv.setUnidadesPorPaquete(p.getUnidadesPorPaqueteVal());
                        inv.setCategoria("CATEGORÍA #" + p.getCategoriaId());
                        listInv.add(inv);
                    }
                    if (!listInv.isEmpty()) {
                        procesarInventarioCargado(listInv);
                    } else {
                        mostrarEstadoVacio();
                    }
                } else {
                    mostrarEstadoVacio();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductoResponse> call, @NonNull Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                mostrarEstadoVacio();
            }
        });
    }

    private void procesarInventarioCargado(List<Inventario> lista) {
        inventarioListOriginal = lista;
        aplicarFiltrosYBusqueda(textoBusqueda());
    }

    private void aplicarFiltrosYBusqueda(String query) {
        List<Inventario> filtrados = new ArrayList<>();
        String q = query.toLowerCase();

        for (Inventario inv : inventarioListOriginal) {
            boolean coincide = true;

            if (!TextUtils.isEmpty(q)) {
                String nombre = inv.getProductoNombre() != null ? inv.getProductoNombre().toLowerCase() : "";
                if (!nombre.contains(q)) {
                    coincide = false;
                }
            }
            if (filtroCategorias != null && !filtroCategorias.coincide(inv.getCategoria())) {
                coincide = false;
            }

            if (coincide) {
                filtrados.add(inv);
            }
        }

        if (filtrados.isEmpty()) {
            mostrarEstadoVacio();
        } else {
            mostrarLista(filtrados);
        }
    }

    private void mostrarLista(List<Inventario> lista) {
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
        if (rvInventario != null) rvInventario.setVisibility(View.VISIBLE);
        if (adapter != null) adapter.setInventarioList(lista);
    }

    private void mostrarEstadoVacio() {
        if (rvInventario != null) rvInventario.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.VISIBLE);
    }

    // =======================================================
    // DIÁLOGO DETALLE DE INVENTARIO
    // =======================================================
    private void mostrarDialogoDetalle(Inventario inventario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_detalle_inventario, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnCerrar = dialogView.findViewById(R.id.btnCerrarDetalleInv);
        TextView tvNombre = dialogView.findViewById(R.id.tvDetalleNombreProducto);
        TextView tvCategoria = dialogView.findViewById(R.id.tvDetalleCategoriaProducto);
        TextView tvId = dialogView.findViewById(R.id.tvDetalleIdProducto);
        TextView tvEstadoBadge = dialogView.findViewById(R.id.tvDetalleEstadoStockBadge);
        TextView tvStockActual = dialogView.findViewById(R.id.tvDetalleStockActual);
        TextView tvDesglose = dialogView.findViewById(R.id.tvDetalleDesglose);
        TextView tvUpp = dialogView.findViewById(R.id.tvDetalleUpp);
        TextView tvPaqDisp = dialogView.findViewById(R.id.tvDetallePaqDisp);
        TextView tvSueltas = dialogView.findViewById(R.id.tvDetalleUnidadesSueltas);
        TextView tvInfoCat = dialogView.findViewById(R.id.tvDetalleInfoCat);
        TextView tvInfoEstado = dialogView.findViewById(R.id.tvDetalleInfoEstado);

        MaterialButton btnHistorial = dialogView.findViewById(R.id.btnDetalleHistorial);
        MaterialButton btnAjustar = dialogView.findViewById(R.id.btnDetalleAjustar);

        if (tvNombre != null) tvNombre.setText(inventario.getProductoNombre());
        if (tvCategoria != null) tvCategoria.setText(inventario.getCategoria().toUpperCase());
        if (tvId != null) tvId.setText("ID: " + inventario.getProductoId());

        int stock = inventario.getStockUnidades();
        if (tvStockActual != null) tvStockActual.setText(stock + " un.");

        int paq = inventario.getPaquetesDisponibles();
        int sueltas = inventario.getUnidadesSueltas();
        int upp = inventario.getUnidadesPorPaquete();

        if (tvDesglose != null) tvDesglose.setText(paq + " paq. + " + sueltas + " un.");
        if (tvUpp != null) tvUpp.setText(String.valueOf(upp));
        if (tvPaqDisp != null) tvPaqDisp.setText(paq + " paq.");
        if (tvSueltas != null) tvSueltas.setText(sueltas + " un.");
        if (tvInfoCat != null) tvInfoCat.setText(inventario.getCategoria());

        if (stock == 0) {
            if (tvEstadoBadge != null) {
                tvEstadoBadge.setText("Sin stock");
                tvEstadoBadge.setTextColor(Color.parseColor("#EF4444"));
                tvEstadoBadge.setBackgroundColor(Color.parseColor("#3B1111"));
            }
            if (tvInfoEstado != null) {
                tvInfoEstado.setText("Sin stock");
                tvInfoEstado.setTextColor(Color.parseColor("#EF4444"));
                tvInfoEstado.setBackgroundColor(Color.parseColor("#3B1111"));
            }
        } else if (stock <= 10) {
            if (tvEstadoBadge != null) {
                tvEstadoBadge.setText("Stock bajo");
                tvEstadoBadge.setTextColor(Color.parseColor("#F59E0B"));
                tvEstadoBadge.setBackgroundColor(Color.parseColor("#3B2C0D"));
            }
            if (tvInfoEstado != null) {
                tvInfoEstado.setText("Stock bajo");
                tvInfoEstado.setTextColor(Color.parseColor("#F59E0B"));
                tvInfoEstado.setBackgroundColor(Color.parseColor("#3B2C0D"));
            }
        } else {
            if (tvEstadoBadge != null) {
                tvEstadoBadge.setText("Stock suficiente");
                tvEstadoBadge.setTextColor(Color.parseColor("#10B981"));
                tvEstadoBadge.setBackgroundColor(Color.parseColor("#062E29"));
            }
            if (tvInfoEstado != null) {
                tvInfoEstado.setText("Activo");
                tvInfoEstado.setTextColor(Color.parseColor("#10B981"));
                tvInfoEstado.setBackgroundColor(Color.parseColor("#062E29"));
            }
        }

        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> dialog.dismiss());

        if (btnHistorial != null) {
            btnHistorial.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarDialogoMovimientos(inventario);
            });
        }

        if (btnAjustar != null) {
            btnAjustar.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarDialogoAjuste(inventario);
            });
        }

        dialog.show();
    }

    // =======================================================
    // DIÁLOGO DE AJUSTE DE INVENTARIO
    // =======================================================
    private void mostrarDialogoAjuste(Inventario inventario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ajuste_inventario, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        RadioButton rbEntrada = dialogView.findViewById(R.id.rbEntrada);
        TextInputLayout tilCantidad = dialogView.findViewById(R.id.tilCantidadAjuste);
        TextInputLayout tilMotivo = dialogView.findViewById(R.id.tilMotivoAjuste);
        TextInputEditText etCantidad = dialogView.findViewById(R.id.etCantidadAjuste);
        TextInputEditText etMotivo = dialogView.findViewById(R.id.etMotivoAjuste);

        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarAjuste);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnGuardarAjuste);

        if (btnCancelar != null) btnCancelar.setOnClickListener(v -> dialog.dismiss());

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> {
                if (tilCantidad != null) tilCantidad.setError(null);

                String cantStr = etCantidad != null && etCantidad.getText() != null ? etCantidad.getText().toString().trim() : "";
                String motivo = etMotivo != null && etMotivo.getText() != null ? etMotivo.getText().toString().trim() : "";

                if (TextUtils.isEmpty(cantStr)) {
                    if (tilCantidad != null) tilCantidad.setError("Ingrese cantidad en unidades");
                    return;
                }

                int cantidadUnidades = 0;
                try {
                    cantidadUnidades = Integer.parseInt(cantStr);
                } catch (NumberFormatException e) {
                    if (tilCantidad != null) tilCantidad.setError("Cantidad inválida");
                    return;
                }

                if (cantidadUnidades <= 0) {
                    if (tilCantidad != null) tilCantidad.setError("Debe ser mayor a 0");
                    return;
                }

                String tipo = (rbEntrada != null && rbEntrada.isChecked()) ? "entrada" : "salida";

                InventarioAjusteRequest request = new InventarioAjusteRequest(tipo, cantidadUnidades, motivo);
                ejecutarAjuste(inventario.getProductoId(), request, dialog, btnGuardar);
            });
        }

        dialog.show();
    }

    private void ejecutarAjuste(int productoId, InventarioAjusteRequest request, AlertDialog dialog, MaterialButton btnGuardar) {
        if (btnGuardar != null) btnGuardar.setEnabled(false);

        ApiClient.getApiService().ajustarInventario(productoId, request).enqueue(new Callback<InventarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<InventarioResponse> call, @NonNull Response<InventarioResponse> response) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);

                if (response.code() == 401) {
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful()) {
                    Toast.makeText(InventarioActivity.this, "¡Ajuste de inventario registrado correctamente!", Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    cargarInventario();
                } else {
                    String msg = "Error al registrar ajuste (" + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errStr = response.errorBody().string();
                            InventarioResponse errRes = new Gson().fromJson(errStr, InventarioResponse.class);
                            if (errRes != null && errRes.getMessage() != null) {
                                msg = errRes.getMessage();
                            }
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(InventarioActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<InventarioResponse> call, @NonNull Throwable t) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);
                Toast.makeText(InventarioActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =======================================================
    // DIÁLOGO DE HISTORIAL DE MOVIMIENTOS
    // =======================================================
    private void mostrarDialogoMovimientos(Inventario inventario) {
        if (inventario == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_movimientos_inventario, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnCerrar = dialogView.findViewById(R.id.btnCerrarMovimientos);
        TextView tvNombreProd = dialogView.findViewById(R.id.tvSubtitleMovimientos);
        TextView tvCatProd = dialogView.findViewById(R.id.tvCategoriaMovimientos);
        TextView tvIdProd = dialogView.findViewById(R.id.tvMovIdProducto);
        TextView tvStockVal = dialogView.findViewById(R.id.tvMovStockActualVal);
        TextView tvDesgloseVal = dialogView.findViewById(R.id.tvMovDesgloseStock);

        MaterialButton chipTodos = dialogView.findViewById(R.id.chipTodosMov);
        MaterialButton chipCompras = dialogView.findViewById(R.id.chipComprasMov);
        MaterialButton chipVentas = dialogView.findViewById(R.id.chipVentasMov);
        MaterialButton chipAjustes = dialogView.findViewById(R.id.chipAjustesMov);
        MaterialButton chipReversiones = dialogView.findViewById(R.id.chipReversionesMov);

        ProgressBar pbMov = dialogView.findViewById(R.id.pbLoadingMovimientos);
        RecyclerView rvMov = dialogView.findViewById(R.id.rvMovimientos);
        TextView tvSinMov = dialogView.findViewById(R.id.tvSinMovimientos);

        if (tvNombreProd != null) tvNombreProd.setText(inventario.getProductoNombre());
        if (tvCatProd != null) tvCatProd.setText(inventario.getCategoria() != null ? inventario.getCategoria().toUpperCase() : "GENERAL");
        if (tvIdProd != null) tvIdProd.setText("ID: " + inventario.getProductoId());
        if (tvStockVal != null) tvStockVal.setText(inventario.getStockUnidades() + " un.");
        if (tvDesgloseVal != null) {
            tvDesgloseVal.setText(inventario.getPaquetesDisponibles() + " paq. + " + inventario.getUnidadesSueltas() + " un.");
        }

        MovimientoAdapter movAdapter = new MovimientoAdapter(this, new ArrayList<>());
        if (rvMov != null) {
            rvMov.setLayoutManager(new LinearLayoutManager(this));
            rvMov.setAdapter(movAdapter);
        }

        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> dialog.dismiss());

        final List<MovimientoInventario>[] movimientosOriginales = new List[]{new ArrayList<>()};

        Runnable filtrarMovimientos = () -> {
            String filtroTag = (rvMov != null && rvMov.getTag() != null) ? (String) rvMov.getTag() : "todos";

            List<MovimientoInventario> filtrados = new ArrayList<>();
            if (movimientosOriginales[0] != null) {
                for (MovimientoInventario m : movimientosOriginales[0]) {
                    if (m == null) continue;
                    String tipo = m.getTipo() != null ? m.getTipo().toLowerCase() : "";
                    if (filtroTag.equals("todos")) {
                        filtrados.add(m);
                    } else if (filtroTag.equals("compras") && (tipo.contains("entrada") || tipo.contains("compra"))) {
                        filtrados.add(m);
                    } else if (filtroTag.equals("ventas") && (tipo.contains("salida") || tipo.contains("venta"))) {
                        filtrados.add(m);
                    } else if (filtroTag.equals("ajustes") && tipo.contains("ajuste")) {
                        filtrados.add(m);
                    } else if (filtroTag.equals("reversiones") && (tipo.contains("revers") || tipo.contains("devoluc") || tipo.contains("cancelac"))) {
                        filtrados.add(m);
                    }
                }
            }

            if (filtrados.isEmpty()) {
                if (rvMov != null) rvMov.setVisibility(View.GONE);
                if (tvSinMov != null) {
                    tvSinMov.setVisibility(View.VISIBLE);
                    tvSinMov.setText("No hay movimientos registrados para este filtro");
                }
            } else {
                if (rvMov != null) rvMov.setVisibility(View.VISIBLE);
                if (tvSinMov != null) tvSinMov.setVisibility(View.GONE);
                if (movAdapter != null) movAdapter.setMovimientos(filtrados);
            }
        };

        View.OnClickListener chipListener = v -> {
            String tag = "todos";
            if (v == chipCompras) tag = "compras";
            else if (v == chipVentas) tag = "ventas";
            else if (v == chipAjustes) tag = "ajustes";
            else if (v == chipReversiones) tag = "reversiones";

            if (rvMov != null) rvMov.setTag(tag);

            MaterialButton[] chips = {chipTodos, chipCompras, chipVentas, chipAjustes, chipReversiones};
            for (MaterialButton c : chips) {
                if (c != null) {
                    if (c == v) {
                        c.setBackgroundColor(Color.parseColor("#F97316"));
                        c.setTextColor(Color.WHITE);
                        c.setStrokeWidth(0);
                    } else {
                        c.setBackgroundColor(Color.parseColor("#101725"));
                        c.setTextColor(Color.parseColor("#94A3B8"));
                        c.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#1E293B")));
                        c.setStrokeWidth(1);
                    }
                }
            }
            filtrarMovimientos.run();
        };

        if (chipTodos != null) chipTodos.setOnClickListener(chipListener);
        if (chipCompras != null) chipCompras.setOnClickListener(chipListener);
        if (chipVentas != null) chipVentas.setOnClickListener(chipListener);
        if (chipAjustes != null) chipAjustes.setOnClickListener(chipListener);
        if (chipReversiones != null) chipReversiones.setOnClickListener(chipListener);

        if (pbMov != null) pbMov.setVisibility(View.VISIBLE);
        if (rvMov != null) rvMov.setVisibility(View.GONE);
        if (tvSinMov != null) tvSinMov.setVisibility(View.GONE);

        ApiClient.getApiService().getMovimientosInventario(inventario.getProductoId()).enqueue(new Callback<MovimientoInventarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovimientoInventarioResponse> call, @NonNull Response<MovimientoInventarioResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    if (pbMov != null) pbMov.setVisibility(View.GONE);
                    movimientosOriginales[0] = response.body().getData();
                    if (rvMov != null) rvMov.setTag("todos");
                    filtrarMovimientos.run();
                } else {
                    // Fallback a lista directa si el JSON es un arreglo [ {...} ]
                    cargarMovimientosListaDirecta(inventario.getProductoId(), pbMov, rvMov, tvSinMov, movimientosOriginales, filtrarMovimientos);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovimientoInventarioResponse> call, @NonNull Throwable t) {
                cargarMovimientosListaDirecta(inventario.getProductoId(), pbMov, rvMov, tvSinMov, movimientosOriginales, filtrarMovimientos);
            }
        });

        dialog.show();
    }

    private void cargarMovimientosListaDirecta(int productoId, ProgressBar pbMov, RecyclerView rvMov, TextView tvSinMov, List<MovimientoInventario>[] movimientosOriginales, Runnable filtrarMovimientos) {
        ApiClient.getApiService().getMovimientosInventarioDirectList(productoId).enqueue(new Callback<List<MovimientoInventario>>() {
            @Override
            public void onResponse(@NonNull Call<List<MovimientoInventario>> call, @NonNull Response<List<MovimientoInventario>> response) {
                if (pbMov != null) pbMov.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<MovimientoInventario> lista = response.body();
                    movimientosOriginales[0] = lista != null ? lista : new ArrayList<>();
                    if (rvMov != null) rvMov.setTag("todos");
                    filtrarMovimientos.run();
                } else {
                    if (rvMov != null) rvMov.setVisibility(View.GONE);
                    if (tvSinMov != null) {
                        tvSinMov.setVisibility(View.VISIBLE);
                        tvSinMov.setText("No hay movimientos registrados");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MovimientoInventario>> call, @NonNull Throwable t) {
                if (pbMov != null) pbMov.setVisibility(View.GONE);
                if (rvMov != null) rvMov.setVisibility(View.GONE);
                if (tvSinMov != null) {
                    tvSinMov.setVisibility(View.VISIBLE);
                    tvSinMov.setText("Error de conexión al cargar movimientos");
                }
            }
        });
    }

    private void manejarSesionExpirada() {
        Toast.makeText(this, "Tu sesión ha expirado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(InventarioActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
