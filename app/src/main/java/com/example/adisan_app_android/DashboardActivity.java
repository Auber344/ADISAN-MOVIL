package com.example.adisan_app_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    private TextView tvGreeting;
    private TextView tvSubGreeting;
    private TextView tvAvatarInitial;

    // Tarjetas principales
    private TextView tvValProductos;
    private TextView tvValUsuarios;
    private TextView tvValPedidos;
    private TextView tvValVentas;
    private TextView tvValStockBajo;
    private TextView tvValEnCamino;
    private TextView tvTotalVendido;
    private TextView tvValOfertas;
    private TextView tvValReportes;
    private TextView tvChipEnRuta;

    // Alertas
    private TextView tvAlertStockBajoText;
    private TextView tvAlertEnCaminoText;

    private MaterialCardView cardProductos;
    private MaterialCardView cardUsuarios;
    private MaterialCardView cardPedidos;
    private MaterialCardView cardVentas;
    private MaterialCardView cardStockBajo;
    private MaterialCardView cardEnCamino;

    // Accesos rápidos
    private MaterialCardView cardReportesFinancieros;
    private MaterialCardView cardQuickProductos;
    private MaterialCardView cardQuickPedidos;
    private MaterialCardView cardQuickOfertas;
    private MaterialCardView cardQuickProveedores;

    // Navegación e iconos superiores
    private BottomNavigationView bottomNavigation;
    private ImageView btnNotifications;
    private ImageView btnAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Inicializar vistas
        initViews();

        // 2. Cargar datos de usuario guardados en SharedPreferences
        cargarDatosUsuario();

        // 3. Cargar métricas del dashboard
        obtenerDatosDashboard();

        // 4. Configurar listeners de interacción y Sidebar
        setupListeners();
        setupSidebar();

        if (getIntent().getBooleanExtra(EXTRA_ABRIR_MENU, false) && drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private static final String EXTRA_ABRIR_MENU = "abrir_menu";

    /** Vuelve al dashboard ya abierto (sin apilar otro) y opcionalmente abre el menú lateral. */
    static Intent volver(android.content.Context origen, boolean abrirMenu) {
        Intent intent = new Intent(origen, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_ABRIR_MENU, abrirMenu);
        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(EXTRA_ABRIR_MENU, false) && drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver desde Productos o Pedidos la barra inferior seguía marcando esa pestaña
        if (bottomNavigation != null && bottomNavigation.getSelectedItemId() != R.id.nav_dashboard) {
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        }
        // Recargar siempre los datos del Dashboard al retomar el foco
        obtenerDatosDashboard();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvSubGreeting = findViewById(R.id.tvSubGreeting);
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial);

        tvValProductos = findViewById(R.id.tvValProductos);
        tvValUsuarios = findViewById(R.id.tvValUsuarios);
        tvValPedidos = findViewById(R.id.tvValPedidos);
        tvValVentas = findViewById(R.id.tvValVentas);
        tvValStockBajo = findViewById(R.id.tvValStockBajo);
        tvValEnCamino = findViewById(R.id.tvValEnCamino);
        tvTotalVendido = findViewById(R.id.tvTotalVendido);
        tvValOfertas = findViewById(R.id.tvValOfertas);
        tvValReportes = findViewById(R.id.tvValReportes);
        tvChipEnRuta = findViewById(R.id.tvChipEnRuta);

        tvAlertStockBajoText = findViewById(R.id.tvAlertStockBajoText);
        tvAlertEnCaminoText = findViewById(R.id.tvAlertEnCaminoText);

        cardProductos = findViewById(R.id.cardProductos);
        cardUsuarios = findViewById(R.id.cardUsuarios);
        cardPedidos = findViewById(R.id.cardPedidos);
        cardVentas = findViewById(R.id.cardVentas);
        cardStockBajo = findViewById(R.id.cardStockBajo);
        cardEnCamino = findViewById(R.id.cardEnCamino);

        cardReportesFinancieros = findViewById(R.id.cardReportesFinancieros);
        cardQuickProductos = findViewById(R.id.cardQuickProductos);
        cardQuickPedidos = findViewById(R.id.cardQuickPedidos);
        cardQuickOfertas = findViewById(R.id.cardQuickOfertas);
        cardQuickProveedores = findViewById(R.id.cardQuickProveedores);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnAvatar = findViewById(R.id.btnAvatar);
    }

    private void setupSidebar() {
        // Abrir Sidebar al tocar el botón de menú hamburguesa
        View.OnClickListener openSidebarListener = v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        };

        View btnMenuHamburguesa = findViewById(R.id.btnMenuHamburguesa);
        if (btnMenuHamburguesa != null) btnMenuHamburguesa.setOnClickListener(openSidebarListener);

        // Vistas dentro del Sidebar
        ImageView btnCerrarSidebar = findViewById(R.id.btnCerrarSidebar);
        View navPerfil = findViewById(R.id.navSidebarPerfil);
        View navInicio = findViewById(R.id.navSidebarInicio);
        View navProductos = findViewById(R.id.navSidebarProductos);
        View navPedidos = findViewById(R.id.navSidebarPedidos);
        View navProveedores = findViewById(R.id.navSidebarProveedores);
        View navUsuarios = findViewById(R.id.navSidebarUsuarios);
        View navReportes = findViewById(R.id.navSidebarReportes);
        View navConfiguracion = findViewById(R.id.navSidebarConfiguracion);
        View navCerrarSesion = findViewById(R.id.navSidebarCerrarSesion);

        if (btnCerrarSidebar != null) {
            btnCerrarSidebar.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        if (navPerfil != null) {
            navPerfil.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                mostrarDialogoPerfilUsuario();
            });
        }

        if (navInicio != null) {
            navInicio.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        if (navProductos != null) {
            navProductos.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(DashboardActivity.this, ProductosActivity.class);
                startActivity(intent);
            });
        }

        if (navPedidos != null) {
            navPedidos.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(DashboardActivity.this, PedidosActivity.class);
                startActivity(intent);
            });
        }

        if (navProveedores != null) {
            navProveedores.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(DashboardActivity.this, ProveedoresActivity.class);
                startActivity(intent);
            });
        }

        if (navUsuarios != null) {
            navUsuarios.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                mostrarDialogoListaUsuarios();
            });
        }

        if (navReportes != null) {
            navReportes.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(DashboardActivity.this, ReportesFinancierosActivity.class);
                startActivity(intent);
            });
        }

        if (navConfiguracion != null) {
            navConfiguracion.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                mostrarAcercaDe();
            });
        }

        if (navCerrarSesion != null) {
            navCerrarSesion.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                manejarCerrarSesion();
            });
        }
    }

    private void cargarDatosUsuario() {
        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        String usuario = preferences.getString("usuario", "admin");

        // Formatear saludo
        if (tvGreeting != null) {
            tvGreeting.setText(getString(R.string.greeting_format, usuario));
        }

        // Configurar inicial en el avatar si existe la vista
        if (tvAvatarInitial != null && !TextUtils.isEmpty(usuario)) {
            tvAvatarInitial.setText(usuario.substring(0, 1).toUpperCase());
            tvAvatarInitial.setVisibility(View.VISIBLE);
            if (btnAvatar != null) {
                btnAvatar.setVisibility(View.GONE);
            }
        }

        // Formatear fecha actual
        if (tvSubGreeting != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "ES"));
            String fechaActual = sdf.format(new Date());
            if (fechaActual.length() > 0) {
                fechaActual = fechaActual.substring(0, 1).toUpperCase() + fechaActual.substring(1);
            }
            tvSubGreeting.setText("Todo bajo control hoy • " + fechaActual);
        }
    }

    private void obtenerDatosDashboard() {
        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", "");

        String authHeader = !TextUtils.isEmpty(token) ? "Bearer " + token : "";

        ApiClient.getApiService().getDashboardData(authHeader).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse data = response.body();
                    if (data.isSuccess()) {
                        actualizarMetricas(data);
                    } else {
                        String msg = data.getMessage() != null ? data.getMessage() : "Error al obtener datos del Dashboard";
                        Toast.makeText(DashboardActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "Respuesta no exitosa (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                Toast.makeText(DashboardActivity.this,
                        "Sin conexión. No se pudieron cargar las métricas.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        cargarConteoComprasDashboard();
        cargarConteoInventarioDisponibleDashboard();
        cargarConteoOfertasDashboard();
    }

    private void cargarConteoOfertasDashboard() {
        ApiClient.getApiService().getOfertas().enqueue(new Callback<OfertaResponse>() {
            @Override
            public void onResponse(@NonNull Call<OfertaResponse> call, @NonNull Response<OfertaResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && tvValOfertas != null) {
                    tvValOfertas.setText(String.format(Locale.getDefault(), "%,d", response.body().getData().size()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<OfertaResponse> call, @NonNull Throwable t) {}
        });
    }

    private void cargarConteoComprasDashboard() {
        ApiClient.getApiService().getCompras().enqueue(new Callback<CompraResponse>() {
            @Override
            public void onResponse(@NonNull Call<CompraResponse> call, @NonNull Response<CompraResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Compra> lista = response.body().getData();
                    if (lista != null && tvValEnCamino != null) {
                        tvValEnCamino.setText(String.format(Locale.getDefault(), "%,d", lista.size()));
                    } else {
                        cargarConteoComprasDashboardDirect();
                    }
                } else {
                    cargarConteoComprasDashboardDirect();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CompraResponse> call, @NonNull Throwable t) {
                cargarConteoComprasDashboardDirect();
            }
        });
    }

    private void cargarConteoComprasDashboardDirect() {
        ApiClient.getApiService().getComprasDirectList().enqueue(new Callback<List<Compra>>() {
            @Override
            public void onResponse(@NonNull Call<List<Compra>> call, @NonNull Response<List<Compra>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Compra> lista = response.body();
                    if (lista != null && tvValEnCamino != null) {
                        tvValEnCamino.setText(String.format(Locale.getDefault(), "%,d", lista.size()));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Compra>> call, @NonNull Throwable t) {}
        });
    }

    private void cargarConteoInventarioDisponibleDashboard() {
        ApiClient.getApiService().getInventarioDirectList().enqueue(new Callback<List<Inventario>>() {
            @Override
            public void onResponse(@NonNull Call<List<Inventario>> call, @NonNull Response<List<Inventario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    procesarStockDisponible(response.body());
                } else {
                    cargarConteoInventarioDisponibleWrapped();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Inventario>> call, @NonNull Throwable t) {
                cargarConteoInventarioDisponibleWrapped();
            }
        });
    }

    private void cargarConteoInventarioDisponibleWrapped() {
        ApiClient.getApiService().getInventario().enqueue(new Callback<InventarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<InventarioResponse> call, @NonNull Response<InventarioResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    procesarStockDisponible(response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<InventarioResponse> call, @NonNull Throwable t) {}
        });
    }

    private void procesarStockDisponible(List<Inventario> lista) {
        if (lista == null) return;
        java.util.Set<Integer> productosConStock = new java.util.HashSet<>();
        for (Inventario inv : lista) {
            if (inv != null && inv.getStockUnidades() > 0) {
                int pid = inv.getProductoId() > 0 ? inv.getProductoId() : inv.getId();
                if (pid > 0) {
                    productosConStock.add(pid);
                }
            }
        }
        int cantidadDisponible = productosConStock.size();
        if (tvValStockBajo != null) {
            tvValStockBajo.setText(String.format(Locale.getDefault(), "%,d", cantidadDisponible));
        }
    }

    private void actualizarMetricas(DashboardResponse data) {
        if (tvValProductos != null) {
            tvValProductos.setText(String.format(Locale.getDefault(), "%,d", data.getProductos()));
        }
        if (tvValUsuarios != null) {
            tvValUsuarios.setText(String.format(Locale.getDefault(), "%,d", data.getUsuarios()));
        }
        if (tvValPedidos != null) {
            tvValPedidos.setText(String.format(Locale.getDefault(), "%,d", data.getPedidos()));
        }
        if (tvValVentas != null) {
            tvValVentas.setText(String.format(Locale.US, "S/ %,.2f", data.getVentas()));
        }
        if (tvTotalVendido != null) {
            tvTotalVendido.setText(String.format(Locale.US, "S/ %,.2f", data.getTotalVendido()));
        }
        if (tvValReportes != null) {
            tvValReportes.setText(String.format(Locale.US, "S/ %,.2f", data.getTotalVendido()));
        }
        if (tvChipEnRuta != null) {
            tvChipEnRuta.setText(data.getEnCamino() + " en ruta");
        }

        // Actualizar alertas
        if (tvAlertStockBajoText != null) {
            if (data.getStockBajo() == 0) {
                tvAlertStockBajoText.setText("Sin productos con stock bajo");
            } else {
                tvAlertStockBajoText.setText(data.getStockBajo() + " productos con stock bajo que requieren atención");
            }
        }

        if (tvAlertEnCaminoText != null) {
            if (data.getEnCamino() == 0) {
                tvAlertEnCaminoText.setText("Sin pedidos actualmente en ruta de despacho");
            } else {
                tvAlertEnCaminoText.setText(data.getEnCamino() + " pedidos actualmente en ruta de despacho");
            }
        }
    }

    private void setupListeners() {
        // Listener de la barra de navegación inferior
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
            bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_dashboard) {
                        return true;
                    } else if (itemId == R.id.nav_productos) {
                        Intent intent = new Intent(DashboardActivity.this, ProductosActivity.class);
                        startActivity(intent);
                        return true;
                    } else if (itemId == R.id.nav_pedidos) {
                        Intent intent = new Intent(DashboardActivity.this, PedidosActivity.class);
                        startActivity(intent);
                        return true;
                    } else if (itemId == R.id.nav_mas) {
                        if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
                        return true;
                    }
                    return false;
                }
            });
        }

        // Click listeners en Accesos Rápidos y Tarjetas
        View.OnClickListener quickAccessListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.cardQuickProductos || id == R.id.cardProductos) {
                    Intent intent = new Intent(DashboardActivity.this, ProductosActivity.class);
                    startActivity(intent);
                } else if (id == R.id.cardStockBajo) {
                    Intent intent = new Intent(DashboardActivity.this, InventarioActivity.class);
                    startActivity(intent);
                } else if (id == R.id.cardQuickPedidos || id == R.id.cardPedidos || id == R.id.cardVentas) {
                    Intent intent = new Intent(DashboardActivity.this, PedidosActivity.class);
                    startActivity(intent);
                } else if (id == R.id.cardEnCamino) {
                    Intent intent = new Intent(DashboardActivity.this, ComprasActivity.class);
                    startActivity(intent);
                } else if (id == R.id.cardQuickOfertas) {
                    Intent intent = new Intent(DashboardActivity.this, OfertasActivity.class);
                    startActivity(intent);
                } else if (id == R.id.cardReportesFinancieros) {
                    Intent intent = new Intent(DashboardActivity.this, ReportesFinancierosActivity.class);
                    startActivity(intent);
                } else if (id == R.id.cardQuickProveedores) {
                    mostrarDialogoListaProveedores();
                } else if (id == R.id.cardUsuarios) {
                    mostrarDialogoListaUsuarios();
                }
            }
        };

        if (cardProductos != null) cardProductos.setOnClickListener(quickAccessListener);
        if (cardUsuarios != null) cardUsuarios.setOnClickListener(quickAccessListener);
        if (cardPedidos != null) cardPedidos.setOnClickListener(quickAccessListener);
        if (cardVentas != null) cardVentas.setOnClickListener(quickAccessListener);
        if (cardStockBajo != null) cardStockBajo.setOnClickListener(quickAccessListener);
        if (cardEnCamino != null) cardEnCamino.setOnClickListener(quickAccessListener);
        if (cardReportesFinancieros != null) cardReportesFinancieros.setOnClickListener(quickAccessListener);

        if (cardQuickProductos != null) cardQuickProductos.setOnClickListener(quickAccessListener);
        if (cardQuickPedidos != null) cardQuickPedidos.setOnClickListener(quickAccessListener);
        if (cardQuickOfertas != null) cardQuickOfertas.setOnClickListener(quickAccessListener);
        if (cardQuickProveedores != null) cardQuickProveedores.setOnClickListener(quickAccessListener);

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                Toast.makeText(DashboardActivity.this, "Sin notificaciones pendientes", Toast.LENGTH_SHORT).show()
            );
        }

        View.OnClickListener avatarClickListener = v -> mostrarDialogoPerfilUsuario();

        if (btnAvatar != null) btnAvatar.setOnClickListener(avatarClickListener);
        if (tvAvatarInitial != null) tvAvatarInitial.setOnClickListener(avatarClickListener);
        View cardAvatarContainer = findViewById(R.id.cardAvatarContainer);
        if (cardAvatarContainer != null) cardAvatarContainer.setOnClickListener(avatarClickListener);
    }

    private void manejarCerrarSesion() {
        Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();

        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // =======================================================
    // MODAL DE PERFIL DE USUARIO LOGUEADO
    // =======================================================

    private void mostrarDialogoPerfilUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_perfil_usuario, null);
        builder.setView(dialogView);

        AlertDialog dialogPerfil = builder.create();
        if (dialogPerfil.getWindow() != null) {
            dialogPerfil.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ProgressBar pbLoading = dialogView.findViewById(R.id.pbLoadingPerfil);
        LinearLayout layoutCampos = dialogView.findViewById(R.id.layoutCamposPerfil);

        TextView tvNombre = dialogView.findViewById(R.id.tvPerfilNombreCompleto);
        TextView tvUsuario = dialogView.findViewById(R.id.tvPerfilUsuarioName);
        TextView tvRol = dialogView.findViewById(R.id.tvPerfilRolBadge);
        TextView tvInicial = dialogView.findViewById(R.id.tvPerfilInicial);

        TextView tvDni = dialogView.findViewById(R.id.tvPerfilDni);
        TextView tvRuc = dialogView.findViewById(R.id.tvPerfilRuc);
        TextView tvTelefono = dialogView.findViewById(R.id.tvPerfilTelefono);
        TextView tvCorreo = dialogView.findViewById(R.id.tvPerfilCorreo);
        TextView tvDireccion = dialogView.findViewById(R.id.tvPerfilDireccion);

        MaterialButton btnEditar = dialogView.findViewById(R.id.btnEditarPerfil);
        MaterialButton btnCambiarClave = dialogView.findViewById(R.id.btnCambiarClavePerfil);
        MaterialButton btnCerrar = dialogView.findViewById(R.id.btnCerrarPerfil);

        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> dialogPerfil.dismiss());
        }

        SharedPreferences preferences = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        int userId = preferences.getInt("id", 1);
        String savedUser = preferences.getString("usuario", "admin");
        String savedRol = preferences.getString("rol", "admin");

        if (tvUsuario != null) tvUsuario.setText("Usuario: " + savedUser);
        if (tvRol != null) tvRol.setText(savedRol.toUpperCase());
        if (tvInicial != null && !savedUser.isEmpty()) {
            tvInicial.setText(savedUser.substring(0, 1).toUpperCase());
        }

        // Cargar perfil completo del usuario
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (layoutCampos != null) layoutCampos.setVisibility(View.GONE);

        final Usuario[] usuarioCargado = new Usuario[1];

        ApiClient.getApiService().getUsuarioById(userId).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                if (layoutCampos != null) layoutCampos.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    Usuario u = response.body();
                    usuarioCargado[0] = u;

                    if (tvNombre != null) tvNombre.setText(u.getNombreCompleto());
                    if (tvUsuario != null) tvUsuario.setText("Usuario: " + u.getUsuario());
                    if (tvRol != null) tvRol.setText(u.getRol().toUpperCase());
                    if (tvInicial != null && !u.getUsuario().isEmpty()) {
                        tvInicial.setText(u.getUsuario().substring(0, 1).toUpperCase());
                    }

                    if (tvDni != null) tvDni.setText(!TextUtils.isEmpty(u.getDni()) ? u.getDni() : "-");
                    if (tvRuc != null) tvRuc.setText(!TextUtils.isEmpty(u.getRuc()) ? u.getRuc() : "-");
                    if (tvTelefono != null) tvTelefono.setText(!TextUtils.isEmpty(u.getTelefono()) ? u.getTelefono() : "-");
                    if (tvCorreo != null) tvCorreo.setText(!TextUtils.isEmpty(u.getCorreo()) ? u.getCorreo() : "-");
                    if (tvDireccion != null) tvDireccion.setText(!TextUtils.isEmpty(u.getDireccion()) ? u.getDireccion() : "Sin dirección registrada");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                if (layoutCampos != null) layoutCampos.setVisibility(View.VISIBLE);
            }
        });

        if (btnEditar != null) {
            btnEditar.setOnClickListener(v -> {
                dialogPerfil.dismiss();
                if (usuarioCargado[0] != null) {
                    mostrarDialogoFormularioUsuario(usuarioCargado[0], null, null, null, null);
                } else {
                    Usuario uDefault = new Usuario();
                    uDefault.setId(userId);
                    uDefault.setUsuario(savedUser);
                    uDefault.setRol(savedRol);
                    mostrarDialogoFormularioUsuario(uDefault, null, null, null, null);
                }
            });
        }

        if (btnCambiarClave != null) {
            btnCambiarClave.setOnClickListener(v -> {
                dialogPerfil.dismiss();
                if (usuarioCargado[0] != null) {
                    mostrarDialogoResetPasswordUsuario(usuarioCargado[0]);
                } else {
                    Usuario uDefault = new Usuario();
                    uDefault.setId(userId);
                    mostrarDialogoResetPasswordUsuario(uDefault);
                }
            });
        }

        dialogPerfil.show();
    }

    // =======================================================
    // HISTORIAL Y GESTION CRUD DE USUARIOS REALES
    // =======================================================

    private void mostrarDialogoListaUsuarios() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_lista_usuarios, null);
        builder.setView(dialogView);

        AlertDialog dialogLista = builder.create();
        if (dialogLista.getWindow() != null) {
            dialogLista.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ProgressBar pbLoading = dialogView.findViewById(R.id.pbLoadingUsuarios);
        LinearLayout layoutVacio = dialogView.findViewById(R.id.layoutUsuariosVacio);
        RecyclerView rvUsuarios = dialogView.findViewById(R.id.rvUsuarios);
        TextInputEditText etBuscar = dialogView.findViewById(R.id.etBuscarUsuario);
        MaterialButton btnNuevo = dialogView.findViewById(R.id.btnNuevoUsuarioDialog);
        MaterialButton btnCerrar = dialogView.findViewById(R.id.btnCerrarListaUsuarios);

        if (rvUsuarios != null) {
            rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        }

        UsuarioAdapter adapter = new UsuarioAdapter(this, new ArrayList<>());
        if (rvUsuarios != null) {
            rvUsuarios.setAdapter(adapter);
        }

        // Acciones CRUD desde el Adapter de Usuarios (Editar, Resetear Clave, Eliminar)
        adapter.setOnUsuarioActionListener(new UsuarioAdapter.OnUsuarioActionListener() {
            @Override
            public void onEditar(Usuario usuario) {
                mostrarDialogoFormularioUsuario(usuario, pbLoading, layoutVacio, rvUsuarios, adapter);
            }

            @Override
            public void onResetPassword(Usuario usuario) {
                mostrarDialogoResetPasswordUsuario(usuario);
            }

            @Override
            public void onEliminar(Usuario usuario) {
                mostrarDialogoConfirmarEliminarUsuario(usuario, pbLoading, layoutVacio, rvUsuarios, adapter);
            }
        });

        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> dialogLista.dismiss());
        }

        if (btnNuevo != null) {
            btnNuevo.setOnClickListener(v -> mostrarDialogoFormularioUsuario(null, pbLoading, layoutVacio, rvUsuarios, adapter));
        }

        if (etBuscar != null) {
            etBuscar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    cargarUsuariosEnDialogo(query, pbLoading, layoutVacio, rvUsuarios, adapter);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Cargar usuarios reales desde MySQL
        cargarUsuariosEnDialogo("", pbLoading, layoutVacio, rvUsuarios, adapter);

        dialogLista.show();
    }

    private void cargarUsuariosEnDialogo(String query, ProgressBar pbLoading, LinearLayout layoutVacio, RecyclerView rvUsuarios, UsuarioAdapter adapter) {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (rvUsuarios != null) rvUsuarios.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);

        Call<UsuarioResponse> call = TextUtils.isEmpty(query)
                ? ApiClient.getApiService().getUsuarios()
                : ApiClient.getApiService().buscarUsuarios(query);

        call.enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResponse> call, @NonNull Response<UsuarioResponse> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                List<Usuario> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    lista = response.body().getData();
                }

                if (lista.isEmpty()) {
                    cargarUsuariosDirectoDialogo(pbLoading, layoutVacio, rvUsuarios, adapter);
                } else {
                    mostrarUsuariosEnDialogo(lista, layoutVacio, rvUsuarios, adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResponse> call, @NonNull Throwable t) {
                cargarUsuariosDirectoDialogo(pbLoading, layoutVacio, rvUsuarios, adapter);
            }
        });
    }

    private void cargarUsuariosDirectoDialogo(ProgressBar pbLoading, LinearLayout layoutVacio, RecyclerView rvUsuarios, UsuarioAdapter adapter) {
        ApiClient.getApiService().getUsuariosDirectList().enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(@NonNull Call<List<Usuario>> call, @NonNull Response<List<Usuario>> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                List<Usuario> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    lista = response.body();
                }
                mostrarUsuariosEnDialogo(lista, layoutVacio, rvUsuarios, adapter);
            }

            @Override
            public void onFailure(@NonNull Call<List<Usuario>> call, @NonNull Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                mostrarUsuariosEnDialogo(new ArrayList<>(), layoutVacio, rvUsuarios, adapter);
            }
        });
    }

    private void mostrarUsuariosEnDialogo(List<Usuario> usuarios, LinearLayout layoutVacio, RecyclerView rvUsuarios, UsuarioAdapter adapter) {
        if (usuarios.isEmpty()) {
            if (rvUsuarios != null) rvUsuarios.setVisibility(View.GONE);
            if (layoutVacio != null) layoutVacio.setVisibility(View.VISIBLE);
        } else {
            if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
            if (rvUsuarios != null) {
                rvUsuarios.setVisibility(View.VISIBLE);
                adapter.setUsuarios(usuarios);
            }
        }
    }

    // Modal para CREAR o EDITAR usuario
    private void mostrarDialogoFormularioUsuario(Usuario usuarioExistente, ProgressBar pbLoading, LinearLayout layoutVacio, RecyclerView rvUsuarios, UsuarioAdapter adapter) {
        boolean esEdicion = usuarioExistente != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nuevo_usuario, null);
        builder.setView(dialogView);

        AlertDialog dialogUser = builder.create();
        if (dialogUser.getWindow() != null) {
            dialogUser.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvSubtitle = dialogView.findViewById(R.id.tvDialogSubtitle);

        TextInputLayout tilNombres = dialogView.findViewById(R.id.tilNombresUsuario);
        TextInputLayout tilApellidos = dialogView.findViewById(R.id.tilApellidosUsuario);
        TextInputLayout tilDni = dialogView.findViewById(R.id.tilDniUsuario);
        TextInputLayout tilTelefono = dialogView.findViewById(R.id.tilTelefonoUsuario);
        TextInputLayout tilCorreo = dialogView.findViewById(R.id.tilCorreoUsuario);
        TextInputLayout tilUsername = dialogView.findViewById(R.id.tilUsuarioName);
        TextInputLayout tilPassword = dialogView.findViewById(R.id.tilPasswordUsuario);
        TextInputLayout tilConfirmPassword = dialogView.findViewById(R.id.tilConfirmPasswordUsuario);

        TextInputEditText etNombres = dialogView.findViewById(R.id.etNombresUsuario);
        TextInputEditText etApellidos = dialogView.findViewById(R.id.etApellidosUsuario);
        TextInputEditText etDni = dialogView.findViewById(R.id.etDniUsuario);
        TextInputEditText etRuc = dialogView.findViewById(R.id.etRucUsuario);
        TextInputEditText etTelefono = dialogView.findViewById(R.id.etTelefonoUsuario);
        TextInputEditText etCorreo = dialogView.findViewById(R.id.etCorreoUsuario);
        TextInputEditText etUsername = dialogView.findViewById(R.id.etUsername);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPasswordUsuario);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPasswordUsuario);
        Spinner spinnerRol = dialogView.findViewById(R.id.spinnerRolUsuario);
        TextInputEditText etDireccion = dialogView.findViewById(R.id.etDireccionUsuario);

        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarNuevoUsuario);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnGuardarNuevoUsuario);

        // Configurar opciones del Spinner Rol
        String[] roles = {"Cliente", "Vendedor", "Admin", "Chofer"};
        ArrayAdapter<String> adapterRol = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapterRol.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerRol != null) {
            spinnerRol.setAdapter(adapterRol);
        }

        if (esEdicion) {
            if (tvTitle != null) tvTitle.setText("Editar Usuario #" + usuarioExistente.getId());
            if (tvSubtitle != null) tvSubtitle.setText("Modifica los datos del usuario en el sistema");
            if (btnGuardar != null) btnGuardar.setText("Actualizar");

            if (etNombres != null) etNombres.setText(usuarioExistente.getNombres());
            if (etApellidos != null) etApellidos.setText(usuarioExistente.getApellidos());
            if (etDni != null) etDni.setText(usuarioExistente.getDni());
            if (etRuc != null) etRuc.setText(usuarioExistente.getRuc());
            if (etTelefono != null) etTelefono.setText(usuarioExistente.getTelefono());
            if (etCorreo != null) etCorreo.setText(usuarioExistente.getCorreo());
            if (etUsername != null) etUsername.setText(usuarioExistente.getUsuario());
            if (etDireccion != null) etDireccion.setText(usuarioExistente.getDireccion());

            // En edición, la contraseña no es obligatoria
            if (tilPassword != null) tilPassword.setVisibility(View.GONE);
            if (tilConfirmPassword != null) tilConfirmPassword.setVisibility(View.GONE);

            // Seleccionar Rol actual
            if (spinnerRol != null && usuarioExistente.getRol() != null) {
                for (int i = 0; i < roles.length; i++) {
                    if (roles[i].equalsIgnoreCase(usuarioExistente.getRol())) {
                        spinnerRol.setSelection(i);
                        break;
                    }
                }
            }
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialogUser.dismiss());
        }

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> {
                if (tilNombres != null) tilNombres.setError(null);
                if (tilApellidos != null) tilApellidos.setError(null);
                if (tilDni != null) tilDni.setError(null);
                if (tilTelefono != null) tilTelefono.setError(null);
                if (tilCorreo != null) tilCorreo.setError(null);
                if (tilUsername != null) tilUsername.setError(null);
                if (tilPassword != null) tilPassword.setError(null);
                if (tilConfirmPassword != null) tilConfirmPassword.setError(null);

                String nombres = etNombres != null && etNombres.getText() != null ? etNombres.getText().toString().trim() : "";
                String apellidos = etApellidos != null && etApellidos.getText() != null ? etApellidos.getText().toString().trim() : "";
                String dni = etDni != null && etDni.getText() != null ? etDni.getText().toString().trim() : "";
                String ruc = etRuc != null && etRuc.getText() != null ? etRuc.getText().toString().trim() : "";
                String telefono = etTelefono != null && etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";
                String correo = etCorreo != null && etCorreo.getText() != null ? etCorreo.getText().toString().trim() : "";
                String username = etUsername != null && etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
                String password = etPassword != null && etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
                String confirmPassword = etConfirmPassword != null && etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";
                String rolSeleccionado = spinnerRol != null && spinnerRol.getSelectedItem() != null ? spinnerRol.getSelectedItem().toString().toLowerCase() : "cliente";
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
                    if (tilDni != null) tilDni.setError("El DNI debe tener 8 dígitos numéricos");
                    esValido = false;
                }

                if (TextUtils.isEmpty(telefono) || telefono.length() < 7) {
                    if (tilTelefono != null) tilTelefono.setError("Ingresa un número de teléfono válido");
                    esValido = false;
                }

                if (TextUtils.isEmpty(correo) || !correo.contains("@")) {
                    if (tilCorreo != null) tilCorreo.setError("Ingresa un correo electrónico válido");
                    esValido = false;
                }

                if (TextUtils.isEmpty(username) || username.length() < 3) {
                    if (tilUsername != null) tilUsername.setError("El usuario debe tener al menos 3 caracteres");
                    esValido = false;
                }

                if (!esEdicion) {
                    if (TextUtils.isEmpty(password) || password.length() < 8) {
                        if (tilPassword != null) tilPassword.setError("La contraseña debe tener al menos 8 caracteres");
                        esValido = false;
                    }
                    if (TextUtils.isEmpty(confirmPassword)) {
                        if (tilConfirmPassword != null) tilConfirmPassword.setError("Confirma la contraseña");
                        esValido = false;
                    } else if (!password.equals(confirmPassword)) {
                        if (tilConfirmPassword != null) tilConfirmPassword.setError("Las contraseñas no coinciden");
                        esValido = false;
                    }
                }

                if (esValido) {
                    Usuario usuarioRequest = esEdicion ? usuarioExistente : new Usuario();
                    usuarioRequest.setNombres(nombres);
                    usuarioRequest.setApellidos(apellidos);
                    usuarioRequest.setDni(dni);
                    usuarioRequest.setRuc(ruc);
                    usuarioRequest.setTelefono(telefono);
                    usuarioRequest.setCorreo(correo);
                    usuarioRequest.setUsuario(username);
                    if (!esEdicion) {
                        usuarioRequest.setPassword(password);
                    }
                    usuarioRequest.setRol(rolSeleccionado);
                    usuarioRequest.setDireccion(direccion);

                    if (esEdicion) {
                        ejecutarEditarUsuario(usuarioExistente.getId(), usuarioRequest, dialogUser, btnGuardar, pbLoading, layoutVacio, rvUsuarios, adapter);
                    } else {
                        ejecutarCrearUsuario(usuarioRequest, dialogUser, btnGuardar, pbLoading, layoutVacio, rvUsuarios, adapter);
                    }
                }
            });
        }

        dialogUser.show();
    }

    private void ejecutarCrearUsuario(Usuario usuario, AlertDialog dialogUser, MaterialButton btnGuardar,
                                     ProgressBar pbLoading, LinearLayout layoutVacio, RecyclerView rvUsuarios, UsuarioAdapter adapter) {
        if (btnGuardar != null) btnGuardar.setEnabled(false);

        ApiClient.getApiService().crearUsuario(usuario).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResponse> call, @NonNull Response<UsuarioResponse> response) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(DashboardActivity.this, "¡Usuario registrado correctamente en MySQL!", Toast.LENGTH_SHORT).show();
                    if (dialogUser != null && dialogUser.isShowing()) dialogUser.dismiss();

                    // Recargar usuarios en el historial si están activos
                    if (rvUsuarios != null && adapter != null) {
                        cargarUsuariosEnDialogo("", pbLoading, layoutVacio, rvUsuarios, adapter);
                    }
                    obtenerDatosDashboard();
                } else {
                    String msg = "Error al registrar usuario";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    Toast.makeText(DashboardActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResponse> call, @NonNull Throwable t) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);
                Toast.makeText(DashboardActivity.this, "Error de conexión al registrar usuario", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ejecutarEditarUsuario(int id, Usuario usuario, AlertDialog dialogUser, MaterialButton btnGuardar,
                                      ProgressBar pbLoading, LinearLayout layoutVacio, RecyclerView rvUsuarios, UsuarioAdapter adapter) {
        if (btnGuardar != null) btnGuardar.setEnabled(false);

        ApiClient.getApiService().editarUsuario(id, usuario).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResponse> call, @NonNull Response<UsuarioResponse> response) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(DashboardActivity.this, "¡Usuario actualizado correctamente!", Toast.LENGTH_SHORT).show();
                    if (dialogUser != null && dialogUser.isShowing()) dialogUser.dismiss();

                    if (rvUsuarios != null && adapter != null) {
                        cargarUsuariosEnDialogo("", pbLoading, layoutVacio, rvUsuarios, adapter);
                    }
                    obtenerDatosDashboard();
                } else {
                    Toast.makeText(DashboardActivity.this, "Error al actualizar usuario (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResponse> call, @NonNull Throwable t) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);
                Toast.makeText(DashboardActivity.this, "Error de conexión al actualizar usuario", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarAcercaDe() {
        SharedPreferences prefs = getSharedPreferences("AdisanPrefs", MODE_PRIVATE);
        String correo = prefs.getString("correo", "");
        String rol = prefs.getString("rol", "");
        String version;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            version = "1.0";
        }
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Distribuciones ADISAN")
                .setMessage("Versión " + version
                        + "\n\nSesión: " + (correo.isEmpty() ? "—" : correo)
                        + "\nRol: " + (rol.isEmpty() ? "—" : rol.substring(0, 1).toUpperCase() + rol.substring(1))
                        + "\n\nDatos sincronizados con Firebase.")
                .setPositiveButton("Cerrar", null)
                .show();
    }

    // Restablecer contraseña: Firebase envía al usuario un enlace para que elija una nueva
    private void mostrarDialogoResetPasswordUsuario(Usuario usuario) {
        String correo = usuario.getCorreo() != null ? usuario.getCorreo().trim() : "";
        if (correo.isEmpty()) {
            Toast.makeText(this, "Este usuario no tiene correo registrado", Toast.LENGTH_LONG).show();
            return;
        }
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Restablecer contraseña")
                .setMessage("Se enviará a " + correo + " un enlace para que " + usuario.getNombreCompleto()
                        + " cree una nueva contraseña.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar enlace", (dialog, which) -> ejecutarResetPassword(usuario.getId()))
                .show();
    }

    private void ejecutarResetPassword(int id) {
        ApiClient.getApiService().resetPasswordUsuario(id, new ResetPasswordRequest(null)).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResponse> call, @NonNull Response<UsuarioResponse> response) {
                String mensaje = response.isSuccessful() && response.body() != null && response.body().getMessage() != null
                        ? response.body().getMessage()
                        : RespuestaApi.mensajeError(response, "No se pudo enviar el correo (" + response.code() + ")");
                Toast.makeText(DashboardActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResponse> call, @NonNull Throwable t) {
                Toast.makeText(DashboardActivity.this, "Error de conexión al enviar el correo", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Modal para confirmar eliminación de usuario
    private void mostrarDialogoConfirmarEliminarUsuario(Usuario usuario, ProgressBar pbLoading, LinearLayout layoutVacio, RecyclerView rvUsuarios, UsuarioAdapter adapter) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Usuario")
                .setMessage("¿Estás seguro de eliminar al usuario \"" + usuario.getNombreCompleto() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> ejecutarEliminarUsuario(usuario.getId(), pbLoading, layoutVacio, rvUsuarios, adapter))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarEliminarUsuario(int id, ProgressBar pbLoading, LinearLayout layoutVacio, RecyclerView rvUsuarios, UsuarioAdapter adapter) {
        ApiClient.getApiService().eliminarUsuario(id).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResponse> call, @NonNull Response<UsuarioResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DashboardActivity.this, "¡Usuario eliminado correctamente!", Toast.LENGTH_SHORT).show();
                    if (rvUsuarios != null && adapter != null) {
                        cargarUsuariosEnDialogo("", pbLoading, layoutVacio, rvUsuarios, adapter);
                    }
                    obtenerDatosDashboard();
                } else {
                    Toast.makeText(DashboardActivity.this, RespuestaApi.mensajeError(response, "Error al eliminar usuario (" + response.code() + ")"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResponse> call, @NonNull Throwable t) {
                Toast.makeText(DashboardActivity.this, "Error de conexión al eliminar usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =======================================================
    // HISTORIAL Y REGISTRO DE PROVEEDORES DESDE DASHBOARD
    // =======================================================

    private void mostrarDialogoListaProveedores() {
        Intent intent = new Intent(DashboardActivity.this, ProveedoresActivity.class);
        startActivity(intent);
    }

    private void cargarProveedoresEnDialogo(ProgressBar pbLoading, LinearLayout layoutVacio, RecyclerView rvProveedores, ProveedorAdapter adapter) {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (rvProveedores != null) rvProveedores.setVisibility(View.GONE);
        if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);

        ApiClient.getApiService().getProveedores().enqueue(new Callback<ProveedorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProveedorResponse> call, @NonNull Response<ProveedorResponse> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                List<Proveedor> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    lista = response.body().getData();
                }

                if (lista.isEmpty()) {
                    cargarProveedoresDirectoDialogo(pbLoading, layoutVacio, rvProveedores, adapter);
                } else {
                    mostrarProveedoresEnDialogo(lista, layoutVacio, rvProveedores, adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProveedorResponse> call, @NonNull Throwable t) {
                cargarProveedoresDirectoDialogo(pbLoading, layoutVacio, rvProveedores, adapter);
            }
        });
    }

    private void cargarProveedoresDirectoDialogo(ProgressBar pbLoading, LinearLayout layoutVacio, RecyclerView rvProveedores, ProveedorAdapter adapter) {
        ApiClient.getApiService().getProveedoresDirectList().enqueue(new Callback<List<Proveedor>>() {
            @Override
            public void onResponse(@NonNull Call<List<Proveedor>> call, @NonNull Response<List<Proveedor>> response) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                List<Proveedor> lista = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    lista = response.body();
                }
                mostrarProveedoresEnDialogo(lista, layoutVacio, rvProveedores, adapter);
            }

            @Override
            public void onFailure(@NonNull Call<List<Proveedor>> call, @NonNull Throwable t) {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                mostrarProveedoresEnDialogo(new ArrayList<>(), layoutVacio, rvProveedores, adapter);
            }
        });
    }

    private void mostrarProveedoresEnDialogo(List<Proveedor> proveedores, LinearLayout layoutVacio, RecyclerView rvProveedores, ProveedorAdapter adapter) {
        if (proveedores.isEmpty()) {
            if (rvProveedores != null) rvProveedores.setVisibility(View.GONE);
            if (layoutVacio != null) layoutVacio.setVisibility(View.VISIBLE);
        } else {
            if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
            if (rvProveedores != null) {
                rvProveedores.setVisibility(View.VISIBLE);
                adapter.setProveedores(proveedores);
            }
        }
    }

    // Modal para registrar nuevo proveedor
    private void mostrarDialogoNuevoProveedor(ProgressBar pbLoading, LinearLayout layoutVacio, RecyclerView rvProveedores, ProveedorAdapter adapter) {
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

                String nombreEmpresa = etNombre != null && etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
                String ruc = etRuc != null && etRuc.getText() != null ? etRuc.getText().toString().trim() : "";
                String telefono = etTelefono != null && etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";
                String correo = etCorreo != null && etCorreo.getText() != null ? etCorreo.getText().toString().trim() : "";
                String direccion = etDireccion != null && etDireccion.getText() != null ? etDireccion.getText().toString().trim() : "";

                boolean esValido = true;

                if (TextUtils.isEmpty(nombreEmpresa)) {
                    if (tilNombre != null) tilNombre.setError("Ingresa el nombre de la empresa");
                    esValido = false;
                }

                if (TextUtils.isEmpty(ruc) || ruc.length() != 11) {
                    if (tilRuc != null) tilRuc.setError("El RUC debe tener 11 dígitos numéricos");
                    esValido = false;
                }

                if (TextUtils.isEmpty(telefono) || telefono.length() < 7) {
                    if (tilTelefono != null) tilTelefono.setError("Ingresa un número de teléfono válido");
                    esValido = false;
                }

                if (TextUtils.isEmpty(correo) || !correo.contains("@")) {
                    if (tilCorreo != null) tilCorreo.setError("Ingresa un correo electrónico válido");
                    esValido = false;
                }

                if (esValido) {
                    Proveedor nuevoProveedor = new Proveedor();
                    nuevoProveedor.setNombreEmpresa(nombreEmpresa);
                    nuevoProveedor.setRuc(ruc);
                    nuevoProveedor.setTelefono(telefono);
                    nuevoProveedor.setCorreo(correo);
                    nuevoProveedor.setDireccion(direccion);

                    ejecutarCrearProveedor(nuevoProveedor, dialogProv, btnGuardar, pbLoading, layoutVacio, rvProveedores, adapter);
                }
            });
        }

        dialogProv.show();
    }

    private void ejecutarCrearProveedor(Proveedor proveedor, AlertDialog dialogProv, MaterialButton btnGuardar,
                                       ProgressBar pbLoading, LinearLayout layoutVacio, RecyclerView rvProveedores, ProveedorAdapter adapter) {
        if (btnGuardar != null) btnGuardar.setEnabled(false);

        ApiClient.getApiService().crearProveedor(proveedor).enqueue(new Callback<ProveedorResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProveedorResponse> call, @NonNull Response<ProveedorResponse> response) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(DashboardActivity.this, "¡Proveedor registrado correctamente en MySQL!", Toast.LENGTH_SHORT).show();
                    if (dialogProv != null && dialogProv.isShowing()) dialogProv.dismiss();

                    // Recargar proveedores en el historial
                    cargarProveedoresEnDialogo(pbLoading, layoutVacio, rvProveedores, adapter);
                    obtenerDatosDashboard();
                } else {
                    String msg = "Error al registrar proveedor (" + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errStr = response.errorBody().string();
                            ProveedorResponse errRes = new Gson().fromJson(errStr, ProveedorResponse.class);
                            if (errRes != null && errRes.getMessage() != null) {
                                msg = errRes.getMessage();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    Toast.makeText(DashboardActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProveedorResponse> call, @NonNull Throwable t) {
                if (btnGuardar != null) btnGuardar.setEnabled(true);
                Toast.makeText(DashboardActivity.this, "Error de conexión al registrar proveedor", Toast.LENGTH_LONG).show();
            }
        });
    }
}