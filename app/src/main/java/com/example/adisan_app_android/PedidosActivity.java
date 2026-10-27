package com.example.adisan_app_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedidosActivity extends AppCompatActivity {

    private ProgressBar pbLoading;
    private LinearLayout layoutVacio;
    private LinearLayout layoutError;
    private TextView tvMensajeError;
    private MaterialButton btnReintentar;
    private MaterialButton btnCrearPrimerPedido;
    private MaterialButton btnNuevoPedidoHeader;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvPedidos;
    private PedidoAdapter adapter;

    private MaterialButton chipTodosPed, chipPendientesPed, chipEnCaminoPed, chipEntregadosPed, chipCanceladosPed;
    private List<Pedido> listaPedidosCompleta = new ArrayList<>();
    private String filtroActual = "todos";

    private ImageView btnVolver;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_pedidos);
        } catch (Throwable e) {
            Toast.makeText(this, "CRASH setContentView: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            initViews();
        } catch (Throwable e) {
            Toast.makeText(this, "CRASH initViews: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            setupWindowInsets();
        } catch (Throwable e) {
            Toast.makeText(this, "CRASH setupWindowInsets: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            setupRecyclerView();
        } catch (Throwable e) {
            Toast.makeText(this, "CRASH setupRecyclerView: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            setupListeners();
        } catch (Throwable e) {
            Toast.makeText(this, "CRASH setupListeners: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            configurarTituloPorRol();
        } catch (Throwable e) {
            Toast.makeText(this, "CRASH configurarTituloPorRol: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            cargarPedidos();
        } catch (Throwable e) {
            Toast.makeText(this, "CRASH cargarPedidos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void configurarTituloPorRol() {
        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        String rol = preferences.getString("rol", "admin");

        TextView tvTitle = findViewById(R.id.tvHeaderTitle);
        if (tvTitle != null) {
            if ("cliente".equalsIgnoreCase(rol)) {
                tvTitle.setText("Mis pedidos");
            } else {
                tvTitle.setText("Pedidos");
            }
        }
    }

    private void initViews() {
        pbLoading = findViewById(R.id.pbLoading);
        layoutVacio = findViewById(R.id.layoutVacio);
        layoutError = findViewById(R.id.layoutError);
        tvMensajeError = findViewById(R.id.tvMensajeError);
        btnReintentar = findViewById(R.id.btnReintentar);
        btnCrearPrimerPedido = findViewById(R.id.btnCrearPrimerPedido);
        btnNuevoPedidoHeader = findViewById(R.id.btnNuevoPedidoHeader);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvPedidos = findViewById(R.id.rvPedidos);
        btnVolver = findViewById(R.id.btnVolver);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        chipTodosPed = findViewById(R.id.chipTodosPed);
        chipPendientesPed = findViewById(R.id.chipPendientesPed);
        chipEnCaminoPed = findViewById(R.id.chipEnCaminoPed);
        chipEntregadosPed = findViewById(R.id.chipEntregadosPed);
        chipCanceladosPed = findViewById(R.id.chipCanceladosPed);
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
        rvPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidoAdapter(this, new ArrayList<>());
        rvPedidos.setAdapter(adapter);

        // Listener para acciones de Ver Detalle, Cambiar Estado y Cancelar
        adapter.setOnPedidoActionListener(new PedidoAdapter.OnPedidoActionListener() {
            @Override
            public void onVerDetalle(Pedido pedido) {
                if (pedido != null && pedido.getId() > 0) {
                    Intent intent = new Intent(PedidosActivity.this, PedidoDetalleActivity.class);
                    intent.putExtra("pedido_id", pedido.getId());
                    intent.putExtra("id", pedido.getId());
                    intent.putExtra("id_pedido", pedido.getId());
                    startActivity(intent);
                } else {
                    Toast.makeText(PedidosActivity.this, "Pedido inválido", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEditarEstado(Pedido pedido) {
                mostrarDialogoCambiarEstado(pedido);
            }

            @Override
            public void onCancelar(Pedido pedido) {
                mostrarDialogoConfirmarCancelar(pedido);
            }
        });
    }

    private void setupListeners() {
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnNuevoPedidoHeader != null) {
            btnNuevoPedidoHeader.setOnClickListener(v -> mostrarDialogoFormularioPedido());
        }

        if (btnCrearPrimerPedido != null) {
            btnCrearPrimerPedido.setOnClickListener(v -> mostrarDialogoFormularioPedido());
        }

        if (btnReintentar != null) {
            btnReintentar.setOnClickListener(v -> cargarPedidos());
        }

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.blue_primary);
            swipeRefresh.setOnRefreshListener(this::cargarPedidos);
        }

        View.OnClickListener chipListener = v -> {
            if (v == chipTodosPed) filtroActual = "todos";
            else if (v == chipPendientesPed) filtroActual = "pendiente";
            else if (v == chipEnCaminoPed) filtroActual = "en_camino";
            else if (v == chipEntregadosPed) filtroActual = "entregado";
            else if (v == chipCanceladosPed) filtroActual = "cancelado";
            
            actualizarEstiloChips((MaterialButton) v);
            aplicarFiltroLocal();
        };

        if (chipTodosPed != null) chipTodosPed.setOnClickListener(chipListener);
        if (chipPendientesPed != null) chipPendientesPed.setOnClickListener(chipListener);
        if (chipEnCaminoPed != null) chipEnCaminoPed.setOnClickListener(chipListener);
        if (chipEntregadosPed != null) chipEntregadosPed.setOnClickListener(chipListener);
        if (chipCanceladosPed != null) chipCanceladosPed.setOnClickListener(chipListener);

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_pedidos);
            bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_pedidos) {
                        return true;
                    } else if (itemId == R.id.nav_dashboard) {
                        startActivity(DashboardActivity.volver(PedidosActivity.this, false));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_productos) {
                        Intent intent = new Intent(PedidosActivity.this, ProductosActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_mas) {
                        // "Más" abre el menú lateral del dashboard, igual que desde el dashboard
                        startActivity(DashboardActivity.volver(PedidosActivity.this, true));
                        finish();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void cargarPedidos() {
        try {
            if (swipeRefresh != null && !swipeRefresh.isRefreshing()) {
                mostrarEstadoCarga();
            }

            // 1. Probar endpoint envuelto { "data": [ ... ] }
            ApiClient.getApiService().getPedidos().enqueue(new Callback<PedidoResponse>() {
                @Override
                public void onResponse(@NonNull Call<PedidoResponse> call, @NonNull Response<PedidoResponse> response) {
                    try {
                        ocultarSwipeRefresh();

                        if (response.code() == 401) {
                            manejarSesionExpirada();
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            PedidoResponse body = response.body();
                            listaPedidosCompleta = body.getData() != null ? body.getData() : new ArrayList<>();

                            if (!listaPedidosCompleta.isEmpty()) {
                                aplicarFiltroLocal();
                            } else {
                                mostrarEstadoVacio();
                            }
                        } else {
                            // Fallback a lista directa si el JSON es un arreglo [ {...} ]
                            cargarPedidosListaDirecta();
                        }
                    } catch (Throwable e) {
                        android.util.Log.e("CRASH_DEBUG", "CRASH in onResponse: " + e.getMessage(), e);
                        Toast.makeText(PedidosActivity.this, "CRASH onResponse: " + e.getClass().getSimpleName() + " - " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PedidoResponse> call, @NonNull Throwable t) {
                    try {
                        cargarPedidosListaDirecta();
                    } catch (Throwable e) {
                        Toast.makeText(PedidosActivity.this, "CRASH onFailure: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Throwable e) {
            Toast.makeText(this, "CRASH cargarPedidos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cargarPedidosListaDirecta() {
        try {
            ApiClient.getApiService().getPedidosDirectList().enqueue(new Callback<List<Pedido>>() {
                @Override
                public void onResponse(@NonNull Call<List<Pedido>> call, @NonNull Response<List<Pedido>> response) {
                    try {
                        ocultarSwipeRefresh();

                        if (response.code() == 401) {
                            manejarSesionExpirada();
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            listaPedidosCompleta = response.body();
                            if (listaPedidosCompleta != null && !listaPedidosCompleta.isEmpty()) {
                                aplicarFiltroLocal();
                            } else {
                                mostrarEstadoVacio();
                            }
                        } else {
                            mostrarEstadoError("Error en el servidor (" + response.code() + ")");
                        }
                    } catch (Throwable e) {
                        android.util.Log.e("CRASH_DEBUG", "CRASH in DirectList onResponse: " + e.getMessage(), e);
                        Toast.makeText(PedidosActivity.this, "CRASH DirectList onResponse: " + e.getClass().getSimpleName() + " - " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Pedido>> call, @NonNull Throwable t) {
                    try {
                        ocultarSwipeRefresh();
                        mostrarEstadoError("Sin conexión a internet. Revisa tu red e inténtalo de nuevo.");
                    } catch (Throwable e) {
                        Toast.makeText(PedidosActivity.this, "CRASH DirectList onFailure: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Throwable e) {
            Toast.makeText(this, "CRASH cargarPedidosListaDirecta: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // =======================================================
    // CREAR NUEVO PEDIDO CON SELECCIÓN DE CLIENTE Y PRODUCTOS
    // =======================================================

    private void mostrarDialogoFormularioPedido() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pedido, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Spinner spinnerClientes = dialogView.findViewById(R.id.spinnerClientesPedido);
        MaterialButton btnRegistrarCliente = dialogView.findViewById(R.id.btnAbrirModalNuevoCliente);

        TextInputLayout tilMetodoPago = dialogView.findViewById(R.id.tilMetodoPagoPedido);
        TextInputEditText etMetodoPago = dialogView.findViewById(R.id.etMetodoPago);

        ProgressBar pbLoadingProd = dialogView.findViewById(R.id.pbLoadingProductosPedido);
        RecyclerView rvSeleccion = dialogView.findViewById(R.id.rvSeleccionProductosPedido);
        TextView tvTotalCalculado = dialogView.findViewById(R.id.tvTotalPedidoCalculado);

        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarDialogPedido);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnGuardarDialogPedido);

        if (rvSeleccion != null) {
            rvSeleccion.setLayoutManager(new LinearLayoutManager(this));
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        // Cargar clientes reales en el Spinner
        cargarClientesEnSpinner(spinnerClientes);

        // Botón Registrar Nuevo Cliente
        if (btnRegistrarCliente != null) {
            btnRegistrarCliente.setOnClickListener(v -> mostrarDialogoNuevoCliente(spinnerClientes));
        }

        // Cargar productos del catálogo
        if (pbLoadingProd != null) pbLoadingProd.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getProductos().enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductoResponse> call, @NonNull Response<ProductoResponse> response) {
                if (pbLoadingProd != null) pbLoadingProd.setVisibility(View.GONE);

                List<Producto> productos = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    productos = response.body().getData();
                }

                if (productos.isEmpty()) {
                    cargarProductosModalDirecto(rvSeleccion, tvTotalCalculado, btnGuardar, spinnerClientes, etMetodoPago, tilMetodoPago, dialog);
                } else {
                    configurarModalProductosPedido(productos, rvSeleccion, tvTotalCalculado, btnGuardar, spinnerClientes, etMetodoPago, tilMetodoPago, dialog);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductoResponse> call, @NonNull Throwable t) {
                cargarProductosModalDirecto(rvSeleccion, tvTotalCalculado, btnGuardar, spinnerClientes, etMetodoPago, tilMetodoPago, dialog);
            }
        });

        dialog.show();
    }

    private void cargarClientesEnSpinner(Spinner spinnerClientes) {
        ApiClient.getApiService().getUsuarios().enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResponse> call, @NonNull Response<UsuarioResponse> response) {
                List<Usuario> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    lista = response.body().getData();
                }

                if (lista.isEmpty()) {
                    cargarClientesModalDirecto(spinnerClientes);
                } else {
                    configurarSpinnerClientes(spinnerClientes, lista, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResponse> call, @NonNull Throwable t) {
                cargarClientesModalDirecto(spinnerClientes);
            }
        });
    }

    private void cargarClientesModalDirecto(Spinner spinnerClientes) {
        ApiClient.getApiService().getUsuariosDirectList().enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(@NonNull Call<List<Usuario>> call, @NonNull Response<List<Usuario>> response) {
                List<Usuario> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    lista = response.body();
                }
                configurarSpinnerClientes(spinnerClientes, lista, null);
            }

            @Override
            public void onFailure(@NonNull Call<List<Usuario>> call, @NonNull Throwable t) {
                configurarSpinnerClientes(spinnerClientes, new ArrayList<>(), null);
            }
        });
    }

    private void configurarSpinnerClientes(Spinner spinnerClientes, List<Usuario> usuarios, Usuario usuarioASeleccionar) {
        if (usuarios.isEmpty()) {
            Usuario defaultUser = new Usuario();
            defaultUser.setId(1);
            defaultUser.setNombres("Cliente General");
            defaultUser.setApellidos("");
            usuarios.add(defaultUser);
        }

        ArrayAdapter<Usuario> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, usuarios);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerClientes != null) {
            spinnerClientes.setAdapter(adapterSpinner);

            if (usuarioASeleccionar != null) {
                for (int i = 0; i < usuarios.size(); i++) {
                    if (usuarios.get(i).getId() == usuarioASeleccionar.getId()) {
                        spinnerClientes.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    // Modal para registrar nuevo cliente
    private void mostrarDialogoNuevoCliente(Spinner spinnerClientes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nuevo_cliente, null);
        builder.setView(dialogView);

        AlertDialog dialogCliente = builder.create();
        if (dialogCliente.getWindow() != null) {
            dialogCliente.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextInputLayout tilNombres = dialogView.findViewById(R.id.tilNombresCliente);
        TextInputLayout tilApellidos = dialogView.findViewById(R.id.tilApellidosCliente);
        TextInputLayout tilDni = dialogView.findViewById(R.id.tilDniCliente);
        TextInputLayout tilTelefono = dialogView.findViewById(R.id.tilTelefonoCliente);
        TextInputLayout tilCorreo = dialogView.findViewById(R.id.tilCorreoCliente);
        TextInputLayout tilDireccion = dialogView.findViewById(R.id.tilDireccionCliente);

        TextInputEditText etNombres = dialogView.findViewById(R.id.etNombresCliente);
        TextInputEditText etApellidos = dialogView.findViewById(R.id.etApellidosCliente);
        TextInputEditText etDni = dialogView.findViewById(R.id.etDniCliente);
        TextInputEditText etTelefono = dialogView.findViewById(R.id.etTelefonoCliente);
        TextInputEditText etCorreo = dialogView.findViewById(R.id.etCorreoCliente);
        TextInputEditText etDireccion = dialogView.findViewById(R.id.etDireccionCliente);

        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarNuevoCliente);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnGuardarNuevoCliente);

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialogCliente.dismiss());
        }

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> {
                if (tilNombres != null) tilNombres.setError(null);
                if (tilApellidos != null) tilApellidos.setError(null);
                if (tilDni != null) tilDni.setError(null);
                if (tilTelefono != null) tilTelefono.setError(null);
                if (tilCorreo != null) tilCorreo.setError(null);

                String nombres = etNombres != null && etNombres.getText() != null ? etNombres.getText().toString().trim() : "";
                String apellidos = etApellidos != null && etApellidos.getText() != null ? etApellidos.getText().toString().trim() : "";
                String dni = etDni != null && etDni.getText() != null ? etDni.getText().toString().trim() : "";
                String telefono = etTelefono != null && etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";
                String correo = etCorreo != null && etCorreo.getText() != null ? etCorreo.getText().toString().trim() : "";
                String direccion = etDireccion != null && etDireccion.getText() != null ? etDireccion.getText().toString().trim() : "";

                boolean esValido = true;

                if (TextUtils.isEmpty(nombres)) {
                    if (tilNombres != null) tilNombres.setError("Ingresa los nombres");
                    esValido = false;
                }

                if (TextUtils.isEmpty(apellidos)) {
                    if (tilApellidos != null) tilApellidos.setError("Ingresa los apellidos");
                    esValido = false;
                }

                if (TextUtils.isEmpty(dni) || dni.length() != 8) {
                    if (tilDni != null) tilDni.setError("El DNI debe tener 8 dígitos");
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
                    Usuario nuevoCliente = new Usuario();
                    nuevoCliente.setNombres(nombres);
                    nuevoCliente.setApellidos(apellidos);
                    nuevoCliente.setDni(dni);
                    nuevoCliente.setTelefono(telefono);
                    nuevoCliente.setCorreo(correo);
                    nuevoCliente.setDireccion(direccion);
                    nuevoCliente.setUsuario(dni); // Usuario = DNI por defecto
                    nuevoCliente.setPassword(dni); // Password = DNI por defecto
                    nuevoCliente.setRol("cliente");

                    ejecutarCrearCliente(nuevoCliente, dialogCliente, btnGuardar, spinnerClientes);
                }
            });
        }

        dialogCliente.show();
    }

    private void ejecutarCrearCliente(Usuario cliente, AlertDialog dialogCliente, MaterialButton btnGuardar, Spinner spinnerClientes) {
        if (btnGuardar != null) btnGuardar.setEnabled(false);

        ApiClient.getApiService().crearUsuario(cliente).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResponse> call, @NonNull Response<UsuarioResponse> response) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(PedidosActivity.this, "¡Cliente registrado correctamente!", Toast.LENGTH_SHORT).show();
                    if (dialogCliente != null && dialogCliente.isShowing()) dialogCliente.dismiss();

                    // Recargar clientes en el Spinner y seleccionar el cliente recién creado
                    cargarClientesEnSpinner(spinnerClientes);
                } else {
                    String msg = "Error al registrar cliente";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            String errStr = response.errorBody().string();
                            UsuarioResponse errRes = new Gson().fromJson(errStr, UsuarioResponse.class);
                            if (errRes != null && errRes.getMessage() != null) {
                                msg = errRes.getMessage();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    Toast.makeText(PedidosActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResponse> call, @NonNull Throwable t) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);
                Toast.makeText(PedidosActivity.this, "Error de conexión al registrar cliente", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargarProductosModalDirecto(RecyclerView rvSeleccion, TextView tvTotalCalculado, MaterialButton btnGuardar,
                                            Spinner spinnerClientes, TextInputEditText etMetodoPago,
                                            TextInputLayout tilMetodoPago, AlertDialog dialog) {
        ApiClient.getApiService().getProductosDirectList().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(@NonNull Call<List<Producto>> call, @NonNull Response<List<Producto>> response) {
                List<Producto> productos = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    productos = response.body();
                }
                configurarModalProductosPedido(productos, rvSeleccion, tvTotalCalculado, btnGuardar, spinnerClientes, etMetodoPago, tilMetodoPago, dialog);
            }

            @Override
            public void onFailure(@NonNull Call<List<Producto>> call, @NonNull Throwable t) {
                configurarModalProductosPedido(new ArrayList<>(), rvSeleccion, tvTotalCalculado, btnGuardar, spinnerClientes, etMetodoPago, tilMetodoPago, dialog);
            }
        });
    }

    private void configurarModalProductosPedido(List<Producto> productos, RecyclerView rvSeleccion, TextView tvTotalCalculado,
                                                MaterialButton btnGuardar, Spinner spinnerClientes, TextInputEditText etMetodoPago,
                                                TextInputLayout tilMetodoPago, AlertDialog dialog) {

        int[] cantidades = new int[productos.size()];

        SeleccionPedidoAdapter adapterSeleccion = new SeleccionPedidoAdapter(this, productos, cantidades, total -> {
            if (tvTotalCalculado != null) {
                tvTotalCalculado.setText(String.format(Locale.US, "S/ %.2f", total));
            }
        });

        if (rvSeleccion != null) {
            rvSeleccion.setAdapter(adapterSeleccion);
        }

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> {
                if (tilMetodoPago != null) tilMetodoPago.setError(null);

                String metodoPago = etMetodoPago != null && etMetodoPago.getText() != null ? etMetodoPago.getText().toString().trim() : "Yape";

                int clienteId = 1;
                if (spinnerClientes != null && spinnerClientes.getSelectedItem() instanceof Usuario) {
                    Usuario userSel = (Usuario) spinnerClientes.getSelectedItem();
                    clienteId = userSel.getId();
                }

                double totalCalculado = adapterSeleccion.obtenerTotalCalculado();
                if (totalCalculado <= 0) {
                    Toast.makeText(PedidosActivity.this, "Selecciona al menos 1 producto para crear el pedido", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<PedidoItemRequest> itemsRequest = new ArrayList<>();
                for (int i = 0; i < productos.size(); i++) {
                    int cant = cantidades[i];
                    if (cant > 0) {
                        Producto prod = productos.get(i);
                        itemsRequest.add(new PedidoItemRequest(
                                prod.getProductoId(),
                                "PAQUETE",
                                cant,
                                0
                        ));
                    }
                }
                String totalStr = String.format(Locale.US, "%.2f", totalCalculado);
                PedidoRequest pedidoRequest = new PedidoRequest(clienteId, metodoPago, totalStr, itemsRequest);

                ejecutarCrearPedidoNuevo(pedidoRequest, dialog, btnGuardar);
            });
        }
    }

    private void ejecutarCrearPedidoNuevo(PedidoRequest request, AlertDialog dialog, MaterialButton btnGuardar) {
        if (btnGuardar != null) btnGuardar.setEnabled(false);

        ApiClient.getApiService().crearPedidoNuevo(request).enqueue(new Callback<PedidoResponse>() {
            @Override
            public void onResponse(@NonNull Call<PedidoResponse> call, @NonNull Response<PedidoResponse> response) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);

                if (response.code() == 401) {
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(PedidosActivity.this, "¡Pedido registrado correctamente!", Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    cargarPedidos();
                } else {
                    String msg = "Error al crear pedido (" + response.code() + ")";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            String errStr = response.errorBody().string();
                            PedidoResponse errRes = new Gson().fromJson(errStr, PedidoResponse.class);
                            if (errRes != null && errRes.getMessage() != null) {
                                msg = errRes.getMessage();
                            }
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(PedidosActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PedidoResponse> call, @NonNull Throwable t) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);
                Toast.makeText(PedidosActivity.this, "Error de conexión al crear pedido: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // =======================================================
    // DETALLE DE PEDIDO
    // =======================================================

    private void cargarDetalleYMostrar(int pedidoId) {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getPedidoById(pedidoId).enqueue(new Callback<Pedido>() {
            @Override
            public void onResponse(@NonNull Call<Pedido> call, @NonNull Response<Pedido> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                if (response.code() == 401) {
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    Pedido pedidoCompleto = response.body();
                    mostrarDialogoDetallePedido(pedidoCompleto);
                } else {
                    Toast.makeText(PedidosActivity.this, "No se pudo cargar el detalle del pedido", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pedido> call, @NonNull Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                Toast.makeText(PedidosActivity.this, "Error de conexión al cargar detalle del pedido", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoDetallePedido(Pedido pedido) {
        if (pedido == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_detalle_pedido, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvTitleDetallePedido);
        TextView tvInfo = dialogView.findViewById(R.id.tvInfoDetallePedido);
        RecyclerView rvDetalles = dialogView.findViewById(R.id.rvDetallesPedidoModal);
        MaterialButton btnCerrar = dialogView.findViewById(R.id.btnCerrarDetallePedido);

        if (tvTitle != null) {
            tvTitle.setText("Pedido #" + pedido.getId());
        }

        if (tvInfo != null) {
            String clienteStr = pedido.getCliente() != null ? pedido.getCliente() : "Venta directa";
            String fechaStr = pedido.getFecha() != null ? pedido.getFecha() : "Pendiente";
            String pagoStr = pedido.getMetodoPago() != null ? pedido.getMetodoPago() : "Efectivo";
            String estadoStr = pedido.getEstado() != null ? pedido.getEstado() : "pendiente";
            double totalVal = pedido.getTotalDouble();

            String info = "Cliente: " + clienteStr +
                    "\nFecha: " + fechaStr +
                    "\nMétodo de Pago: " + pagoStr +
                    "\nEstado: " + estadoStr.toUpperCase() +
                    "\nTotal: S/ " + String.format(Locale.US, "%.2f", totalVal);
            tvInfo.setText(info);
        }

        if (rvDetalles != null) {
            rvDetalles.setLayoutManager(new LinearLayoutManager(this));
            DetallePedidoAdapter detAdapter = new DetallePedidoAdapter(this, pedido.getDetalles());
            rvDetalles.setAdapter(detAdapter);
        }

        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    // =======================================================
    // CAMBIAR ESTADO DE PEDIDO
    // =======================================================

    private void mostrarDialogoCambiarEstado(Pedido pedido) {
        String[] estadosDisplay = {"Pendiente", "Aceptado", "En camino", "Entregado"};
        String[] estadosValor = {"pendiente", "aceptado", "en_camino", "entregado"};
        
        int seleccionActual = 0;
        for (int i = 0; i < estadosValor.length; i++) {
            if (estadosValor[i].equalsIgnoreCase(pedido.getEstado())) {
                seleccionActual = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Cambiar Estado del Pedido #" + pedido.getId())
                .setSingleChoiceItems(estadosDisplay, seleccionActual, (dialog, which) -> {
                    String nuevoEstado = estadosValor[which];
                    dialog.dismiss();
                    ejecutarActualizarEstado(pedido.getId(), nuevoEstado);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarActualizarEstado(int pedidoId, String nuevoEstado) {
        mostrarEstadoCarga();

        String estadoLower = nuevoEstado != null ? nuevoEstado.toLowerCase() : "pendiente";

        Pedido updateRequest = new Pedido();
        updateRequest.setEstado(estadoLower);

        ApiClient.getApiService().editarPedido(pedidoId, updateRequest).enqueue(new Callback<PedidoResponse>() {
            @Override
            public void onResponse(@NonNull Call<PedidoResponse> call, @NonNull Response<PedidoResponse> response) {
                if (response.code() == 401) {
                    manejarSesionExpirada();
                    return;
                }

                if (response.isSuccessful()) {
                    String displayStr = estadoLower.substring(0, 1).toUpperCase() + estadoLower.substring(1).replace("_", " ");
                    Toast.makeText(PedidosActivity.this, "¡Estado actualizado a " + displayStr + "!", Toast.LENGTH_SHORT).show();
                    cargarPedidos();
                } else {
                    String errorMsg = "Error al actualizar estado (" + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String errStr = response.errorBody().string();
                            PedidoResponse errRes = new com.google.gson.Gson().fromJson(errStr, PedidoResponse.class);
                            if (errRes != null && errRes.getMessage() != null) {
                                errorMsg = errRes.getMessage();
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(PedidosActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    cargarPedidos();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PedidoResponse> call, @NonNull Throwable t) {
                Toast.makeText(PedidosActivity.this, "Error de conexión al actualizar estado", Toast.LENGTH_SHORT).show();
                cargarPedidos();
            }
        });
    }

    // =======================================================
    // CANCELAR / ELIMINAR PEDIDO
    // =======================================================

    private void mostrarDialogoConfirmarCancelar(Pedido pedido) {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar Pedido")
                .setMessage("¿Estás seguro de cambiar a CANCELADO el pedido #" + pedido.getId() + "?")
                .setPositiveButton("Confirmar", (dialog, which) -> ejecutarActualizarEstado(pedido.getId(), "CANCELADO"))
                .setNegativeButton("Volver", null)
                .show();
    }

    // =======================================================
    // MANEJO DE ESTADOS DE LA UI Y FILTROS
    // =======================================================

    private void aplicarFiltroLocal() {
        if (listaPedidosCompleta == null || listaPedidosCompleta.isEmpty()) {
            mostrarEstadoVacio();
            return;
        }

        List<Pedido> filtrados = new ArrayList<>();
        for (Pedido p : listaPedidosCompleta) {
            String estadoStr = p.getEstado() != null ? p.getEstado().toLowerCase() : "pendiente";
            if ("todos".equals(filtroActual)) {
                filtrados.add(p);
            } else if ("en_camino".equals(filtroActual) && ("en_camino".equals(estadoStr) || "en camino".equals(estadoStr))) {
                filtrados.add(p);
            } else if (filtroActual.equals(estadoStr)) {
                filtrados.add(p);
            }
        }

        if (filtrados.isEmpty()) {
            mostrarEstadoVacio();
        } else {
            mostrarListaPedidos(filtrados);
        }
    }

    private void actualizarEstiloChips(MaterialButton seleccionado) {
        MaterialButton[] chips = {chipTodosPed, chipPendientesPed, chipEnCaminoPed, chipEntregadosPed, chipCanceladosPed};
        for (MaterialButton c : chips) {
            if (c == null) continue;
            if (c == seleccionado) {
                c.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F97316")));
                c.setTextColor(android.graphics.Color.WHITE);
                c.setStrokeWidth(0);
            } else {
                c.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1E293B")));
                c.setTextColor(android.graphics.Color.parseColor("#94A3B8"));
                c.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2E3D59")));
                c.setStrokeWidth(1);
            }
        }
    }

    private void mostrarEstadoCarga() {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (rvPedidos != null) rvPedidos.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
    }

    private void ocultarSwipeRefresh() {
        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void mostrarListaPedidos(List<Pedido> pedidos) {
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
        if (rvPedidos != null) {
            rvPedidos.setVisibility(View.VISIBLE);
            adapter.setPedidos(pedidos);
        }
    }

    private void mostrarEstadoVacio() {
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (rvPedidos != null) rvPedidos.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.VISIBLE);
    }

    private void mostrarEstadoError(String mensaje) {
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (rvPedidos != null) rvPedidos.setVisibility(View.GONE);
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

        Intent intent = new Intent(PedidosActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // =======================================================
    // ADAPTER INTERNO PARA SELECCIONAR PRODUCTOS DEL PEDIDO
    // =======================================================

    private static class SeleccionPedidoAdapter extends RecyclerView.Adapter<SeleccionPedidoAdapter.SeleccionViewHolder> {

        interface OnTotalChangeListener {
            void onTotalChange(double total);
        }

        private final Context context;
        private final List<Producto> productoList;
        private final int[] cantidades;
        private final OnTotalChangeListener totalChangeListener;

        public SeleccionPedidoAdapter(Context context, List<Producto> productoList, int[] cantidades, OnTotalChangeListener listener) {
            this.context = context;
            this.productoList = productoList != null ? productoList : new ArrayList<>();
            this.cantidades = cantidades;
            this.totalChangeListener = listener;
        }

        public double obtenerTotalCalculado() {
            double total = 0;
            for (int i = 0; i < productoList.size(); i++) {
                int cant = cantidades[i];
                if (cant > 0) {
                    total += cant * productoList.get(i).getPrecioVentaDouble();
                }
            }
            return total;
        }

        @NonNull
        @Override
        public SeleccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_producto_seleccion, parent, false);
            return new SeleccionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SeleccionViewHolder holder, int position) {
            Producto producto = productoList.get(position);
            int cant = cantidades[position];

            holder.tvNombre.setText(producto.getProductoNombre());
            holder.tvInfo.setText(String.format(Locale.US, "S/ %.2f • Stock: %d", producto.getPrecioVentaDouble(), producto.getStock()));
            holder.tvCantidad.setText(String.valueOf(cant));

            double subtotal = cant * producto.getPrecioVentaDouble();
            holder.tvSubtotal.setText(String.format(Locale.US, "Subtotal: S/ %.2f", subtotal));

            holder.btnMas.setOnClickListener(v -> {
                if (cantidades[position] < producto.getStock()) {
                    cantidades[position]++;
                    notifyItemChanged(position);
                    if (totalChangeListener != null) {
                        totalChangeListener.onTotalChange(obtenerTotalCalculado());
                    }
                } else {
                    Toast.makeText(context, "Stock máximo alcanzado (" + producto.getStock() + ")", Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnMenos.setOnClickListener(v -> {
                if (cantidades[position] > 0) {
                    cantidades[position]--;
                    notifyItemChanged(position);
                    if (totalChangeListener != null) {
                        totalChangeListener.onTotalChange(obtenerTotalCalculado());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return productoList != null ? productoList.size() : 0;
        }

        static class SeleccionViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre;
            TextView tvInfo;
            TextView tvSubtotal;
            TextView tvCantidad;
            MaterialButton btnMenos;
            MaterialButton btnMas;

            public SeleccionViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvNombreItemVenta);
                tvInfo = itemView.findViewById(R.id.tvInfoItemVenta);
                tvSubtotal = itemView.findViewById(R.id.tvSubtotalItemVenta);
                tvCantidad = itemView.findViewById(R.id.tvCantidadItem);
                btnMenos = itemView.findViewById(R.id.btnMenosCantidad);
                btnMas = itemView.findViewById(R.id.btnMasCantidad);
            }
        }
    }
}