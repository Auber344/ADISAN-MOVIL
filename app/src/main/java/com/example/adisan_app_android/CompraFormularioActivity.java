package com.example.adisan_app_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompraFormularioActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private AutoCompleteTextView actvProveedorCompra;
    private AutoCompleteTextView actvProductoCompra;

    private TextView tvProductoNombrePreview;
    private TextView tvProductoCategoriaPreview;
    private TextView tvUnidadesPorPaqueteInfo;
    private TextView tvPrecioCompraRef;
    private TextView tvPrecioVentaPaqRef;
    private TextView tvPrecioVentaUnidRef;

    private EditText etCantidadPaquetes;
    private ImageView btnMenosPaquetes;
    private ImageView btnMasPaquetes;
    private TextView tvUnidadesCalculadas;
    private TextInputEditText etPrecioUnitarioPaquete;

    private TextView tvResumenPaquetes;
    private TextView tvResumenUnidades;
    private TextView tvResumenPrecioPaq;
    private TextView tvResumenTotal;

    private MaterialButton btnConfirmarCompra;
    private MaterialButton btnCancelarFormCompra;
    private RecyclerView rvItems;

    private List<Proveedor> proveedorList = new ArrayList<>();
    private List<Producto> productoList = new ArrayList<>();
    private List<CompraRequest.CompraItemRequest> itemRequests = new ArrayList<>();

    private Producto productoSeleccionado;
    private Proveedor proveedorSeleccionado;

    private boolean isUpdatingValues = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compra_formulario);

        initViews();
        setupRecyclerView();
        setupListeners();
        cargarProveedores();
        cargarProductos();
    }

    private void initViews() {
        btnVolver = findViewById(R.id.btnVolverFormCompra);
        actvProveedorCompra = findViewById(R.id.actvProveedorCompra);
        actvProductoCompra = findViewById(R.id.actvProductoCompra);

        tvProductoNombrePreview = findViewById(R.id.tvProductoNombrePreview);
        tvProductoCategoriaPreview = findViewById(R.id.tvProductoCategoriaPreview);
        tvUnidadesPorPaqueteInfo = findViewById(R.id.tvUnidadesPorPaqueteInfo);
        tvPrecioCompraRef = findViewById(R.id.tvPrecioCompraRef);
        tvPrecioVentaPaqRef = findViewById(R.id.tvPrecioVentaPaqRef);
        tvPrecioVentaUnidRef = findViewById(R.id.tvPrecioVentaUnidRef);

        etCantidadPaquetes = findViewById(R.id.etCantidadPaquetes);
        btnMenosPaquetes = findViewById(R.id.btnMenosPaquetes);
        btnMasPaquetes = findViewById(R.id.btnMasPaquetes);
        tvUnidadesCalculadas = findViewById(R.id.tvUnidadesCalculadas);
        etPrecioUnitarioPaquete = findViewById(R.id.etPrecioUnitarioPaquete);

        tvResumenPaquetes = findViewById(R.id.tvResumenPaquetes);
        tvResumenUnidades = findViewById(R.id.tvResumenUnidades);
        tvResumenPrecioPaq = findViewById(R.id.tvResumenPrecioPaq);
        tvResumenTotal = findViewById(R.id.tvResumenTotal);

        btnConfirmarCompra = findViewById(R.id.btnConfirmarCompra);
        btnCancelarFormCompra = findViewById(R.id.btnCancelarFormCompra);
        rvItems = findViewById(R.id.rvItemsCompra);
    }

    private void setupRecyclerView() {
        if (rvItems != null) {
            rvItems.setLayoutManager(new LinearLayoutManager(this));
            List<String> prodNombres = new ArrayList<>();
            List<Integer> uppList = new ArrayList<>();
            CompraItemFormAdapter itemAdapter = new CompraItemFormAdapter(this, itemRequests, prodNombres, uppList);
            rvItems.setAdapter(itemAdapter);
        }
    }

    private void setupListeners() {
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnCancelarFormCompra != null) {
            btnCancelarFormCompra.setOnClickListener(v -> finish());
        }

        if (btnMenosPaquetes != null) {
            btnMenosPaquetes.setOnClickListener(v -> {
                int cant = obtenerCantidadPaquetes();
                if (cant > 1) {
                    setCantidadPaquetes(cant - 1);
                }
            });
        }

        if (btnMasPaquetes != null) {
            btnMasPaquetes.setOnClickListener(v -> {
                int cant = obtenerCantidadPaquetes();
                setCantidadPaquetes(cant + 1);
            });
        }

        if (etCantidadPaquetes != null) {
            etCantidadPaquetes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (!isUpdatingValues) {
                        recalcularResumen();
                    }
                }
            });
        }

        if (etPrecioUnitarioPaquete != null) {
            etPrecioUnitarioPaquete.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (!isUpdatingValues) {
                        recalcularResumen();
                    }
                }
            });
        }

        if (btnConfirmarCompra != null) {
            btnConfirmarCompra.setOnClickListener(v -> enviarCompra());
        }
    }

    private int obtenerCantidadPaquetes() {
        if (etCantidadPaquetes != null && etCantidadPaquetes.getText() != null) {
            try {
                int val = Integer.parseInt(etCantidadPaquetes.getText().toString().trim());
                return val > 0 ? val : 1;
            } catch (Exception e) {
                return 1;
            }
        }
        return 1;
    }

    private void setCantidadPaquetes(int cant) {
        isUpdatingValues = true;
        if (etCantidadPaquetes != null) {
            etCantidadPaquetes.setText(String.valueOf(cant));
            etCantidadPaquetes.setSelection(etCantidadPaquetes.getText().length());
        }
        isUpdatingValues = false;
        recalcularResumen();
    }

    private void actualizarPreviewProductoYProveedorAuto() {
        if (productoSeleccionado == null) return;

        int upp = productoSeleccionado.getUnidadesPorPaqueteVal();
        double pCompraRef = 0.0;
        double pVentaPaq = 0.0;
        double pVentaUnid = 0.0;
        try { pCompraRef = Double.parseDouble(productoSeleccionado.getPrecioCompraPaquete()); } catch (Exception ignored) {}
        try { pVentaPaq = Double.parseDouble(productoSeleccionado.getPrecioVentaPaquete()); } catch (Exception ignored) {}
        try { pVentaUnid = Double.parseDouble(productoSeleccionado.getPrecioVentaUnidad()); } catch (Exception ignored) {}

        if (tvProductoNombrePreview != null) tvProductoNombrePreview.setText(productoSeleccionado.getProductoNombre());
        if (tvProductoCategoriaPreview != null) tvProductoCategoriaPreview.setText("CATEGORÍA #" + productoSeleccionado.getCategoriaId());
        if (tvUnidadesPorPaqueteInfo != null) tvUnidadesPorPaqueteInfo.setText(upp + " unidades");
        if (tvPrecioCompraRef != null) tvPrecioCompraRef.setText(String.format(Locale.US, "S/ %.2f", pCompraRef));
        if (tvPrecioVentaPaqRef != null) tvPrecioVentaPaqRef.setText(String.format(Locale.US, "S/ %.2f", pVentaPaq));
        if (tvPrecioVentaUnidRef != null) tvPrecioVentaUnidRef.setText(String.format(Locale.US, "S/ %.2f", pVentaUnid));

        isUpdatingValues = true;
        if (etPrecioUnitarioPaquete != null) {
            etPrecioUnitarioPaquete.setText(String.format(Locale.US, "%.2f", pCompraRef > 0 ? pCompraRef : 0.0));
        }
        isUpdatingValues = false;

        // Auto-seleccionar proveedor correspondiente
        int provIdProd = productoSeleccionado.getProveedorId();
        for (Proveedor p : proveedorList) {
            if (p.getId() == provIdProd) {
                proveedorSeleccionado = p;
                if (actvProveedorCompra != null) {
                    actvProveedorCompra.setText(p.getNombreEmpresa(), false);
                }
                break;
            }
        }

        recalcularResumen();
    }

    private void recalcularResumen() {
        int cantPaq = obtenerCantidadPaquetes();
        int upp = productoSeleccionado != null ? productoSeleccionado.getUnidadesPorPaqueteVal() : 1;
        int unidadesTotales = cantPaq * upp;

        double precioPaq = 0.0;
        if (etPrecioUnitarioPaquete != null && etPrecioUnitarioPaquete.getText() != null) {
            try {
                precioPaq = Double.parseDouble(etPrecioUnitarioPaquete.getText().toString().trim());
            } catch (Exception ignored) {}
        }

        double total = cantPaq * precioPaq;

        if (tvUnidadesCalculadas != null) tvUnidadesCalculadas.setText(unidadesTotales + " unidades");
        if (tvResumenPaquetes != null) tvResumenPaquetes.setText(String.valueOf(cantPaq));
        if (tvResumenUnidades != null) tvResumenUnidades.setText(String.valueOf(unidadesTotales));
        if (tvResumenPrecioPaq != null) tvResumenPrecioPaq.setText(String.format(Locale.US, "S/ %.2f", precioPaq));
        if (tvResumenTotal != null) tvResumenTotal.setText(String.format(Locale.US, "S/ %.2f", total));
    }

    private void cargarProveedores() {
        ApiClient.getApiService().getProveedores().enqueue(new Callback<ProveedorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProveedorResponse> call, @NonNull Response<ProveedorResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    proveedorList = response.body().getData();
                }
                configurarAutoCompleteProveedores();
            }

            @Override
            public void onFailure(@NonNull Call<ProveedorResponse> call, @NonNull Throwable t) {
                configurarAutoCompleteProveedores();
            }
        });
    }

    private void configurarAutoCompleteProveedores() {
        if (proveedorList.isEmpty()) {
            Proveedor p = new Proveedor();
            p.setId(1);
            p.setNombreEmpresa("Proveedor General");
            proveedorList.add(p);
        }
        ArrayAdapter<Proveedor> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, proveedorList);
        if (actvProveedorCompra != null) {
            actvProveedorCompra.setAdapter(adapter);
            actvProveedorCompra.setThreshold(1);
            actvProveedorCompra.setOnClickListener(v -> actvProveedorCompra.showDropDown());

            actvProveedorCompra.setOnItemClickListener((parent, view, position, id) -> {
                proveedorSeleccionado = (Proveedor) parent.getItemAtPosition(position);
            });

            if (!proveedorList.isEmpty() && proveedorSeleccionado == null) {
                proveedorSeleccionado = proveedorList.get(0);
                actvProveedorCompra.setText(proveedorSeleccionado.getNombreEmpresa(), false);
            }
        }
    }

    private void cargarProductos() {
        ApiClient.getApiService().getProductosElement().enqueue(new Callback<com.google.gson.JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<com.google.gson.JsonElement> call, @NonNull Response<com.google.gson.JsonElement> response) {
                List<Producto> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    lista = extraerListaProductos(response.body());
                }
                productoList = lista;
                configurarAutoCompleteProductos();
            }

            @Override
            public void onFailure(@NonNull Call<com.google.gson.JsonElement> call, @NonNull Throwable t) {
                configurarAutoCompleteProductos();
            }
        });
    }

    private List<Producto> extraerListaProductos(com.google.gson.JsonElement el) {
        List<Producto> lista = new ArrayList<>();
        if (el == null) return lista;
        try {
            if (el.isJsonArray()) {
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Producto>>() {}.getType();
                lista = new Gson().fromJson(el, listType);
            } else if (el.isJsonObject()) {
                com.google.gson.JsonObject obj = el.getAsJsonObject();
                String[] keys = {"productos", "data", "items", "results", "list", "rows", "productosList", "producto", "item"};
                for (String k : keys) {
                    if (obj.has(k)) {
                        com.google.gson.JsonElement sub = obj.get(k);
                        List<Producto> subLista = extraerListaProductos(sub);
                        if (!subLista.isEmpty()) {
                            return subLista;
                        }
                    }
                }
                if (obj.has("nombre") || obj.has("id") || obj.has("producto_id") || obj.has("precio_venta_paquete")) {
                    Producto p = new Gson().fromJson(obj, Producto.class);
                    if (p != null) {
                        lista.add(p);
                    }
                }
            }
        } catch (Exception ignored) {}
        return lista;
    }

    private void configurarAutoCompleteProductos() {
        ArrayAdapter<Producto> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productoList);
        if (actvProductoCompra != null) {
            actvProductoCompra.setAdapter(adapter);
            actvProductoCompra.setThreshold(1);
            actvProductoCompra.setOnClickListener(v -> actvProductoCompra.showDropDown());

            actvProductoCompra.setOnItemClickListener((parent, view, position, id) -> {
                productoSeleccionado = (Producto) parent.getItemAtPosition(position);
                actualizarPreviewProductoYProveedorAuto();
            });

            if (!productoList.isEmpty() && productoSeleccionado == null) {
                productoSeleccionado = productoList.get(0);
                actvProductoCompra.setText(productoSeleccionado.getProductoNombre(), false);
                actualizarPreviewProductoYProveedorAuto();
            }
        }
    }

    private void enviarCompra() {
        String prodText = actvProductoCompra != null && actvProductoCompra.getText() != null ? actvProductoCompra.getText().toString().trim() : "";
        if (productoSeleccionado == null && !TextUtils.isEmpty(prodText)) {
            for (Producto p : productoList) {
                if (p.getProductoNombre().equalsIgnoreCase(prodText) || p.toString().equalsIgnoreCase(prodText)) {
                    productoSeleccionado = p;
                    break;
                }
            }
        }

        if (productoSeleccionado == null) {
            Toast.makeText(this, "Selecciona un producto", Toast.LENGTH_SHORT).show();
            return;
        }

        int cantPaq = obtenerCantidadPaquetes();
        String precioStr = etPrecioUnitarioPaquete != null && etPrecioUnitarioPaquete.getText() != null ? etPrecioUnitarioPaquete.getText().toString().trim() : "";

        if (TextUtils.isEmpty(precioStr)) {
            Toast.makeText(this, "Ingresa el precio por paquete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Resolver Proveedor ID
        int proveedorId = 1;
        String provText = actvProveedorCompra != null && actvProveedorCompra.getText() != null ? actvProveedorCompra.getText().toString().trim() : "";
        if (proveedorSeleccionado != null) {
            proveedorId = proveedorSeleccionado.getId();
        } else {
            for (Proveedor p : proveedorList) {
                if (p.getNombreEmpresa().equalsIgnoreCase(provText)) {
                    proveedorId = p.getId();
                    break;
                }
            }
        }

        itemRequests.clear();
        CompraRequest.CompraItemRequest item = new CompraRequest.CompraItemRequest(
                productoSeleccionado.getProductoId(),
                cantPaq,
                0,
                precioStr
        );
        itemRequests.add(item);

        android.util.Log.d("DEBUG_COMPRA", "proveedor_id = " + proveedorId);
        android.util.Log.d("DEBUG_COMPRA", "producto seleccionado = " + (productoSeleccionado != null ? productoSeleccionado.getProductoNombre() : "null"));
        android.util.Log.d("DEBUG_COMPRA", "producto_id = " + (productoSeleccionado != null ? productoSeleccionado.getProductoId() : 0));
        android.util.Log.d("DEBUG_COMPRA", "cantidad_paquetes = " + cantPaq);
        android.util.Log.d("DEBUG_COMPRA", "cantidad_unidades = 0");
        android.util.Log.d("DEBUG_COMPRA", "precio_unitario = " + precioStr);
        android.util.Log.d("DEBUG_COMPRA", "items.size() = " + itemRequests.size());
        android.util.Log.d("DEBUG_COMPRA", "registrando compra");

        CompraRequest request = new CompraRequest(proveedorId, itemRequests);

        String jsonPayload = new Gson().toJson(request);
        android.util.Log.d("DEBUG_JSON_COMPRA", "JSON Real Enviado: " + jsonPayload);
        android.util.Log.d("DEBUG_JSON_COMPRA", "proveedor_id = " + proveedorId);
        android.util.Log.d("DEBUG_JSON_COMPRA", "items.size = " + itemRequests.size());
        if (!itemRequests.isEmpty()) {
            android.util.Log.d("DEBUG_JSON_COMPRA", "producto_id = " + itemRequests.get(0).getProductoId());
            android.util.Log.d("DEBUG_JSON_COMPRA", "cantidad_paquetes = " + itemRequests.get(0).getCantidadPaquetes());
            android.util.Log.d("DEBUG_JSON_COMPRA", "cantidad_unidades = " + itemRequests.get(0).getCantidadUnidades());
            android.util.Log.d("DEBUG_JSON_COMPRA", "precio_unitario = " + itemRequests.get(0).getPrecioUnitario());
        }

        if (btnConfirmarCompra != null) btnConfirmarCompra.setEnabled(false);

        ApiClient.getApiService().crearCompra(request).enqueue(new Callback<CompraResponse>() {
            @Override
            public void onResponse(@NonNull Call<CompraResponse> call, @NonNull Response<CompraResponse> response) {
                if (btnConfirmarCompra != null) btnConfirmarCompra.setEnabled(true);

                if (response.code() == 401) {
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CompraFormularioActivity.this, "¡Compra registrada exitosamente!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String msg = "Error al registrar compra (" + response.code() + ")";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            String errStr = response.errorBody().string();
                            CompraResponse errRes = new Gson().fromJson(errStr, CompraResponse.class);
                            if (errRes != null && errRes.getMessage() != null) {
                                msg = errRes.getMessage();
                            }
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(CompraFormularioActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CompraResponse> call, @NonNull Throwable t) {
                if (btnConfirmarCompra != null) btnConfirmarCompra.setEnabled(true);
                Toast.makeText(CompraFormularioActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void manejarSesionExpirada() {
        Toast.makeText(this, "Tu sesión ha expirado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(CompraFormularioActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
