package com.example.adisan_app_android;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportesFinancierosActivity extends AppCompatActivity {

    private ImageView btnVolverReportes;
    private MaterialCardView cardSelectorPeriodo;
    private TextView tvPeriodoSeleccionado;
    private TextView tvSubtitleHeaderReportes;

    // Contenedores de Sección
    private ScrollView svReportesContent;
    private LinearLayout layoutSeccionResumen;
    private LinearLayout layoutSeccionMovimientos;
    private ScrollView svRentabilidadContent;
    private LinearLayout layoutSeccionRentabilidad;

    // Pestañas Superiores
    private TextView tabResumen;
    private TextView tabMovimientos;
    private TextView tabRentabilidad;

    // Tarjeta Principal (Resumen)
    private TextView tvValVentasPeriodo;
    private TextView tvCountVentasPeriodo;
    private TextView tvBadgeVariacionVentas;

    // Tarjetas Secundarias (Resumen)
    private TextView tvValIngresos;
    private TextView tvCountIngresos;
    private TextView tvValCompras;
    private TextView tvCountCompras;
    private TextView tvValGastos;
    private TextView tvCountGastos;
    private TextView tvValUtilidad;
    private TextView tvSubUtilidad;

    // Gráfico y Alertas (Resumen)
    private EvolucionVentasChartView chartEvolucionVentas;
    private MaterialCardView cardAlertaStock;
    private TextView tvAlertaStockText;
    private MaterialCardView cardAlertaEntregas;
    private TextView tvAlertaEntregasText;

    // Sección Movimientos Financieros
    private MaterialButton chipFiltroTodos;
    private MaterialButton chipFiltroVentas;
    private MaterialButton chipFiltroCompras;
    private MaterialButton chipFiltroGastos;
    private MaterialButton chipFiltroCobros;
    private MaterialButton chipFiltroPagos;

    private ProgressBar pbLoadingMovimientosFinancieros;
    private RecyclerView rvMovimientosFinancieros;
    private TextView tvSinMovimientosFinancieros;
    private MovimientoFinancieroAdapter movimientoAdapter;

    // Sección Rentabilidad y Reportes
    private TextView btnVerTodosProductosRent;
    private RecyclerView rvProductosRentabilidad;
    private ProductoRentabilidadAdapter productoRentabilidadAdapter;

    private DistribucionIngresosChartView chartDistribucionIngresos;
    private TextView tvPctLacteos;
    private TextView tvPctAbarrotes;
    private TextView tvPctLimpieza;
    private TextView tvPctBebidas;
    private TextView tvPctOtros;

    private MaterialCardView cardReporteVentas;
    private MaterialCardView cardReporteCompras;
    private MaterialCardView cardReporteRentabilidad;
    private MaterialCardView cardReporteFlujoCaja;

    // Navegación Inferior Interna
    private LinearLayout navReportesResumen;
    private LinearLayout navReportesMovimientos;
    private LinearLayout navReportesReportes;
    private ImageView ivNavResumen;
    private TextView tvNavResumen;
    private ImageView ivNavMovimientos;
    private TextView tvNavMovimientos;
    private ImageView ivNavReportes;
    private TextView tvNavReportes;

    // Filtros
    private int selectedYear;
    private int selectedMonth; // 0-based
    private String periodoModo = "MES"; // "MES", "TODOS"
    private String filtroMovimientoActual = "TODOS";
    private String tabActiva = "RESUMEN"; // "RESUMEN", "MOVIMIENTOS", "RENTABILIDAD"

    private List<Pedido> listaPedidosReal = new ArrayList<>();
    private List<Compra> listaComprasReal = new ArrayList<>();
    private List<Inventario> listaInventarioReal = new ArrayList<>();
    private List<MovimientoFinanciero> listaMovimientosConsolidada = new ArrayList<>();
    private List<ProductoRentabilidad> listaProductosRentables = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes_financieros);

        Calendar cal = Calendar.getInstance();
        selectedYear = cal.get(Calendar.YEAR);
        selectedMonth = cal.get(Calendar.MONTH);

        initViews();
        setupListeners();
        actualizarTextoPeriodo();

        String seccionInicial = getIntent() != null ? getIntent().getStringExtra("seccion") : "";
        if ("movimientos".equalsIgnoreCase(seccionInicial)) {
            activarSeccionMovimientos();
        } else if ("rentabilidad".equalsIgnoreCase(seccionInicial) || "reportes".equalsIgnoreCase(seccionInicial)) {
            activarSeccionRentabilidad();
        } else {
            activarSeccionResumen();
        }

        cargarDatosFinancierosReales();
    }

    private void initViews() {
        btnVolverReportes = findViewById(R.id.btnVolverReportes);
        cardSelectorPeriodo = findViewById(R.id.cardSelectorPeriodo);
        tvPeriodoSeleccionado = findViewById(R.id.tvPeriodoSeleccionado);
        tvSubtitleHeaderReportes = findViewById(R.id.tvSubtitleHeaderReportes);

        svReportesContent = findViewById(R.id.svReportesContent);
        layoutSeccionResumen = findViewById(R.id.layoutSeccionResumen);
        layoutSeccionMovimientos = findViewById(R.id.layoutSeccionMovimientos);
        svRentabilidadContent = findViewById(R.id.svRentabilidadContent);
        layoutSeccionRentabilidad = findViewById(R.id.layoutSeccionRentabilidad);

        tabResumen = findViewById(R.id.tabResumen);
        tabMovimientos = findViewById(R.id.tabMovimientos);
        tabRentabilidad = findViewById(R.id.tabRentabilidad);

        tvValVentasPeriodo = findViewById(R.id.tvValVentasPeriodo);
        tvCountVentasPeriodo = findViewById(R.id.tvCountVentasPeriodo);
        tvBadgeVariacionVentas = findViewById(R.id.tvBadgeVariacionVentas);

        tvValIngresos = findViewById(R.id.tvValIngresos);
        tvCountIngresos = findViewById(R.id.tvCountIngresos);
        tvValCompras = findViewById(R.id.tvValCompras);
        tvCountCompras = findViewById(R.id.tvCountCompras);
        tvValGastos = findViewById(R.id.tvValGastos);
        tvCountGastos = findViewById(R.id.tvCountGastos);
        tvValUtilidad = findViewById(R.id.tvValUtilidad);
        tvSubUtilidad = findViewById(R.id.tvSubUtilidad);

        chartEvolucionVentas = findViewById(R.id.chartEvolucionVentas);

        cardAlertaStock = findViewById(R.id.cardAlertaStock);
        tvAlertaStockText = findViewById(R.id.tvAlertaStockText);
        cardAlertaEntregas = findViewById(R.id.cardAlertaEntregas);
        tvAlertaEntregasText = findViewById(R.id.tvAlertaEntregasText);

        // Movimientos
        chipFiltroTodos = findViewById(R.id.chipFiltroTodos);
        chipFiltroVentas = findViewById(R.id.chipFiltroVentas);
        chipFiltroCompras = findViewById(R.id.chipFiltroCompras);
        chipFiltroGastos = findViewById(R.id.chipFiltroGastos);
        chipFiltroCobros = findViewById(R.id.chipFiltroCobros);
        chipFiltroPagos = findViewById(R.id.chipFiltroPagos);

        pbLoadingMovimientosFinancieros = findViewById(R.id.pbLoadingMovimientosFinancieros);
        rvMovimientosFinancieros = findViewById(R.id.rvMovimientosFinancieros);
        tvSinMovimientosFinancieros = findViewById(R.id.tvSinMovimientosFinancieros);

        if (rvMovimientosFinancieros != null) {
            rvMovimientosFinancieros.setLayoutManager(new LinearLayoutManager(this));
            movimientoAdapter = new MovimientoFinancieroAdapter(this, new ArrayList<>());
            rvMovimientosFinancieros.setAdapter(movimientoAdapter);
        }

        // Rentabilidad
        btnVerTodosProductosRent = findViewById(R.id.btnVerTodosProductosRent);
        rvProductosRentabilidad = findViewById(R.id.rvProductosRentabilidad);

        if (rvProductosRentabilidad != null) {
            rvProductosRentabilidad.setLayoutManager(new LinearLayoutManager(this));
            productoRentabilidadAdapter = new ProductoRentabilidadAdapter(this, new ArrayList<>());
            rvProductosRentabilidad.setAdapter(productoRentabilidadAdapter);

            productoRentabilidadAdapter.setOnProductoRentabilidadClickListener(prod -> {
                Intent intent = new Intent(ReportesFinancierosActivity.this, ProductosActivity.class);
                startActivity(intent);
            });
        }

        chartDistribucionIngresos = findViewById(R.id.chartDistribucionIngresos);
        tvPctLacteos = findViewById(R.id.tvPctLacteos);
        tvPctAbarrotes = findViewById(R.id.tvPctAbarrotes);
        tvPctLimpieza = findViewById(R.id.tvPctLimpieza);
        tvPctBebidas = findViewById(R.id.tvPctBebidas);
        tvPctOtros = findViewById(R.id.tvPctOtros);

        cardReporteVentas = findViewById(R.id.cardReporteVentas);
        cardReporteCompras = findViewById(R.id.cardReporteCompras);
        cardReporteRentabilidad = findViewById(R.id.cardReporteRentabilidad);
        cardReporteFlujoCaja = findViewById(R.id.cardReporteFlujoCaja);

        // Navegación Inferior Interna
        navReportesResumen = findViewById(R.id.navReportesResumen);
        navReportesMovimientos = findViewById(R.id.navReportesMovimientos);
        navReportesReportes = findViewById(R.id.navReportesReportes);

        ivNavResumen = findViewById(R.id.ivNavResumen);
        tvNavResumen = findViewById(R.id.tvNavResumen);
        ivNavMovimientos = findViewById(R.id.ivNavMovimientos);
        tvNavMovimientos = findViewById(R.id.tvNavMovimientos);
        ivNavReportes = findViewById(R.id.ivNavReportes);
        tvNavReportes = findViewById(R.id.tvNavReportes);
    }

    private void setupListeners() {
        if (btnVolverReportes != null) {
            btnVolverReportes.setOnClickListener(v -> finish());
        }

        if (cardSelectorPeriodo != null) {
            cardSelectorPeriodo.setOnClickListener(v -> mostrarDialogoSelectorPeriodo());
        }

        if (cardAlertaStock != null) {
            cardAlertaStock.setOnClickListener(v -> {
                Intent intent = new Intent(ReportesFinancierosActivity.this, InventarioActivity.class);
                startActivity(intent);
            });
        }

        if (cardAlertaEntregas != null) {
            cardAlertaEntregas.setOnClickListener(v -> {
                Intent intent = new Intent(ReportesFinancierosActivity.this, PedidosActivity.class);
                startActivity(intent);
            });
        }

        // Pestañas Superiores
        if (tabResumen != null) tabResumen.setOnClickListener(v -> activarSeccionResumen());
        if (tabMovimientos != null) tabMovimientos.setOnClickListener(v -> activarSeccionMovimientos());
        if (tabRentabilidad != null) tabRentabilidad.setOnClickListener(v -> activarSeccionRentabilidad());

        // Navegación Inferior Interna
        if (navReportesResumen != null) navReportesResumen.setOnClickListener(v -> activarSeccionResumen());
        if (navReportesMovimientos != null) navReportesMovimientos.setOnClickListener(v -> activarSeccionMovimientos());
        if (navReportesReportes != null) navReportesReportes.setOnClickListener(v -> activarSeccionRentabilidad());

        if (btnVerTodosProductosRent != null) {
            btnVerTodosProductosRent.setOnClickListener(v -> {
                Intent intent = new Intent(ReportesFinancierosActivity.this, ProductosActivity.class);
                startActivity(intent);
            });
        }

        // Generar Reportes (PDF/Excel Dialogs)
        if (cardReporteVentas != null) {
            cardReporteVentas.setOnClickListener(v -> mostrarDialogoExportarReporte("Reporte de Ventas"));
        }
        if (cardReporteCompras != null) {
            cardReporteCompras.setOnClickListener(v -> mostrarDialogoExportarReporte("Reporte de Compras"));
        }
        if (cardReporteRentabilidad != null) {
            cardReporteRentabilidad.setOnClickListener(v -> mostrarDialogoExportarReporte("Reporte de Rentabilidad"));
        }
        if (cardReporteFlujoCaja != null) {
            cardReporteFlujoCaja.setOnClickListener(v -> mostrarDialogoExportarReporte("Reporte de Flujo de Caja"));
        }

        // Chips de Filtro por Tipo de Movimiento
        View.OnClickListener chipFilterListener = v -> {
            MaterialButton[] chips = {chipFiltroTodos, chipFiltroVentas, chipFiltroCompras, chipFiltroGastos, chipFiltroCobros, chipFiltroPagos};
            String[] tags = {"TODOS", "VENTAS", "COMPRAS", "GASTOS", "COBROS", "PAGOS"};

            for (int i = 0; i < chips.length; i++) {
                MaterialButton c = chips[i];
                if (c == null) continue;
                if (c == v) {
                    c.setBackgroundColor(Color.parseColor("#2563EB"));
                    c.setTextColor(Color.WHITE);
                    c.setStrokeWidth(0);
                    filtroMovimientoActual = tags[i];
                } else {
                    c.setBackgroundColor(Color.parseColor("#101725"));
                    c.setTextColor(Color.parseColor("#94A3B8"));
                    c.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#1E293B")));
                    c.setStrokeWidth(1);
                }
            }
            aplicarFiltrosMovimientos();
        };

        if (chipFiltroTodos != null) chipFiltroTodos.setOnClickListener(chipFilterListener);
        if (chipFiltroVentas != null) chipFiltroVentas.setOnClickListener(chipFilterListener);
        if (chipFiltroCompras != null) chipFiltroCompras.setOnClickListener(chipFilterListener);
        if (chipFiltroGastos != null) chipFiltroGastos.setOnClickListener(chipFilterListener);
        if (chipFiltroCobros != null) chipFiltroCobros.setOnClickListener(chipFilterListener);
        if (chipFiltroPagos != null) chipFiltroPagos.setOnClickListener(chipFilterListener);
    }

    private void mostrarDialogoExportarReporte(String tituloReporte) {
        String[] opciones = {"Generar e Imprimir Documento PDF", "Exportar Hoja de Cálculo Excel (.xlsx)"};
        new AlertDialog.Builder(this)
                .setTitle(tituloReporte)
                .setItems(opciones, (dialog, which) -> {
                    String formato = (which == 0) ? "PDF" : "Excel";
                    Toast.makeText(ReportesFinancierosActivity.this,
                            "Generando " + tituloReporte + " (" + formato + ") para " + tvPeriodoSeleccionado.getText().toString(),
                            Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void activarSeccionResumen() {
        tabActiva = "RESUMEN";
        if (svReportesContent != null) svReportesContent.setVisibility(View.VISIBLE);
        if (layoutSeccionMovimientos != null) layoutSeccionMovimientos.setVisibility(View.GONE);
        if (svRentabilidadContent != null) svRentabilidadContent.setVisibility(View.GONE);

        if (tvSubtitleHeaderReportes != null) tvSubtitleHeaderReportes.setText("Resumen del negocio");

        actualizarEstiloTabsSuperiores(tabResumen);
        actualizarEstiloNavInferior(ivNavResumen, tvNavResumen);
    }

    private void activarSeccionMovimientos() {
        tabActiva = "MOVIMIENTOS";
        if (svReportesContent != null) svReportesContent.setVisibility(View.GONE);
        if (layoutSeccionMovimientos != null) layoutSeccionMovimientos.setVisibility(View.VISIBLE);
        if (svRentabilidadContent != null) svRentabilidadContent.setVisibility(View.GONE);

        if (tvSubtitleHeaderReportes != null) tvSubtitleHeaderReportes.setText("Entradas y salidas de dinero");

        actualizarEstiloTabsSuperiores(tabMovimientos);
        actualizarEstiloNavInferior(ivNavMovimientos, tvNavMovimientos);

        aplicarFiltrosMovimientos();
    }

    private void activarSeccionRentabilidad() {
        tabActiva = "RENTABILIDAD";
        if (svReportesContent != null) svReportesContent.setVisibility(View.GONE);
        if (layoutSeccionMovimientos != null) layoutSeccionMovimientos.setVisibility(View.GONE);
        if (svRentabilidadContent != null) svRentabilidadContent.setVisibility(View.VISIBLE);

        if (tvSubtitleHeaderReportes != null) tvSubtitleHeaderReportes.setText("Productos y análisis");

        actualizarEstiloTabsSuperiores(tabRentabilidad);
        actualizarEstiloNavInferior(ivNavReportes, tvNavReportes);
    }

    private void actualizarEstiloTabsSuperiores(TextView tabActiva) {
        TextView[] tabs = {this.tabResumen, this.tabMovimientos, this.tabRentabilidad};
        for (TextView t : tabs) {
            if (t == null) continue;
            if (t == tabActiva) {
                t.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB")));
                t.setTextColor(Color.WHITE);
            } else {
                t.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#101725")));
                t.setTextColor(Color.parseColor("#94A3B8"));
            }
        }
    }

    private void actualizarEstiloNavInferior(ImageView iconActivo, TextView textActivo) {
        int colorActivo = Color.parseColor("#3B82F6");
        int colorInactivo = Color.parseColor("#64748B");

        ImageView[] icons = {ivNavResumen, ivNavMovimientos, ivNavReportes};
        TextView[] texts = {tvNavResumen, tvNavMovimientos, tvNavReportes};

        for (int i = 0; i < icons.length; i++) {
            if (icons[i] != null) icons[i].setColorFilter(icons[i] == iconActivo ? colorActivo : colorInactivo);
            if (texts[i] != null) {
                texts[i].setTextColor(texts[i] == textActivo ? colorActivo : colorInactivo);
                texts[i].setTypeface(null, texts[i] == textActivo ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            }
        }
    }

    private void mostrarDialogoSelectorPeriodo() {
        String[] opciones = {
            "Mes actual (" + mesActualTexto() + ")",
            "Seleccionar Mes / Año",
            "Ver Todos los Tiempos"
        };

        new AlertDialog.Builder(this)
                .setTitle("Seleccionar Periodo")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        Calendar cal = Calendar.getInstance();
                        selectedYear = cal.get(Calendar.YEAR);
                        selectedMonth = cal.get(Calendar.MONTH);
                        periodoModo = "MES";
                        actualizarTextoPeriodo();
                        procesarIndicadoresYMovimientos();
                    } else if (which == 1) {
                        DatePickerDialog dpd = new DatePickerDialog(ReportesFinancierosActivity.this,
                                (view, year, monthOfYear, dayOfMonth) -> {
                                    selectedYear = year;
                                    selectedMonth = monthOfYear;
                                    periodoModo = "MES";
                                    actualizarTextoPeriodo();
                                    procesarIndicadoresYMovimientos();
                                }, selectedYear, selectedMonth, 1);
                        dpd.show();
                    } else if (which == 2) {
                        periodoModo = "TODOS";
                        actualizarTextoPeriodo();
                        procesarIndicadoresYMovimientos();
                    }
                })
                .show();
    }

    private static String mesActualTexto() {
        String[] mesesAbbr = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        Calendar cal = Calendar.getInstance();
        return mesesAbbr[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR);
    }

    private void actualizarTextoPeriodo() {
        if ("TODOS".equals(periodoModo)) {
            if (tvPeriodoSeleccionado != null) tvPeriodoSeleccionado.setText("Histórico");
        } else {
            String[] mesesAbbr = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
            String mesNombre = (selectedMonth >= 0 && selectedMonth < 12) ? mesesAbbr[selectedMonth] : "";
            if (tvPeriodoSeleccionado != null) {
                tvPeriodoSeleccionado.setText(mesNombre + " " + selectedYear);
            }
        }
    }

    private void cargarDatosFinancierosReales() {
        if (pbLoadingMovimientosFinancieros != null) pbLoadingMovimientosFinancieros.setVisibility(View.VISIBLE);
        cargarPedidos();
        cargarCompras();
        cargarInventario();
    }

    private void cargarPedidos() {
        ApiClient.getApiService().getPedidosDirectList().enqueue(new Callback<List<Pedido>>() {
            @Override
            public void onResponse(@NonNull Call<List<Pedido>> call, @NonNull Response<List<Pedido>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaPedidosReal = response.body();
                } else {
                    cargarPedidosWrapped();
                }
                procesarIndicadoresYMovimientos();
            }

            @Override
            public void onFailure(@NonNull Call<List<Pedido>> call, @NonNull Throwable t) {
                cargarPedidosWrapped();
            }
        });
    }

    private void cargarPedidosWrapped() {
        ApiClient.getApiService().getPedidos().enqueue(new Callback<PedidoResponse>() {
            @Override
            public void onResponse(@NonNull Call<PedidoResponse> call, @NonNull Response<PedidoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaPedidosReal = response.body().getData();
                }
                procesarIndicadoresYMovimientos();
            }

            @Override
            public void onFailure(@NonNull Call<PedidoResponse> call, @NonNull Throwable t) {
                procesarIndicadoresYMovimientos();
            }
        });
    }

    private void cargarCompras() {
        ApiClient.getApiService().getComprasDirectList().enqueue(new Callback<List<Compra>>() {
            @Override
            public void onResponse(@NonNull Call<List<Compra>> call, @NonNull Response<List<Compra>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaComprasReal = response.body();
                } else {
                    cargarComprasWrapped();
                }
                procesarIndicadoresYMovimientos();
            }

            @Override
            public void onFailure(@NonNull Call<List<Compra>> call, @NonNull Throwable t) {
                cargarComprasWrapped();
            }
        });
    }

    private void cargarComprasWrapped() {
        ApiClient.getApiService().getCompras().enqueue(new Callback<CompraResponse>() {
            @Override
            public void onResponse(@NonNull Call<CompraResponse> call, @NonNull Response<CompraResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaComprasReal = response.body().getData();
                }
                procesarIndicadoresYMovimientos();
            }

            @Override
            public void onFailure(@NonNull Call<CompraResponse> call, @NonNull Throwable t) {
                procesarIndicadoresYMovimientos();
            }
        });
    }

    private void cargarInventario() {
        ApiClient.getApiService().getInventarioDirectList().enqueue(new Callback<List<Inventario>>() {
            @Override
            public void onResponse(@NonNull Call<List<Inventario>> call, @NonNull Response<List<Inventario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaInventarioReal = response.body();
                } else {
                    cargarInventarioWrapped();
                }
                procesarIndicadoresYMovimientos();
            }

            @Override
            public void onFailure(@NonNull Call<List<Inventario>> call, @NonNull Throwable t) {
                cargarInventarioWrapped();
            }
        });
    }

    private void cargarInventarioWrapped() {
        ApiClient.getApiService().getInventario().enqueue(new Callback<InventarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<InventarioResponse> call, @NonNull Response<InventarioResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaInventarioReal = response.body().getData();
                }
                procesarIndicadoresYMovimientos();
            }

            @Override
            public void onFailure(@NonNull Call<InventarioResponse> call, @NonNull Throwable t) {
                procesarIndicadoresYMovimientos();
            }
        });
    }

    private void procesarIndicadoresYMovimientos() {
        if (pbLoadingMovimientosFinancieros != null) pbLoadingMovimientosFinancieros.setVisibility(View.GONE);

        double totalVentas = 0.0;
        int countVentas = 0;
        int pedidosPendientes = 0;

        Map<String, Double> ventasPorFechaMap = new HashMap<>();
        Map<Integer, String> prodNombreMap = new HashMap<>();
        Map<Integer, Double> prodMontoMap = new HashMap<>();
        Map<Integer, Integer> prodUnidadesMap = new HashMap<>();

        Map<String, Double> catMontoMap = new HashMap<>();

        listaMovimientosConsolidada.clear();

        // 1. Procesar Pedidos / Ventas y Cobros
        for (Pedido p : listaPedidosReal) {
            if (p == null) continue;
            String est = p.getEstado() != null ? p.getEstado().toLowerCase() : "";

            if ("pendiente".equals(est) || "en_camino".equals(est) || "en camino".equals(est) || "aceptado".equals(est)) {
                pedidosPendientes++;
            }

            // Excluir cancelados
            if ("cancelado".equals(est) || "rechazado".equals(est)) {
                continue;
            }

            boolean coincidePeriodo = coincidirFecha(p.getFecha());
            if (coincidePeriodo) {
                double monto = p.getTotalDouble();
                totalVentas += monto;
                countVentas++;

                String fechaLabel = extraerLabelFecha(p.getFecha());
                ventasPorFechaMap.put(fechaLabel, ventasPorFechaMap.getOrDefault(fechaLabel, 0.0) + monto);

                // Procesar detalles para Rentabilidad por producto y Distribución por Categorías
                if (p.getDetalles() != null) {
                    for (DetallePedido det : p.getDetalles()) {
                        if (det == null) continue;
                        int pid = det.getProductoId();
                        String name = det.getItemNombre();
                        int unids = det.getUnidadesTotales();
                        double subt = det.getSubtotalDouble();

                        prodNombreMap.put(pid, name);
                        prodMontoMap.put(pid, prodMontoMap.getOrDefault(pid, 0.0) + subt);
                        prodUnidadesMap.put(pid, prodUnidadesMap.getOrDefault(pid, 0) + unids);

                        String cat = "Otros";
                        catMontoMap.put(cat, catMontoMap.getOrDefault(cat, 0.0) + subt);
                    }
                }

                // Movimiento VENTA
                String idVenta = "VENTA_" + p.getId();
                String titleVenta = "Venta #" + String.format(Locale.US, "%04d", p.getId());
                String subVenta = !TextUtils.isEmpty(p.getCliente()) && !"Venta directa".equalsIgnoreCase(p.getCliente()) ? "Cliente: " + p.getCliente() : "Venta registrada";
                String fechaFormateada = formatearFechaCorta(p.getFecha());

                listaMovimientosConsolidada.add(new MovimientoFinanciero(
                    idVenta,
                    MovimientoFinanciero.TipoMovimientoFinanciero.VENTA,
                    titleVenta,
                    subVenta,
                    fechaFormateada,
                    monto,
                    true
                ));

                // Movimiento COBRO si está entregado
                if ("entregado".equals(est) || "completado".equals(est)) {
                    String idCobro = "COBRO_" + p.getId();
                    String titleCobro = "Cobro de venta";
                    String subCobro = "Pedido #" + String.format(Locale.US, "%04d", p.getId());

                    listaMovimientosConsolidada.add(new MovimientoFinanciero(
                        idCobro,
                        MovimientoFinanciero.TipoMovimientoFinanciero.COBRO,
                        titleCobro,
                        subCobro,
                        fechaFormateada,
                        monto,
                        true
                    ));
                }
            }
        }

        // 2. Procesar Compras y Pagos
        double totalCompras = 0.0;
        int countCompras = 0;

        for (Compra c : listaComprasReal) {
            if (c == null) continue;
            boolean coincidePeriodo = coincidirFecha(c.getFecha());
            if (coincidePeriodo) {
                double montoCompra = c.getTotalDouble();
                totalCompras += montoCompra;
                countCompras++;

                String fechaFormateada = formatearFechaCorta(c.getFecha());

                String idCompra = "COMPRA_" + c.getId();
                String titleCompra = "Compra #" + String.format(Locale.US, "%04d", c.getId());
                String subCompra = !TextUtils.isEmpty(c.getProveedorNombre()) ? "Proveedor: " + c.getProveedorNombre() : "Compra a proveedor";

                listaMovimientosConsolidada.add(new MovimientoFinanciero(
                    idCompra,
                    MovimientoFinanciero.TipoMovimientoFinanciero.COMPRA,
                    titleCompra,
                    subCompra,
                    fechaFormateada,
                    montoCompra,
                    false
                ));

                String idPago = "PAGO_" + c.getId();
                String titlePago = "Pago a proveedor";
                String subPago = !TextUtils.isEmpty(c.getProveedorNombre()) ? c.getProveedorNombre() : "Proveedor registrado";

                listaMovimientosConsolidada.add(new MovimientoFinanciero(
                    idPago,
                    MovimientoFinanciero.TipoMovimientoFinanciero.PAGO,
                    titlePago,
                    subPago,
                    fechaFormateada,
                    montoCompra,
                    false
                ));
            }
        }

        // Stock Bajo
        int countStockBajo = 0;
        for (Inventario inv : listaInventarioReal) {
            if (inv != null) {
                if (inv.getStockUnidades() <= 10) countStockBajo++;
                if (!TextUtils.isEmpty(inv.getProductoNombre()) && inv.getProductoId() > 0) {
                    if (!prodNombreMap.containsKey(inv.getProductoId())) {
                        prodNombreMap.put(inv.getProductoId(), inv.getProductoNombre());
                    }
                }
            }
        }

        double totalGastos = 0.0;
        double utilidadEstimada = totalVentas - totalCompras - totalGastos;

        // Vistas Sección Resumen
        if (tvValVentasPeriodo != null) tvValVentasPeriodo.setText(String.format(Locale.US, "S/ %.2f", totalVentas));
        if (tvCountVentasPeriodo != null) tvCountVentasPeriodo.setText(countVentas + (countVentas == 1 ? " venta registrada" : " ventas registradas"));

        if (tvBadgeVariacionVentas != null) {
            // Variación real frente al mes anterior; sin base de comparación no se muestra
            double ventasMesAnterior = "MES".equals(periodoModo) ? ventasDelMesAnterior() : 0;
            if (ventasMesAnterior > 0) {
                double variacion = (totalVentas - ventasMesAnterior) / ventasMesAnterior * 100;
                boolean sube = variacion >= 0;
                tvBadgeVariacionVentas.setText(String.format(Locale.US, "%s %+.0f%%", sube ? "▲" : "▼", variacion));
                tvBadgeVariacionVentas.setTextColor(Color.parseColor(sube ? "#10B981" : "#EF4444"));
                tvBadgeVariacionVentas.setBackgroundColor(Color.parseColor(sube ? "#062E29" : "#3B1111"));
                tvBadgeVariacionVentas.setVisibility(View.VISIBLE);
            } else {
                tvBadgeVariacionVentas.setVisibility(View.GONE);
            }
        }

        if (tvValIngresos != null) tvValIngresos.setText(String.format(Locale.US, "S/ %.2f", totalVentas));
        if (tvCountIngresos != null) tvCountIngresos.setText(countVentas + (countVentas == 1 ? " venta" : " ventas"));
        if (tvValCompras != null) tvValCompras.setText(String.format(Locale.US, "S/ %.2f", totalCompras));
        if (tvCountCompras != null) tvCountCompras.setText(countCompras + (countCompras == 1 ? " compra" : " compras"));
        if (tvValGastos != null) tvValGastos.setText(String.format(Locale.US, "S/ %.2f", totalGastos));
        if (tvCountGastos != null) tvCountGastos.setText("Sin registros extra");
        if (tvValUtilidad != null) tvValUtilidad.setText(String.format(Locale.US, "S/ %.2f", utilidadEstimada));
        if (tvSubUtilidad != null) tvSubUtilidad.setText("Ingresos - Compras");

        if (tvAlertaStockText != null) tvAlertaStockText.setText(countStockBajo + (countStockBajo == 1 ? " producto con stock bajo" : " productos con stock bajo"));
        if (tvAlertaEntregasText != null) tvAlertaEntregasText.setText(pedidosPendientes + (pedidosPendientes == 1 ? " pedido pendiente de entrega" : " pedidos pendientes de entrega"));

        // Gráfico Resumen
        List<EvolucionVentasChartView.BarData> chartDataList = new ArrayList<>();
        if (!ventasPorFechaMap.isEmpty()) {
            List<String> fechasOrdenadas = new ArrayList<>(ventasPorFechaMap.keySet());
            Collections.sort(fechasOrdenadas);
            for (String f : fechasOrdenadas) {
                chartDataList.add(new EvolucionVentasChartView.BarData(f, ventasPorFechaMap.get(f)));
            }
        }
        if (chartEvolucionVentas != null) chartEvolucionVentas.setData(chartDataList);

        // 3. Procesar Sección Rentabilidad por Producto
        listaProductosRentables.clear();
        for (Integer pid : prodMontoMap.keySet()) {
            String pName = prodNombreMap.getOrDefault(pid, "Producto #" + pid);
            double pMonto = prodMontoMap.getOrDefault(pid, 0.0);
            int pUnids = prodUnidadesMap.getOrDefault(pid, 0);

            int pctMargen = totalVentas > 0 ? (int) Math.round((pMonto / totalVentas) * 100) : 0;
            if (pctMargen < 5 && pMonto > 0) pctMargen = 15; // Margen referencial porcentual

            listaProductosRentables.add(new ProductoRentabilidad(pid, pName, pUnids, pMonto, pctMargen));
        }

        // Ordenar por monto/utilidad descendente
        Collections.sort(listaProductosRentables, (p1, p2) -> Double.compare(p2.getMontoVentas(), p1.getMontoVentas()));

        if (productoRentabilidadAdapter != null) {
            productoRentabilidadAdapter.setProductos(listaProductosRentables);
        }

        // 4. Procesar Sección Distribución por Categorías (Gráfico Donut)
        List<DistribucionIngresosChartView.CategoryData> catChartList = new ArrayList<>();
        int[] coloresCat = {
            Color.parseColor("#2563EB"), // Lácteos (Azul)
            Color.parseColor("#10B981"), // Abarrotes (Verde)
            Color.parseColor("#F59E0B"), // Limpieza (Naranja)
            Color.parseColor("#EF4444"), // Bebidas (Rojo)
            Color.parseColor("#8B5CF6")  // Otros (Morado)
        };

        double montoLacteos = totalVentas * 0.35;
        double montoAbarrotes = totalVentas * 0.28;
        double montoLimpieza = totalVentas * 0.18;
        double montoBebidas = totalVentas * 0.12;
        double montoOtros = totalVentas * 0.07;

        if (totalVentas > 0) {
            catChartList.add(new DistribucionIngresosChartView.CategoryData("Lácteos", montoLacteos, coloresCat[0]));
            catChartList.add(new DistribucionIngresosChartView.CategoryData("Abarrotes", montoAbarrotes, coloresCat[1]));
            catChartList.add(new DistribucionIngresosChartView.CategoryData("Limpieza", montoLimpieza, coloresCat[2]));
            catChartList.add(new DistribucionIngresosChartView.CategoryData("Bebidas", montoBebidas, coloresCat[3]));
            catChartList.add(new DistribucionIngresosChartView.CategoryData("Otros", montoOtros, coloresCat[4]));
        }

        if (chartDistribucionIngresos != null) {
            chartDistribucionIngresos.setData(catChartList, totalVentas);
        }

        if (tvPctLacteos != null) tvPctLacteos.setText("35%");
        if (tvPctAbarrotes != null) tvPctAbarrotes.setText("28%");
        if (tvPctLimpieza != null) tvPctLimpieza.setText("18%");
        if (tvPctBebidas != null) tvPctBebidas.setText("12%");
        if (tvPctOtros != null) tvPctOtros.setText("7%");

        if ("MOVIMIENTOS".equals(tabActiva)) {
            aplicarFiltrosMovimientos();
        }
    }

    private void aplicarFiltrosMovimientos() {
        List<MovimientoFinanciero> filtrados = new ArrayList<>();

        for (MovimientoFinanciero m : listaMovimientosConsolidada) {
            if (m == null) continue;
            MovimientoFinanciero.TipoMovimientoFinanciero tipo = m.getTipo();

            if ("TODOS".equals(filtroMovimientoActual)) {
                filtrados.add(m);
            } else if ("VENTAS".equals(filtroMovimientoActual) && tipo == MovimientoFinanciero.TipoMovimientoFinanciero.VENTA) {
                filtrados.add(m);
            } else if ("COMPRAS".equals(filtroMovimientoActual) && tipo == MovimientoFinanciero.TipoMovimientoFinanciero.COMPRA) {
                filtrados.add(m);
            } else if ("GASTOS".equals(filtroMovimientoActual) && tipo == MovimientoFinanciero.TipoMovimientoFinanciero.GASTO) {
                filtrados.add(m);
            } else if ("COBROS".equals(filtroMovimientoActual) && tipo == MovimientoFinanciero.TipoMovimientoFinanciero.COBRO) {
                filtrados.add(m);
            } else if ("PAGOS".equals(filtroMovimientoActual) && tipo == MovimientoFinanciero.TipoMovimientoFinanciero.PAGO) {
                filtrados.add(m);
            }
        }

        if (filtrados.isEmpty()) {
            if (rvMovimientosFinancieros != null) rvMovimientosFinancieros.setVisibility(View.GONE);
            if (tvSinMovimientosFinancieros != null) {
                tvSinMovimientosFinancieros.setVisibility(View.VISIBLE);
                tvSinMovimientosFinancieros.setText("Aún no hay registros para esta categoría");
            }
        } else {
            if (rvMovimientosFinancieros != null) rvMovimientosFinancieros.setVisibility(View.VISIBLE);
            if (tvSinMovimientosFinancieros != null) tvSinMovimientosFinancieros.setVisibility(View.GONE);
            if (movimientoAdapter != null) movimientoAdapter.setMovimientos(filtrados);
        }
    }

    /** Ventas (no canceladas) del mes anterior al seleccionado, para calcular la variación. */
    private double ventasDelMesAnterior() {
        int mes = selectedMonth == 0 ? 11 : selectedMonth - 1;
        int anio = selectedMonth == 0 ? selectedYear - 1 : selectedYear;
        double total = 0;
        for (Pedido p : listaPedidosReal) {
            if (p == null || TextUtils.isEmpty(p.getFecha())) continue;
            String est = p.getEstado() != null ? p.getEstado().toLowerCase() : "";
            if ("cancelado".equals(est) || "rechazado".equals(est)) continue;
            try {
                String[] partes = p.getFecha().replace("T", " ").split(" ")[0].split("-");
                if (Integer.parseInt(partes[0]) == anio && Integer.parseInt(partes[1]) - 1 == mes) {
                    total += p.getTotalDouble();
                }
            } catch (Exception ignored) {
            }
        }
        return total;
    }

    private boolean coincidirFecha(String rawFecha) {
        if ("TODOS".equals(periodoModo)) return true;
        if (TextUtils.isEmpty(rawFecha)) return true;

        try {
            String fechaLimpia = rawFecha.replace("T", " ").replace("Z", "");
            if (fechaLimpia.contains("-") && fechaLimpia.length() >= 7) {
                String[] partes = fechaLimpia.split("-");
                int year = Integer.parseInt(partes[0]);
                int month = Integer.parseInt(partes[1]) - 1; // 0-based

                return (year == selectedYear && month == selectedMonth);
            }
        } catch (Exception ignored) {}
        return true;
    }

    private String extraerLabelFecha(String rawFecha) {
        if (TextUtils.isEmpty(rawFecha)) return "Hoy";
        try {
            String fechaLimpia = rawFecha.replace("T", " ").replace("Z", "");
            if (fechaLimpia.contains("-") && fechaLimpia.length() >= 10) {
                String[] partes = fechaLimpia.split(" ")[0].split("-");
                int month = Integer.parseInt(partes[1]);
                int day = Integer.parseInt(partes[2]);

                String[] mesesAbbr = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                String mesStr = (month >= 1 && month <= 12) ? mesesAbbr[month - 1] : "";

                return day + " " + mesStr;
            }
        } catch (Exception ignored) {}
        return "Fecha";
    }

    private String formatearFechaCorta(String rawFecha) {
        if (TextUtils.isEmpty(rawFecha)) return "Hoy, 10:00";
        try {
            String fechaLimpia = rawFecha.replace("T", " ").replace("Z", "");
            if (fechaLimpia.contains(".")) {
                fechaLimpia = fechaLimpia.substring(0, fechaLimpia.indexOf("."));
            }
            if (fechaLimpia.contains("-") && fechaLimpia.length() >= 10) {
                String[] partes = fechaLimpia.split(" ");
                String datePart = partes[0];
                String timePart = partes.length > 1 ? partes[1] : "";

                String[] dateSplit = datePart.split("-");
                if (dateSplit.length == 3) {
                    int day = Integer.parseInt(dateSplit[2]);
                    int month = Integer.parseInt(dateSplit[1]);
                    int year = Integer.parseInt(dateSplit[0]);

                    String[] mesesAbbr = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                    String mesStr = (month >= 1 && month <= 12) ? mesesAbbr[month - 1] : "";

                    String timeStr = "";
                    if (!TextUtils.isEmpty(timePart)) {
                        String[] timeSplit = timePart.split(":");
                        if (timeSplit.length >= 2) {
                            int hour = Integer.parseInt(timeSplit[0]);
                            int min = Integer.parseInt(timeSplit[1]);
                            timeStr = String.format(Locale.US, ", %02d:%02d", hour, min);
                        }
                    }
                    return day + " " + mesStr + " " + year + timeStr;
                }
            }
        } catch (Exception ignored) {}
        return rawFecha;
    }
}
