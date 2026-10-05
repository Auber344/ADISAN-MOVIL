package com.example.adisan_app_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductosActivity extends AppCompatActivity {

    private ProgressBar pbLoading;
    private LinearLayout layoutVacio;
    private LinearLayout layoutError;
    private TextView tvMensajeError;
    private MaterialButton btnReintentar;
    private MaterialButton btnCrearPrimerProducto;
    private MaterialButton btnNuevoProductoHeader;
    private MaterialButton btnVerCarrito;
    private MaterialButton btnVerInventario;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvProductos;
    private ProductoAdapter adapter;

    private ImageView btnVolver;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_productos);

            // 1. Inicializar vistas
            initViews();

            // 2. Manejar insets para que el header respete la barra de estado superior
            setupWindowInsets();

            // 3. Configurar RecyclerView y Adapter
            setupRecyclerView();

            // 4. Configurar listeners
            setupListeners();

            // 5. Configurar títulos según el Rol del usuario
            configurarTituloPorRol();

            // 6. Cargar productos desde el backend
            cargarProductos();
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir Productos: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar automáticamente la lista al retomar foco
        cargarProductos();
    }

    private void configurarTituloPorRol() {
        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        String rol = preferences.getString("rol", "admin");

        TextView tvTitle = findViewById(R.id.tvHeaderTitle);
        if (tvTitle != null) {
            if ("cliente".equalsIgnoreCase(rol)) {
                tvTitle.setText("Comprar productos");
            } else {
                tvTitle.setText("Vender productos");
            }
        }
    }

    private void initViews() {
        pbLoading = findViewById(R.id.pbLoading);
        layoutVacio = findViewById(R.id.layoutVacio);
        layoutError = findViewById(R.id.layoutError);
        tvMensajeError = findViewById(R.id.tvMensajeError);
        btnReintentar = findViewById(R.id.btnReintentar);
        btnCrearPrimerProducto = findViewById(R.id.btnCrearPrimerProducto);
        btnNuevoProductoHeader = findViewById(R.id.btnNuevoProductoHeader);
        btnVerCarrito = findViewById(R.id.btnVerCarrito);
        btnVerInventario = findViewById(R.id.btnVerInventario);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvProductos = findViewById(R.id.rvProductos);
        btnVolver = findViewById(R.id.btnVolver);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupWindowInsets() {
        View headerBar = findViewById(R.id.headerBar);
        if (headerBar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(headerBar, (v, insets) -> {
                Insets statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                int topPadding = statusInsets.top > 0 ? statusInsets.top + 16 : 36;
                v.setPadding(v.getPaddingLeft(), topPadding, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }
    }

    private void setupRecyclerView() {
        rvProductos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductoAdapter(this, new ArrayList<>());
        rvProductos.setAdapter(adapter);

        // Listener para acciones de Ver Detalle, Carrito, Editar y Eliminar
        adapter.setOnProductoActionListener(new ProductoAdapter.OnProductoActionListener() {
            @Override
            public void onVerDetalle(Producto producto) {
                Intent intent = new Intent(ProductosActivity.this, ProductoDetalleActivity.class);
                intent.putExtra("producto_json", new Gson().toJson(producto));
                startActivity(intent);
            }

            @Override
            public void onEditar(Producto producto) {
                mostrarDialogoFormularioProducto(producto);
            }

            @Override
            public void onEliminar(Producto producto) {
                mostrarDialogoConfirmarEliminar(producto);
            }

            @Override
            public void onAgregarCarrito(Producto producto) {
                if (producto.getStockUnidadesVal() <= 0) {
                    Toast.makeText(ProductosActivity.this, "Producto sin stock disponible", Toast.LENGTH_SHORT).show();
                    return;
                }
                mostrarDialogoTipoVenta(producto);
            }

            @Override
            public void onAgregarStock(Producto producto) {
                mostrarDialogoAgregarStock(producto);
            }
        });
    }

    private void setupListeners() {
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnNuevoProductoHeader != null) {
            btnNuevoProductoHeader.setOnClickListener(v -> mostrarDialogoFormularioProducto(null));
        }

        if (btnVerCarrito != null) {
            btnVerCarrito.setOnClickListener(v -> {
                Intent intent = new Intent(ProductosActivity.this, CarritoActivity.class);
                startActivity(intent);
            });
        }

        if (btnVerInventario != null) {
            btnVerInventario.setOnClickListener(v -> {
                Intent intent = new Intent(ProductosActivity.this, InventarioActivity.class);
                startActivity(intent);
            });
        }

        if (btnCrearPrimerProducto != null) {
            btnCrearPrimerProducto.setOnClickListener(v -> mostrarDialogoFormularioProducto(null));
        }

        if (btnReintentar != null) {
            btnReintentar.setOnClickListener(v -> cargarProductos());
        }

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.blue_primary);
            swipeRefresh.setOnRefreshListener(this::cargarProductos);
        }

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_productos);
            bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_productos) {
                        return true;
                    } else if (itemId == R.id.nav_dashboard) {
                        Intent intent = new Intent(ProductosActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_pedidos) {
                        Intent intent = new Intent(ProductosActivity.this, PedidosActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_mas) {
                        Toast.makeText(ProductosActivity.this, "Sección Más (Próximamente)", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void cargarProductos() {
        if (swipeRefresh != null && !swipeRefresh.isRefreshing()) {
            mostrarEstadoCarga();
        }

        ApiClient.getApiService().getProductos().enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductoResponse> call, @NonNull Response<ProductoResponse> response) {
                android.util.Log.d("ADISAN_PRODUCTOS_TRACE", "ADISAN_PRODUCTOS_TRACE: HTTP code = " + response.code());
                ocultarSwipeRefresh();

                if (response.code() == 401) {
                    manejarSesionExpirada();
                    return;
                }

                List<Producto> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("ADISAN_PRODUCTOS_TRACE", "ADISAN_PRODUCTOS_TRACE: response.body() is not null");
                    try {
                        lista = response.body().getData();
                        if (lista == null) {
                            android.util.Log.e("ADISAN_PRODUCTOS_TRACE", "ADISAN_PRODUCTOS_TRACE: response.body().getData() is NULL!");
                        } else {
                            android.util.Log.d("ADISAN_PRODUCTOS_TRACE", "ADISAN_PRODUCTOS_TRACE: getData() size = " + lista.size());
                            for (Producto p : lista) {
                                if (p.getId() == 7) {
                                    android.util.Log.d("ADISAN_PRODUCTOS_TRACE", "ADISAN_PRODUCTOS_TRACE: Producto ID 7 found: id=" + p.getId() + ", nombre=" + p.getNombre() + ", stockUnidadesVal=" + p.getStockUnidadesVal() + ", catId=" + p.getCategoriaId() + ", upp=" + p.getUnidadesPorPaqueteVal() + ", pCompra=" + p.getPrecioCompraPaquete() + ", pVentaPaq=" + p.getPrecioVentaPaquete() + ", pVentaUnid=" + p.getPrecioVentaUnidad());
                                }
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ADISAN_PRODUCTOS_TRACE", "ADISAN_PRODUCTOS_TRACE: EXCEPTION en onResponse: " + e.getMessage(), e);
                    }
                } else {
                    android.util.Log.e("ADISAN_PRODUCTOS_TRACE", "ADISAN_PRODUCTOS_TRACE: response not successful or body is null. code=" + response.code());
                }

                if (lista != null && !lista.isEmpty()) {
                    android.util.Log.d("ADISAN_PRODUCTOS_TRACE", "ADISAN_PRODUCTOS_TRACE: llamando a mostrarListaProductos()");
                    mostrarListaProductos(lista);
                } else {
                    android.util.Log.d("ADISAN_PRODUCTOS_TRACE", "ADISAN_PRODUCTOS_TRACE: lista vacía, llamando a cargarProductosDirecto()");
                    cargarProductosDirecto();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductoResponse> call, @NonNull Throwable t) {
                android.util.Log.e("ADISAN_PRODUCTOS_TRACE", "ADISAN_PRODUCTOS_TRACE: FAILURE: " + t.getMessage(), t);
                cargarProductosDirecto();
            }
        });
    }

    private void cargarProductosDirecto() {
        ApiClient.getApiService().getProductosDirectList().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(@NonNull Call<List<Producto>> call, @NonNull Response<List<Producto>> response) {
                android.util.Log.d("ADISAN_STOCK_TRACE", "TRACE 1B (getProductosDirectList response): code=" + response.code());
                ocultarSwipeRefresh();
                List<Producto> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    lista = response.body();
                    for (Producto p : lista) {
                        if (p.getId() == 7) {
                            android.util.Log.d("ADISAN_STOCK_TRACE", "TRACE 2B (DirectList after response): id=" + p.getId() + ", nombre=" + p.getNombre() + ", getStockVal=" + p.getStockUnidadesVal());
                        }
                    }
                }

                if (lista != null && !lista.isEmpty()) {
                    mostrarListaProductos(lista);
                } else {
                    mostrarEstadoVacio();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Producto>> call, @NonNull Throwable t) {
                ocultarSwipeRefresh();
                if (t instanceof java.io.IOException) {
                    mostrarEstadoError("Error de conexión al servidor (10.0.2.2:3000).");
                } else {
                    mostrarEstadoVacio();
                }
            }
        });
    }

    // =======================================================
    // DIÁLOGO PARA AUMENTAR STOCK
    // =======================================================
    private void mostrarDialogoAgregarStock(Producto producto) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = 
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_stock, null);
        bottomSheetDialog.setContentView(dialogView);

        View bottomSheet = (View) dialogView.getParent();
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(android.R.color.transparent);
        }

        TextView tvNombre = dialogView.findViewById(R.id.tvNombreProductoStock);
        TextView tvStockActual = dialogView.findViewById(R.id.tvStockActualDialog);
        android.widget.EditText etCantidad = dialogView.findViewById(R.id.etCantidadAgregar);
        android.widget.ImageButton btnCerrar = dialogView.findViewById(R.id.btnCerrarDialogStock);
        com.google.android.material.button.MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarStock);
        com.google.android.material.button.MaterialButton btnGuardar = dialogView.findViewById(R.id.btnGuardarStock);

        if (tvNombre != null) tvNombre.setText(producto.getProductoNombre());
        if (tvStockActual != null) tvStockActual.setText(String.valueOf(producto.getStock()));

        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> bottomSheetDialog.dismiss());
        if (btnCancelar != null) btnCancelar.setOnClickListener(v -> bottomSheetDialog.dismiss());

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> {
                String cantidadStr = etCantidad != null && etCantidad.getText() != null ? etCantidad.getText().toString().trim() : "";
                if (cantidadStr.isEmpty()) {
                    Toast.makeText(ProductosActivity.this, "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
                    return;
                }
                int cantidadAgregar = 0;
                try {
                    cantidadAgregar = Integer.parseInt(cantidadStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(ProductosActivity.this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (cantidadAgregar <= 0) {
                    Toast.makeText(ProductosActivity.this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                int nuevoStock = producto.getStock() + cantidadAgregar;
                producto.setStock(nuevoStock);
                if (adapter != null) adapter.notifyDataSetChanged();
                
                ejecutarActualizacionStock(producto.getProductoId(), producto, bottomSheetDialog, btnGuardar);
            });
        }

        bottomSheetDialog.show();
    }

    private void ejecutarActualizacionStock(int id, Producto productoRequest, com.google.android.material.bottomsheet.BottomSheetDialog dialog, MaterialButton btnGuardar) {
        if (btnGuardar != null) btnGuardar.setEnabled(false);
        
        ApiClient.getApiService().editarProducto(id, productoRequest).enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductoResponse> call, @NonNull Response<ProductoResponse> response) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);

                if (response.code() == 401) {
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    ProductoResponse body = response.body();
                    if (body.isSuccess() || body.getMessage() == null || body.getMessage().toLowerCase().contains("éxito") || body.getMessage().toLowerCase().contains("correctamente")) {
                        Toast.makeText(ProductosActivity.this, "¡Stock añadido correctamente!", Toast.LENGTH_SHORT).show();
                        if (dialog != null && dialog.isShowing()) dialog.dismiss();
                        cargarProductos();
                    } else {
                        String msg = body.getMessage() != null ? body.getMessage() : "Error al actualizar stock";
                        Toast.makeText(ProductosActivity.this, msg, Toast.LENGTH_LONG).show();
                        cargarProductos();
                    }
                } else {
                    String msg = "Error al actualizar stock (" + response.code() + ")";
                    Toast.makeText(ProductosActivity.this, msg, Toast.LENGTH_LONG).show();
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    cargarProductos();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductoResponse> call, @NonNull Throwable t) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);
                Toast.makeText(ProductosActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                if (dialog != null && dialog.isShowing()) dialog.dismiss();
                cargarProductos();
            }
        });
    }

    private void mostrarDialogoTipoVenta(Producto producto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tipo_venta, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        android.widget.RadioGroup rgTipoVenta = dialogView.findViewById(R.id.rgTipoVenta);
        android.widget.RadioButton rbPaquete = dialogView.findViewById(R.id.rbPaquete);
        android.widget.RadioButton rbUnidad = dialogView.findViewById(R.id.rbUnidad);
        android.widget.RadioButton rbMixta = dialogView.findViewById(R.id.rbMixta);

        TextInputLayout tilPaquetes = dialogView.findViewById(R.id.tilCantidadPaquetesCart);
        TextInputLayout tilUnidades = dialogView.findViewById(R.id.tilCantidadUnidadesCart);
        TextInputEditText etPaquetes = dialogView.findViewById(R.id.etCantidadPaquetesCart);
        TextInputEditText etUnidades = dialogView.findViewById(R.id.etCantidadUnidadesCart);

        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarTipoVenta);
        MaterialButton btnAgregar = dialogView.findViewById(R.id.btnAgregarAlCarritoFinal);

        if (rgTipoVenta != null) {
            rgTipoVenta.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbPaquete) {
                    if (tilPaquetes != null) tilPaquetes.setVisibility(View.VISIBLE);
                    if (tilUnidades != null) tilUnidades.setVisibility(View.GONE);
                } else if (checkedId == R.id.rbUnidad) {
                    if (tilPaquetes != null) tilPaquetes.setVisibility(View.GONE);
                    if (tilUnidades != null) tilUnidades.setVisibility(View.VISIBLE);
                } else {
                    if (tilPaquetes != null) tilPaquetes.setVisibility(View.VISIBLE);
                    if (tilUnidades != null) tilUnidades.setVisibility(View.VISIBLE);
                }
            });
        }

        if (btnCancelar != null) btnCancelar.setOnClickListener(v -> dialog.dismiss());

        if (btnAgregar != null) {
            btnAgregar.setOnClickListener(v -> {
                String tipoVenta = "PAQUETE";
                int cantPaq = 0;
                int cantUnid = 0;

                if (rbUnidad != null && rbUnidad.isChecked()) {
                    tipoVenta = "UNIDAD";
                } else if (rbMixta != null && rbMixta.isChecked()) {
                    tipoVenta = "MIXTA";
                } else {
                    tipoVenta = "PAQUETE";
                }

                if (tipoVenta.equals("PAQUETE") || tipoVenta.equals("MIXTA")) {
                    String paqStr = etPaquetes != null && etPaquetes.getText() != null ? etPaquetes.getText().toString().trim() : "0";
                    try {
                        cantPaq = Integer.parseInt(paqStr);
                    } catch (Exception ignored) {}
                }

                if (tipoVenta.equals("UNIDAD") || tipoVenta.equals("MIXTA")) {
                    String unidStr = etUnidades != null && etUnidades.getText() != null ? etUnidades.getText().toString().trim() : "0";
                    try {
                        cantUnid = Integer.parseInt(unidStr);
                    } catch (Exception ignored) {}
                }

                if (tipoVenta.equals("PAQUETE") && cantPaq <= 0) {
                    Toast.makeText(this, "Ingresa cantidad de paquetes válida", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tipoVenta.equals("UNIDAD") && cantUnid <= 0) {
                    Toast.makeText(this, "Ingresa cantidad de unidades válida", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tipoVenta.equals("MIXTA") && cantPaq <= 0 && cantUnid <= 0) {
                    Toast.makeText(this, "Ingresa al menos paquetes o unidades", Toast.LENGTH_SHORT).show();
                    return;
                }

                CarritoManager.getInstance().agregarProducto(producto, tipoVenta, cantPaq, cantUnid);
                Toast.makeText(this, "¡" + producto.getProductoNombre() + " agregado al carrito!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    // =======================================================
    // FORMULARIO CREAR Y EDITAR PRODUCTO
    // =======================================================

    private void mostrarDialogoFormularioProducto(Producto productoExistente) {
        boolean esEdicion = productoExistente != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_producto, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCanceledOnTouchOutside(false);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvSubtitle = dialogView.findViewById(R.id.tvDialogSubtitle);

        AutoCompleteTextView actvProveedor = dialogView.findViewById(R.id.actvProveedor);
        AutoCompleteTextView actvCategoria = dialogView.findViewById(R.id.actvCategoria);
        TextInputLayout tilProveedor = dialogView.findViewById(R.id.tilProveedor);
        TextInputLayout tilCategoria = dialogView.findViewById(R.id.tilCategoria);
        MaterialButton btnCrearCategoria = dialogView.findViewById(R.id.btnCrearCategoria);

        TextInputLayout tilNombre = dialogView.findViewById(R.id.tilNombre);
        TextInputLayout tilUnidadesPorPaquete = dialogView.findViewById(R.id.tilUnidadesPorPaquete);
        TextInputLayout tilPrecioCompraPaquete = dialogView.findViewById(R.id.tilPrecioCompraPaquete);
        TextInputLayout tilPrecioVentaPaquete = dialogView.findViewById(R.id.tilPrecioVentaPaquete);
        TextInputLayout tilPrecioVentaUnidad = dialogView.findViewById(R.id.tilPrecioVentaUnidad);

        TextInputEditText etNombre = dialogView.findViewById(R.id.etNombre);
        TextInputEditText etUnidadesPorPaquete = dialogView.findViewById(R.id.etUnidadesPorPaquete);
        TextInputEditText etPrecioCompraPaquete = dialogView.findViewById(R.id.etPrecioCompraPaquete);
        TextInputEditText etPrecioVentaPaquete = dialogView.findViewById(R.id.etPrecioVentaPaquete);
        TextInputEditText etPrecioVentaUnidad = dialogView.findViewById(R.id.etPrecioVentaUnidad);

        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarDialog);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnGuardarDialog);

        final Proveedor[] selectedProv = {null};
        final Categoria[] selectedCat = {null};

        if (actvProveedor != null) {
            actvProveedor.setOnItemClickListener((parent, view, position, id) -> {
                selectedProv[0] = (Proveedor) parent.getItemAtPosition(position);
            });
        }

        if (actvCategoria != null) {
            actvCategoria.setOnItemClickListener((parent, view, position, id) -> {
                selectedCat[0] = (Categoria) parent.getItemAtPosition(position);
            });
        }

        // Cargar Proveedores y Categorías en AutoCompleteTextViews
        cargarProveedoresEnAutocomplete(actvProveedor, tilProveedor, productoExistente);
        cargarCategoriasEnAutocomplete(actvCategoria, tilCategoria, productoExistente, null);

        if (btnCrearCategoria != null) {
            btnCrearCategoria.setOnClickListener(v -> mostrarDialogoNuevaCategoriaParaAutocomplete(actvCategoria, tilCategoria, productoExistente));
        }

        if (esEdicion) {
            if (tvTitle != null) tvTitle.setText("Editar producto");
            if (tvSubtitle != null) tvSubtitle.setText("Modifica los datos del producto #" + productoExistente.getProductoId());

            if (etNombre != null) etNombre.setText(productoExistente.getProductoNombre());
            if (etUnidadesPorPaquete != null) etUnidadesPorPaquete.setText(String.valueOf(productoExistente.getUnidadesPorPaqueteVal()));
            if (etPrecioCompraPaquete != null) etPrecioCompraPaquete.setText(productoExistente.getPrecioCompraPaquete());
            if (etPrecioVentaPaquete != null) etPrecioVentaPaquete.setText(productoExistente.getPrecioVentaPaquete());
            if (etPrecioVentaUnidad != null) etPrecioVentaUnidad.setText(productoExistente.getPrecioVentaUnidad());
        } else {
            if (tvTitle != null) tvTitle.setText("Nuevo producto");
            if (tvSubtitle != null) tvSubtitle.setText("Ingresa los datos para registrar un nuevo producto");
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> {
                if (tilNombre != null) tilNombre.setError(null);
                if (tilProveedor != null) tilProveedor.setError(null);
                if (tilCategoria != null) tilCategoria.setError(null);
                if (tilUnidadesPorPaquete != null) tilUnidadesPorPaquete.setError(null);
                if (tilPrecioCompraPaquete != null) tilPrecioCompraPaquete.setError(null);
                if (tilPrecioVentaPaquete != null) tilPrecioVentaPaquete.setError(null);
                if (tilPrecioVentaUnidad != null) tilPrecioVentaUnidad.setError(null);

                String nombre = etNombre != null && etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
                String provText = actvProveedor != null && actvProveedor.getText() != null ? actvProveedor.getText().toString().trim() : "";
                String catText = actvCategoria != null && actvCategoria.getText() != null ? actvCategoria.getText().toString().trim() : "";
                String unidadesStr = etUnidadesPorPaquete != null && etUnidadesPorPaquete.getText() != null ? etUnidadesPorPaquete.getText().toString().trim() : "";
                String precioCompraPaqueteStr = etPrecioCompraPaquete != null && etPrecioCompraPaquete.getText() != null ? etPrecioCompraPaquete.getText().toString().trim() : "";
                String precioVentaPaqueteStr = etPrecioVentaPaquete != null && etPrecioVentaPaquete.getText() != null ? etPrecioVentaPaquete.getText().toString().trim() : "";
                String precioVentaUnidadStr = etPrecioVentaUnidad != null && etPrecioVentaUnidad.getText() != null ? etPrecioVentaUnidad.getText().toString().trim() : "";

                boolean esValido = true;

                if (TextUtils.isEmpty(nombre)) {
                    if (tilNombre != null) tilNombre.setError("Ingresa el nombre del producto");
                    esValido = false;
                }

                // Resolver Proveedor
                Proveedor proveedorSeleccionado = selectedProv[0];
                ArrayAdapter<Proveedor> provAdapter = actvProveedor != null && actvProveedor.getAdapter() instanceof ArrayAdapter ? (ArrayAdapter<Proveedor>) actvProveedor.getAdapter() : null;
                if (proveedorSeleccionado == null && provAdapter != null) {
                    for (int i = 0; i < provAdapter.getCount(); i++) {
                        Proveedor p = provAdapter.getItem(i);
                        if (p != null && (p.getNombreEmpresa().equalsIgnoreCase(provText) || p.toString().equalsIgnoreCase(provText))) {
                            proveedorSeleccionado = p;
                            break;
                        }
                    }
                }
                if (proveedorSeleccionado == null && provAdapter != null && provAdapter.getCount() > 0) {
                    proveedorSeleccionado = provAdapter.getItem(0);
                }
                if (proveedorSeleccionado == null) {
                    if (tilProveedor != null) tilProveedor.setError("Selecciona un proveedor válido");
                    esValido = false;
                }

                // Resolver Categoría
                Categoria categoriaSeleccionada = selectedCat[0];
                ArrayAdapter<Categoria> catAdapter = actvCategoria != null && actvCategoria.getAdapter() instanceof ArrayAdapter ? (ArrayAdapter<Categoria>) actvCategoria.getAdapter() : null;
                if (categoriaSeleccionada == null && catAdapter != null) {
                    for (int i = 0; i < catAdapter.getCount(); i++) {
                        Categoria c = catAdapter.getItem(i);
                        if (c != null && (c.getNombre().equalsIgnoreCase(catText) || c.toString().equalsIgnoreCase(catText))) {
                            categoriaSeleccionada = c;
                            break;
                        }
                    }
                }
                if (categoriaSeleccionada == null && catAdapter != null && catAdapter.getCount() > 0) {
                    categoriaSeleccionada = catAdapter.getItem(0);
                }
                if (categoriaSeleccionada == null) {
                    if (tilCategoria != null) tilCategoria.setError("Selecciona una categoría válida");
                    esValido = false;
                }

                if (TextUtils.isEmpty(unidadesStr)) {
                    if (tilUnidadesPorPaquete != null) tilUnidadesPorPaquete.setError("Ingresa unidades por paquete");
                    esValido = false;
                }

                if (TextUtils.isEmpty(precioCompraPaqueteStr)) {
                    if (tilPrecioCompraPaquete != null) tilPrecioCompraPaquete.setError("Ingresa precio compra paquete");
                    esValido = false;
                }

                if (TextUtils.isEmpty(precioVentaPaqueteStr)) {
                    if (tilPrecioVentaPaquete != null) tilPrecioVentaPaquete.setError("Ingresa precio venta paquete");
                    esValido = false;
                }

                if (TextUtils.isEmpty(precioVentaUnidadStr)) {
                    if (tilPrecioVentaUnidad != null) tilPrecioVentaUnidad.setError("Ingresa precio venta unidad");
                    esValido = false;
                }

                if (esValido) {
                    int unidadesPorPaquete = Integer.parseInt(unidadesStr);
                    int proveedorId = proveedorSeleccionado != null ? proveedorSeleccionado.getId() : 1;
                    int categoriaId = categoriaSeleccionada != null ? categoriaSeleccionada.getId() : 1;

                    Producto productoRequest = esEdicion ? productoExistente : new Producto();
                    productoRequest.setProductoNombre(nombre);
                    productoRequest.setUnidadesPorPaquete(unidadesPorPaquete);
                    productoRequest.setPrecioCompraPaquete(precioCompraPaqueteStr);
                    productoRequest.setPrecioVentaPaquete(precioVentaPaqueteStr);
                    productoRequest.setPrecioVentaUnidad(precioVentaUnidadStr);
                    productoRequest.setActivo(1);
                    productoRequest.setProveedorId(proveedorId);
                    productoRequest.setCategoriaId(categoriaId);

                    if (esEdicion) {
                        ejecutarEditarProducto(productoExistente.getProductoId(), productoRequest, dialog, btnGuardar);
                    } else {
                        ejecutarCrearProducto(productoRequest, dialog, btnGuardar);
                    }
                }
            });
        }

        dialog.show();
    }

    private void cargarProveedoresEnAutocomplete(AutoCompleteTextView actvProveedor, TextInputLayout tilProveedor, Producto productoExistente) {
        ApiClient.getApiService().getProveedores().enqueue(new Callback<ProveedorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProveedorResponse> call, @NonNull Response<ProveedorResponse> response) {
                List<Proveedor> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    lista = response.body().getData();
                }

                if (lista.isEmpty()) {
                    ApiClient.getApiService().getProveedoresDirectList().enqueue(new Callback<List<Proveedor>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Proveedor>> call2, @NonNull Response<List<Proveedor>> response2) {
                            List<Proveedor> lista2 = new ArrayList<>();
                            if (response2.isSuccessful() && response2.body() != null) {
                                lista2 = response2.body();
                            }
                            configurarAutoCompleteProveedores(actvProveedor, tilProveedor, lista2, productoExistente);
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<Proveedor>> call2, @NonNull Throwable t2) {
                            configurarAutoCompleteProveedores(actvProveedor, tilProveedor, new ArrayList<>(), productoExistente);
                        }
                    });
                } else {
                    configurarAutoCompleteProveedores(actvProveedor, tilProveedor, lista, productoExistente);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProveedorResponse> call, @NonNull Throwable t) {
                configurarAutoCompleteProveedores(actvProveedor, tilProveedor, new ArrayList<>(), productoExistente);
            }
        });
    }

    private void configurarAutoCompleteProveedores(AutoCompleteTextView actvProveedor, TextInputLayout tilProveedor, List<Proveedor> proveedores, Producto productoExistente) {
        if (proveedores.isEmpty()) {
            Proveedor pDefault = new Proveedor();
            pDefault.setId(1);
            pDefault.setNombreEmpresa("Proveedor General");
            proveedores.add(pDefault);
        }

        ArrayAdapter<Proveedor> adapterProv = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, proveedores);
        if (actvProveedor != null) {
            actvProveedor.setAdapter(adapterProv);
            actvProveedor.setThreshold(1);
            actvProveedor.setOnClickListener(v -> actvProveedor.showDropDown());

            if (productoExistente != null) {
                for (Proveedor p : proveedores) {
                    if (p.getId() == productoExistente.getProveedorId()) {
                        actvProveedor.setText(p.getNombreEmpresa(), false);
                        break;
                    }
                }
            } else if (!proveedores.isEmpty()) {
                actvProveedor.setText(proveedores.get(0).getNombreEmpresa(), false);
            }
        }
    }

    private void cargarCategoriasEnAutocomplete(AutoCompleteTextView actvCategoria, TextInputLayout tilCategoria, Producto productoExistente, Categoria categoriaASeleccionar) {
        ApiClient.getApiService().getCategoriasElement().enqueue(new Callback<com.google.gson.JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<com.google.gson.JsonElement> call, @NonNull Response<com.google.gson.JsonElement> response) {
                List<Categoria> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        com.google.gson.JsonElement json = response.body();
                        if (json.isJsonArray()) {
                            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Categoria>>() {}.getType();
                            lista = new Gson().fromJson(json, listType);
                        } else if (json.isJsonObject()) {
                            com.google.gson.JsonObject obj = json.getAsJsonObject();
                            com.google.gson.JsonElement dataEl = null;
                            if (obj.has("data")) dataEl = obj.get("data");
                            else if (obj.has("categorias")) dataEl = obj.get("categorias");
                            else if (obj.has("items")) dataEl = obj.get("items");

                            if (dataEl != null && dataEl.isJsonArray()) {
                                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Categoria>>() {}.getType();
                                lista = new Gson().fromJson(dataEl, listType);
                            }
                        }
                    } catch (Exception ignored) {}
                }
                configurarAutoCompleteCategorias(actvCategoria, tilCategoria, lista, productoExistente, categoriaASeleccionar);
            }

            @Override
            public void onFailure(@NonNull Call<com.google.gson.JsonElement> call, @NonNull Throwable t) {
                configurarAutoCompleteCategorias(actvCategoria, tilCategoria, new ArrayList<>(), productoExistente, categoriaASeleccionar);
            }
        });
    }

    private void configurarAutoCompleteCategorias(AutoCompleteTextView actvCategoria, TextInputLayout tilCategoria, List<Categoria> categorias, Producto productoExistente, Categoria categoriaASeleccionar) {
        if (categorias.isEmpty()) {
            Categoria cDefault = new Categoria("Bebidas", 1);
            cDefault.setId(1);
            categorias.add(cDefault);
        }

        ArrayAdapter<Categoria> adapterCat = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categorias);
        if (actvCategoria != null) {
            actvCategoria.setAdapter(adapterCat);
            actvCategoria.setThreshold(1);
            actvCategoria.setOnClickListener(v -> actvCategoria.showDropDown());

            if (categoriaASeleccionar != null) {
                for (Categoria c : categorias) {
                    if (c.getNombre().equalsIgnoreCase(categoriaASeleccionar.getNombre())) {
                        actvCategoria.setText(c.getNombre(), false);
                        break;
                    }
                }
            } else if (productoExistente != null) {
                for (Categoria c : categorias) {
                    if (c.getId() == productoExistente.getCategoriaId()) {
                        actvCategoria.setText(c.getNombre(), false);
                        break;
                    }
                }
            } else if (!categorias.isEmpty()) {
                actvCategoria.setText(categorias.get(0).getNombre(), false);
            }
        }
    }

    private void mostrarDialogoNuevaCategoriaParaAutocomplete(AutoCompleteTextView actvCategoria, TextInputLayout tilCategoria, Producto productoExistente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nueva_categoria, null);
        builder.setView(dialogView);

        AlertDialog dialogCat = builder.create();
        if (dialogCat.getWindow() != null) {
            dialogCat.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextInputLayout tilNombre = dialogView.findViewById(R.id.tilNombreCategoria);
        TextInputEditText etNombre = dialogView.findViewById(R.id.etNombreCategoria);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarCategoria);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnGuardarCategoria);

        if (btnCancelar != null) btnCancelar.setOnClickListener(v -> dialogCat.dismiss());

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> {
                if (tilNombre != null) tilNombre.setError(null);
                String nombreCat = etNombre != null && etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
                if (TextUtils.isEmpty(nombreCat)) {
                    if (tilNombre != null) tilNombre.setError("Ingresa el nombre de la categoría");
                    return;
                }

                if (btnGuardar != null) btnGuardar.setEnabled(false);
                Categoria nuevaCat = new Categoria(nombreCat, 1);

                ApiClient.getApiService().crearCategoria(nuevaCat).enqueue(new Callback<CategoriaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CategoriaResponse> call, @NonNull Response<CategoriaResponse> response) {
                        if (btnGuardar != null) btnGuardar.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(ProductosActivity.this, "¡Categoría registrada correctamente!", Toast.LENGTH_SHORT).show();
                            if (dialogCat != null && dialogCat.isShowing()) dialogCat.dismiss();
                            cargarCategoriasEnAutocomplete(actvCategoria, tilCategoria, productoExistente, nuevaCat);
                        } else {
                            Toast.makeText(ProductosActivity.this, "Error al crear categoría (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CategoriaResponse> call, @NonNull Throwable t) {
                        if (btnGuardar != null) btnGuardar.setEnabled(true);
                        Toast.makeText(ProductosActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        dialogCat.show();
    }

    private void ejecutarCrearProducto(Producto producto, AlertDialog dialog, MaterialButton btnGuardar) {
        if (btnGuardar != null) btnGuardar.setEnabled(false);

        ApiClient.getApiService().crearProducto(producto).enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductoResponse> call, @NonNull Response<ProductoResponse> response) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);

                if (response.code() == 401) {
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful()) {
                    Toast.makeText(ProductosActivity.this, "¡Producto creado correctamente!", Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    cargarProductos();
                } else {
                    String msg = "Error al crear producto (" + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errStr = response.errorBody().string();
                            ProductoResponse errRes = new Gson().fromJson(errStr, ProductoResponse.class);
                            if (errRes != null && errRes.getMessage() != null) {
                                msg = errRes.getMessage();
                            } else {
                                msg = "Error: " + errStr;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    Toast.makeText(ProductosActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductoResponse> call, @NonNull Throwable t) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);
                Toast.makeText(ProductosActivity.this, "Error de conexión al crear producto: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ejecutarEditarProducto(int id, Producto producto, AlertDialog dialog, MaterialButton btnGuardar) {
        if (btnGuardar != null) btnGuardar.setEnabled(false);

        ApiClient.getApiService().editarProducto(id, producto).enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductoResponse> call, @NonNull Response<ProductoResponse> response) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);

                if (response.code() == 401) {
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful()) {
                    Toast.makeText(ProductosActivity.this, "¡Producto actualizado correctamente!", Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    cargarProductos();
                } else {
                    String msg = "Error al actualizar producto (" + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errStr = response.errorBody().string();
                            ProductoResponse errRes = new Gson().fromJson(errStr, ProductoResponse.class);
                            if (errRes != null && errRes.getMessage() != null) {
                                msg = errRes.getMessage();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    Toast.makeText(ProductosActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductoResponse> call, @NonNull Throwable t) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);
                Toast.makeText(ProductosActivity.this, "Error de conexión al actualizar producto", Toast.LENGTH_LONG).show();
            }
        });
    }

    // =======================================================
    // ELIMINAR PRODUCTO
    // =======================================================

    private void mostrarDialogoConfirmarEliminar(Producto producto) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de eliminar el producto \"" + producto.getProductoNombre() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> ejecutarEliminarProducto(producto.getProductoId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarEliminarProducto(int id) {
        mostrarEstadoCarga();

        ApiClient.getApiService().eliminarProducto(id).enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductoResponse> call, @NonNull Response<ProductoResponse> response) {
                if (response.code() == 401) {
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful()) {
                    Toast.makeText(ProductosActivity.this, "¡Producto eliminado correctamente!", Toast.LENGTH_SHORT).show();
                    cargarProductos();
                } else {
                    Toast.makeText(ProductosActivity.this, "Error al eliminar producto (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    cargarProductos();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductoResponse> call, @NonNull Throwable t) {
                Toast.makeText(ProductosActivity.this, "Error de conexión al eliminar producto", Toast.LENGTH_SHORT).show();
                cargarProductos();
            }
        });
    }

    // =======================================================
    // MANEJO DE ESTADOS DE LA UI
    // =======================================================

    private void mostrarEstadoCarga() {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (rvProductos != null) rvProductos.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
    }

    private void ocultarSwipeRefresh() {
        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void mostrarListaProductos(List<Producto> productos) {
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);

        if (rvProductos != null && productos != null) {
            rvProductos.setVisibility(View.VISIBLE);

            List<Producto> listaMutable = new ArrayList<>(productos);
            android.util.Log.d("ADISAN_PRODUCTOS_DEBUG", "cantidad enviada al adapter = " + listaMutable.size());

            // Ordenar por ID descendente para que el nuevo producto creado aparezca primero en la parte superior
            Collections.sort(listaMutable, (p1, p2) -> Integer.compare(p2.getProductoId(), p1.getProductoId()));

            if (adapter == null) {
                adapter = new ProductoAdapter(this, listaMutable);
                rvProductos.setAdapter(adapter);
            } else {
                adapter.setProductos(listaMutable);
            }
            rvProductos.scrollToPosition(0);
        }
    }

    private void mostrarEstadoVacio() {
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (rvProductos != null) rvProductos.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.VISIBLE);
    }

    private void mostrarEstadoError(String mensaje) {
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (rvProductos != null) rvProductos.setVisibility(View.GONE);
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

        Intent intent = new Intent(ProductosActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}