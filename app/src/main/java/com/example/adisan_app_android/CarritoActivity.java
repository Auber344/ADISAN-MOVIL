package com.example.adisan_app_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarritoActivity extends AppCompatActivity {

    private LinearLayout layoutStepCarrito;
    private ScrollView layoutStepDatosPedido;

    private ImageView btnVolverCarrito;
    private MaterialButton btnVaciar;
    private RecyclerView rvCarrito;
    private LinearLayout layoutVacio;
    private MaterialButton btnIrACatalogo;
    private MaterialButton btnContinuar;
    private TextView tvTotalCalculado;
    private TextView tvResumenProductos;
    private TextView tvResumenUnidades;

    // Step 2 views
    private ImageView btnVolverDatosPedido;
    private MaterialCardView cardVentaDirecta, cardClienteFormal;
    private ImageView ivCheckVentaDirecta, ivCheckClienteFormal;
    private LinearLayout layoutSpinnerClienteContainer;
    private Spinner spinnerClienteDatosPedido;

    private MaterialCardView cardPagoEfectivo, cardPagoYape, cardPagoTarjeta;
    private TextView tvResumenProdVal, tvResumenPaqVal, tvResumenUnidVal, tvResumenTotalUnidVal;
    private TextView tvResumenMetodoPagoVal, tvResumenTipoVentaVal, tvResumenTotalVentaVal;
    private MaterialButton btnConfirmarPedidoFinal;

    private CarritoAdapter adapter;
    private boolean esVentaDirecta = true;
    private String metodoPagoSeleccionado = "Yape";
    private Usuario clienteSeleccionado = null;
    private List<Usuario> listaClientes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        initViews();
        setupRecyclerView();
        setupListeners();
        actualizarVista();
    }

    private void initViews() {
        layoutStepCarrito = findViewById(R.id.layoutStepCarrito);
        layoutStepDatosPedido = findViewById(R.id.layoutStepDatosPedido);

        btnVolverCarrito = findViewById(R.id.btnVolverCarrito);
        btnVaciar = findViewById(R.id.btnVaciarCarrito);
        rvCarrito = findViewById(R.id.rvCarrito);
        layoutVacio = findViewById(R.id.layoutCarritoVacio);
        btnIrACatalogo = findViewById(R.id.btnIrACatalogo);
        btnContinuar = findViewById(R.id.btnIrADatosPedido);
        tvTotalCalculado = findViewById(R.id.tvTotalCarritoCalculado);
        tvResumenProductos = findViewById(R.id.tvResumenProductosCount);
        tvResumenUnidades = findViewById(R.id.tvResumenUnidadesCount);

        // Step 2
        btnVolverDatosPedido = findViewById(R.id.btnVolverDatosPedido);
        cardVentaDirecta = findViewById(R.id.cardVentaDirecta);
        cardClienteFormal = findViewById(R.id.cardClienteFormal);
        ivCheckVentaDirecta = findViewById(R.id.ivCheckVentaDirecta);
        ivCheckClienteFormal = findViewById(R.id.ivCheckClienteFormal);
        layoutSpinnerClienteContainer = findViewById(R.id.layoutSpinnerClienteContainer);
        spinnerClienteDatosPedido = findViewById(R.id.spinnerClienteDatosPedido);

        cardPagoEfectivo = findViewById(R.id.cardPagoEfectivo);
        cardPagoYape = findViewById(R.id.cardPagoYape);
        cardPagoTarjeta = findViewById(R.id.cardPagoTarjeta);

        tvResumenProdVal = findViewById(R.id.tvResumenProductosVal);
        tvResumenPaqVal = findViewById(R.id.tvResumenPaquetesVal);
        tvResumenUnidVal = findViewById(R.id.tvResumenUnidadesVal);
        tvResumenTotalUnidVal = findViewById(R.id.tvResumenTotalUnidadesVal);
        tvResumenMetodoPagoVal = findViewById(R.id.tvResumenMetodoPagoVal);
        tvResumenTipoVentaVal = findViewById(R.id.tvResumenTipoVentaVal);
        tvResumenTotalVentaVal = findViewById(R.id.tvResumenTotalVentaVal);

        btnConfirmarPedidoFinal = findViewById(R.id.btnConfirmarPedidoFinal);
    }

    private void setupRecyclerView() {
        rvCarrito.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CarritoAdapter(this, CarritoManager.getInstance().getItems(), new CarritoAdapter.OnCarritoChangeListener() {
            @Override
            public void onChange() {
                actualizarVista();
            }
        });
        rvCarrito.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnVolverCarrito != null) {
            btnVolverCarrito.setOnClickListener(v -> finish());
        }

        if (btnVaciar != null) {
            btnVaciar.setOnClickListener(v -> {
                CarritoManager.getInstance().limpiar();
                actualizarVista();
                Toast.makeText(this, "Carrito vaciado", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnIrACatalogo != null) {
            btnIrACatalogo.setOnClickListener(v -> finish());
        }

        if (btnContinuar != null) {
            btnContinuar.setOnClickListener(v -> {
                if (CarritoManager.getInstance().getItems().isEmpty()) {
                    Toast.makeText(this, "Tu carrito está vacío", Toast.LENGTH_SHORT).show();
                    return;
                }
                mostrarStep2();
            });
        }

        if (btnVolverDatosPedido != null) {
            btnVolverDatosPedido.setOnClickListener(v -> mostrarStep1());
        }

        // Selección Venta Directa vs Cliente Formal
        if (cardVentaDirecta != null) {
            cardVentaDirecta.setOnClickListener(v -> seleccionarVentaDirecta());
        }
        if (cardClienteFormal != null) {
            cardClienteFormal.setOnClickListener(v -> seleccionarClienteFormal());
        }

        // Selección Métodos de Pago
        if (cardPagoEfectivo != null) cardPagoEfectivo.setOnClickListener(v -> seleccionarMetodoPago("Efectivo", cardPagoEfectivo));
        if (cardPagoYape != null) cardPagoYape.setOnClickListener(v -> seleccionarMetodoPago("Yape", cardPagoYape));
        if (cardPagoTarjeta != null) cardPagoTarjeta.setOnClickListener(v -> seleccionarMetodoPago("Tarjeta", cardPagoTarjeta));

        if (spinnerClienteDatosPedido != null) {
            spinnerClienteDatosPedido.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < listaClientes.size()) {
                        clienteSeleccionado = listaClientes.get(position);
                        actualizarResumenStep2();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (btnConfirmarPedidoFinal != null) {
            btnConfirmarPedidoFinal.setOnClickListener(v -> ejecutarConfirmarPedido());
        }
    }

    private void mostrarStep1() {
        if (layoutStepCarrito != null) layoutStepCarrito.setVisibility(View.VISIBLE);
        if (layoutStepDatosPedido != null) layoutStepDatosPedido.setVisibility(View.GONE);
    }

    private void mostrarStep2() {
        if (layoutStepCarrito != null) layoutStepCarrito.setVisibility(View.GONE);
        if (layoutStepDatosPedido != null) layoutStepDatosPedido.setVisibility(View.VISIBLE);
        cargarClientesParaStep2();
        actualizarResumenStep2();
    }

    private void seleccionarVentaDirecta() {
        esVentaDirecta = true;
        clienteSeleccionado = null;
        if (cardVentaDirecta != null) {
            cardVentaDirecta.setStrokeColor(Color.parseColor("#2563EB"));
            cardVentaDirecta.setStrokeWidth(2);
        }
        if (cardClienteFormal != null) {
            cardClienteFormal.setStrokeColor(Color.parseColor("#2E3D59"));
            cardClienteFormal.setStrokeWidth(1);
        }
        if (layoutSpinnerClienteContainer != null) {
            layoutSpinnerClienteContainer.setVisibility(View.GONE);
        }
        actualizarResumenStep2();
    }

    private void seleccionarClienteFormal() {
        esVentaDirecta = false;
        if (cardClienteFormal != null) {
            cardClienteFormal.setStrokeColor(Color.parseColor("#2563EB"));
            cardClienteFormal.setStrokeWidth(2);
        }
        if (cardVentaDirecta != null) {
            cardVentaDirecta.setStrokeColor(Color.parseColor("#2E3D59"));
            cardVentaDirecta.setStrokeWidth(1);
        }
        if (layoutSpinnerClienteContainer != null) {
            layoutSpinnerClienteContainer.setVisibility(View.VISIBLE);
        }
        actualizarResumenStep2();
    }

    private void seleccionarMetodoPago(String metodo, MaterialCardView selectedCard) {
        metodoPagoSeleccionado = metodo;
        MaterialCardView[] cards = {cardPagoEfectivo, cardPagoYape, cardPagoTarjeta};
        for (MaterialCardView c : cards) {
            if (c != null) {
                if (c == selectedCard) {
                    c.setStrokeColor(Color.parseColor("#2563EB"));
                    c.setStrokeWidth(2);
                } else {
                    c.setStrokeColor(Color.parseColor("#2E3D59"));
                    c.setStrokeWidth(1);
                }
            }
        }
        actualizarResumenStep2();
    }

    private void cargarClientesParaStep2() {
        ApiClient.getApiService().getUsuarios().enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResponse> call, @NonNull Response<UsuarioResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaClientes = response.body().getData();
                } else {
                    listaClientes = new ArrayList<>();
                }

                if (listaClientes.isEmpty()) {
                    Usuario def = new Usuario();
                    def.setId(1);
                    def.setNombres("Cliente General");
                    listaClientes.add(def);
                }

                ArrayAdapter<Usuario> adapterSpinner = new ArrayAdapter<>(CarritoActivity.this, android.R.layout.simple_spinner_item, listaClientes);
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                if (spinnerClienteDatosPedido != null) {
                    spinnerClienteDatosPedido.setAdapter(adapterSpinner);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResponse> call, @NonNull Throwable t) {
                listaClientes = new ArrayList<>();
                Usuario def = new Usuario();
                def.setId(1);
                def.setNombres("Cliente General");
                listaClientes.add(def);
                ArrayAdapter<Usuario> adapterSpinner = new ArrayAdapter<>(CarritoActivity.this, android.R.layout.simple_spinner_item, listaClientes);
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                if (spinnerClienteDatosPedido != null) {
                    spinnerClienteDatosPedido.setAdapter(adapterSpinner);
                }
            }
        });
    }

    private void actualizarResumenStep2() {
        List<ItemCarrito> items = CarritoManager.getInstance().getItems();
        int prodCount = items.size();
        int totalPaq = 0;
        int totalUnidSueltas = 0;
        int totalUnidBase = 0;

        for (ItemCarrito it : items) {
            totalPaq += it.getCantidadPaquetes();
            totalUnidSueltas += it.getCantidadUnidades();
            totalUnidBase += it.getCantidadTotalUnidades();
        }

        double totalVenta = CarritoManager.getInstance().obtenerTotal();

        if (tvResumenProdVal != null) tvResumenProdVal.setText(String.valueOf(prodCount));
        if (tvResumenPaqVal != null) tvResumenPaqVal.setText(String.valueOf(totalPaq));
        if (tvResumenUnidVal != null) tvResumenUnidVal.setText(String.valueOf(totalUnidSueltas));
        if (tvResumenTotalUnidVal != null) tvResumenTotalUnidVal.setText(String.valueOf(totalUnidBase));
        if (tvResumenMetodoPagoVal != null) tvResumenMetodoPagoVal.setText(metodoPagoSeleccionado);

        if (tvResumenTipoVentaVal != null) {
            if (esVentaDirecta) {
                tvResumenTipoVentaVal.setText("Directa");
            } else {
                tvResumenTipoVentaVal.setText(clienteSeleccionado != null ? clienteSeleccionado.getNombres() : "Cliente");
            }
        }

        if (tvResumenTotalVentaVal != null) {
            tvResumenTotalVentaVal.setText(String.format(Locale.US, "S/ %.2f", totalVenta));
        }
    }

    private void actualizarVista() {
        List<ItemCarrito> items = CarritoManager.getInstance().getItems();
        if (items.isEmpty()) {
            if (layoutVacio != null) layoutVacio.setVisibility(View.VISIBLE);
            if (rvCarrito != null) rvCarrito.setVisibility(View.GONE);
            if (btnContinuar != null) btnContinuar.setEnabled(false);
            if (tvTotalCalculado != null) tvTotalCalculado.setText("S/ 0.00");
            if (tvResumenProductos != null) tvResumenProductos.setText("0");
            if (tvResumenUnidades != null) tvResumenUnidades.setText("0");
        } else {
            if (layoutVacio != null) layoutVacio.setVisibility(View.GONE);
            if (rvCarrito != null) rvCarrito.setVisibility(View.VISIBLE);
            if (btnContinuar != null) btnContinuar.setEnabled(true);

            double total = CarritoManager.getInstance().obtenerTotal();
            if (tvTotalCalculado != null) {
                tvTotalCalculado.setText(String.format(Locale.US, "S/ %.2f", total));
            }
            if (tvResumenProductos != null) {
                tvResumenProductos.setText(String.valueOf(items.size()));
            }
            int totalUnidades = 0;
            for (ItemCarrito it : items) {
                totalUnidades += it.getCantidadTotalUnidades();
            }
            if (tvResumenUnidades != null) {
                tvResumenUnidades.setText(String.valueOf(totalUnidades));
            }

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void ejecutarConfirmarPedido() {
        List<ItemCarrito> items = CarritoManager.getInstance().getItems();
        if (items.isEmpty()) {
            Toast.makeText(this, "Tu carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer clienteId = null;
        if (!esVentaDirecta && clienteSeleccionado != null) {
            clienteId = clienteSeleccionado.getId();
        }

        List<PedidoItemRequest> itemsRequest = new ArrayList<>();
        for (ItemCarrito item : items) {
            if (item.isOferta()) {
                itemsRequest.add(new PedidoItemRequest("OFERTA", item.getOfertaId(), item.getCantidadOfertas()));
            } else {
                itemsRequest.add(new PedidoItemRequest(
                        item.getProductoId(),
                        item.getTipoVenta(),
                        item.getCantidadPaquetes(),
                        item.getCantidadUnidades()
                ));
            }
        }

        double totalVentaVal = CarritoManager.getInstance().obtenerTotal();
        String totalStr = String.format(Locale.US, "%.2f", totalVentaVal);
        PedidoRequest pedidoRequest = new PedidoRequest(clienteId, metodoPagoSeleccionado, totalStr, itemsRequest);

        if (btnConfirmarPedidoFinal != null) btnConfirmarPedidoFinal.setEnabled(false);

        ApiClient.getApiService().crearPedidoNuevo(pedidoRequest).enqueue(new Callback<PedidoResponse>() {
            @Override
            public void onResponse(@NonNull Call<PedidoResponse> call, @NonNull Response<PedidoResponse> response) {
                if (btnConfirmarPedidoFinal != null) btnConfirmarPedidoFinal.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CarritoActivity.this, "¡Pedido registrado correctamente!", Toast.LENGTH_SHORT).show();
                    CarritoManager.getInstance().limpiar();

                    Intent intent = new Intent(CarritoActivity.this, PedidosActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = response.body() != null && response.body().getMessage() != null
                            ? response.body().getMessage()
                            : RespuestaApi.mensajeError(response, "Error al crear pedido");
                    Toast.makeText(CarritoActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PedidoResponse> call, @NonNull Throwable t) {
                if (btnConfirmarPedidoFinal != null) btnConfirmarPedidoFinal.setEnabled(true);
                Toast.makeText(CarritoActivity.this, "Error de conexión al enviar el pedido", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =======================================================
    // ADAPTER RECYCLERVIEW DEL CARRITO (REDISEÑADO)
    // =======================================================

    private static class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

        interface OnCarritoChangeListener {
            void onChange();
        }

        private final Context context;
        private final List<ItemCarrito> items;
        private final OnCarritoChangeListener listener;

        public CarritoAdapter(Context context, List<ItemCarrito> items, OnCarritoChangeListener listener) {
            this.context = context;
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_carrito, parent, false);
            return new CarritoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
            ItemCarrito item = items.get(position);
            if (item == null) return;

            holder.tvNombre.setText(item.getProductoNombre());
            holder.tvId.setText("ID: #" + item.getProductoId());
            holder.tvPrecioPaquete.setText(String.format(Locale.US, "S/ %.2f\npor paquete", item.getPrecioVentaPaquete()));
            holder.tvPrecioUnidad.setText(String.format(Locale.US, "S/ %.2f\npor unidad", item.getPrecioVentaUnidad()));
            holder.tvStock.setText("Stock: " + item.getStock() + " unid.");
            holder.tvSubtotal.setText(String.format(Locale.US, "S/ %.2f", item.getSubtotal()));

            String tipo = item.getTipoVenta();

            // Configurar botones de tipo de venta
            actualizarEstadoTipoVenta(holder, tipo);

            holder.btnTipoPaquete.setOnClickListener(v -> {
                item.setTipoVenta("PAQUETE");
                if (item.getCantidadPaquetes() <= 0) item.setCantidadPaquetes(1);
                notifyItemChanged(position);
                if (listener != null) listener.onChange();
            });

            holder.btnTipoUnidad.setOnClickListener(v -> {
                item.setTipoVenta("UNIDAD");
                if (item.getCantidadUnidades() <= 0) item.setCantidadUnidades(1);
                notifyItemChanged(position);
                if (listener != null) listener.onChange();
            });

            holder.btnTipoMixta.setOnClickListener(v -> {
                item.setTipoVenta("MIXTA");
                if (item.getCantidadPaquetes() <= 0) item.setCantidadPaquetes(1);
                if (item.getCantidadUnidades() <= 0) item.setCantidadUnidades(1);
                notifyItemChanged(position);
                if (listener != null) listener.onChange();
            });

            // Steppers Paquete
            holder.tvCantidadPaq.setText(String.valueOf(item.getCantidadPaquetes()));
            int totalUnidPaq = item.getCantidadPaquetes() * item.getUnidadesPorPaquete();
            holder.tvInfoPaquete.setText(item.getUnidadesPorPaquete() + " unidades\npor paquete\nTotal unidades: " + totalUnidPaq);

            holder.btnMasPaq.setOnClickListener(v -> {
                item.setCantidadPaquetes(item.getCantidadPaquetes() + 1);
                notifyItemChanged(position);
                if (listener != null) listener.onChange();
            });

            holder.btnMenosPaq.setOnClickListener(v -> {
                if (item.getCantidadPaquetes() > 1) {
                    item.setCantidadPaquetes(item.getCantidadPaquetes() - 1);
                    notifyItemChanged(position);
                    if (listener != null) listener.onChange();
                } else if ("MIXTA".equalsIgnoreCase(item.getTipoVenta()) && item.getCantidadUnidades() > 0) {
                    item.setCantidadPaquetes(0);
                    notifyItemChanged(position);
                    if (listener != null) listener.onChange();
                } else {
                    CarritoManager.getInstance().eliminarItem(position);
                    notifyDataSetChanged();
                    if (listener != null) listener.onChange();
                }
            });

            // Steppers Unidad
            holder.tvCantidadUnid.setText(String.valueOf(item.getCantidadUnidades()));

            holder.btnMasUnid.setOnClickListener(v -> {
                item.setCantidadUnidades(item.getCantidadUnidades() + 1);
                notifyItemChanged(position);
                if (listener != null) listener.onChange();
            });

            holder.btnMenosUnid.setOnClickListener(v -> {
                if (item.getCantidadUnidades() > 1) {
                    item.setCantidadUnidades(item.getCantidadUnidades() - 1);
                    notifyItemChanged(position);
                    if (listener != null) listener.onChange();
                } else {
                    CarritoManager.getInstance().eliminarItem(position);
                    notifyDataSetChanged();
                    if (listener != null) listener.onChange();
                }
            });

            // Steppers Mixta
            holder.tvCantidadMixtaPaq.setText(String.valueOf(item.getCantidadPaquetes()));
            holder.tvCantidadMixtaUnid.setText(String.valueOf(item.getCantidadUnidades()));

            holder.btnMasMixtaPaq.setOnClickListener(v -> {
                item.setCantidadPaquetes(item.getCantidadPaquetes() + 1);
                notifyItemChanged(position);
                if (listener != null) listener.onChange();
            });

            holder.btnMenosMixtaPaq.setOnClickListener(v -> {
                if (item.getCantidadPaquetes() > 0) {
                    item.setCantidadPaquetes(item.getCantidadPaquetes() - 1);
                    notifyItemChanged(position);
                    if (listener != null) listener.onChange();
                }
            });

            holder.btnMasMixtaUnid.setOnClickListener(v -> {
                item.setCantidadUnidades(item.getCantidadUnidades() + 1);
                notifyItemChanged(position);
                if (listener != null) listener.onChange();
            });

            holder.btnMenosMixtaUnid.setOnClickListener(v -> {
                if (item.getCantidadUnidades() > 0) {
                    item.setCantidadUnidades(item.getCantidadUnidades() - 1);
                    notifyItemChanged(position);
                    if (listener != null) listener.onChange();
                }
            });

            holder.btnEliminar.setOnClickListener(v -> {
                CarritoManager.getInstance().eliminarItem(position);
                notifyDataSetChanged();
                if (listener != null) listener.onChange();
            });
        }

        private void actualizarEstadoTipoVenta(CarritoViewHolder holder, String tipo) {
            holder.layoutPaq.setVisibility(View.GONE);
            holder.layoutUnid.setVisibility(View.GONE);
            holder.layoutMixta.setVisibility(View.GONE);

            // Estilos por defecto (inactivo)
            estilizarBotonInactivo(holder.btnTipoPaquete);
            estilizarBotonInactivo(holder.btnTipoUnidad);
            estilizarBotonInactivo(holder.btnTipoMixta);

            if ("PAQUETE".equalsIgnoreCase(tipo)) {
                estilizarBotonActivo(holder.btnTipoPaquete);
                holder.layoutPaq.setVisibility(View.VISIBLE);
            } else if ("UNIDAD".equalsIgnoreCase(tipo)) {
                estilizarBotonActivo(holder.btnTipoUnidad);
                holder.layoutUnid.setVisibility(View.VISIBLE);
            } else {
                estilizarBotonActivo(holder.btnTipoMixta);
                holder.layoutMixta.setVisibility(View.VISIBLE);
            }
        }

        private void estilizarBotonActivo(MaterialButton btn) {
            btn.setBackgroundColor(Color.parseColor("#F97316"));
            btn.setTextColor(Color.WHITE);
        }

        private void estilizarBotonInactivo(MaterialButton btn) {
            btn.setBackgroundColor(Color.parseColor("#0F172A"));
            btn.setTextColor(Color.parseColor("#94A3B8"));
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        static class CarritoViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre;
            TextView tvId;
            TextView tvPrecioPaquete;
            TextView tvPrecioUnidad;
            TextView tvStock;
            TextView tvSubtotal;

            MaterialButton btnTipoPaquete;
            MaterialButton btnTipoUnidad;
            MaterialButton btnTipoMixta;

            LinearLayout layoutPaq;
            LinearLayout layoutUnid;
            LinearLayout layoutMixta;

            MaterialButton btnMenosPaq, btnMasPaq;
            TextView tvCantidadPaq, tvInfoPaquete;

            MaterialButton btnMenosUnid, btnMasUnid;
            TextView tvCantidadUnid;

            MaterialButton btnMenosMixtaPaq, btnMasMixtaPaq;
            TextView tvCantidadMixtaPaq;

            MaterialButton btnMenosMixtaUnid, btnMasMixtaUnid;
            TextView tvCantidadMixtaUnid;

            ImageView btnEliminar;

            public CarritoViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvNombreItemCarrito);
                tvId = itemView.findViewById(R.id.tvIdItemCarrito);
                tvPrecioPaquete = itemView.findViewById(R.id.tvPrecioPaqueteItem);
                tvPrecioUnidad = itemView.findViewById(R.id.tvPrecioUnidadItem);
                tvStock = itemView.findViewById(R.id.tvStockItemCarrito);
                tvSubtotal = itemView.findViewById(R.id.tvSubtotalItemCarrito);

                btnTipoPaquete = itemView.findViewById(R.id.btnTipoPaquete);
                btnTipoUnidad = itemView.findViewById(R.id.btnTipoUnidad);
                btnTipoMixta = itemView.findViewById(R.id.btnTipoMixta);

                layoutPaq = itemView.findViewById(R.id.layoutSteppersPaquete);
                layoutUnid = itemView.findViewById(R.id.layoutSteppersUnidad);
                layoutMixta = itemView.findViewById(R.id.layoutSteppersMixta);

                btnMenosPaq = itemView.findViewById(R.id.btnMenosPaq);
                btnMasPaq = itemView.findViewById(R.id.btnMasPaq);
                tvCantidadPaq = itemView.findViewById(R.id.tvCantidadPaq);
                tvInfoPaquete = itemView.findViewById(R.id.tvInfoPaquete);

                btnMenosUnid = itemView.findViewById(R.id.btnMenosUnid);
                btnMasUnid = itemView.findViewById(R.id.btnMasUnid);
                tvCantidadUnid = itemView.findViewById(R.id.tvCantidadUnid);

                btnMenosMixtaPaq = itemView.findViewById(R.id.btnMenosMixtaPaq);
                btnMasMixtaPaq = itemView.findViewById(R.id.btnMasMixtaPaq);
                tvCantidadMixtaPaq = itemView.findViewById(R.id.tvCantidadMixtaPaq);

                btnMenosMixtaUnid = itemView.findViewById(R.id.btnMenosMixtaUnid);
                btnMasMixtaUnid = itemView.findViewById(R.id.btnMasMixtaUnid);
                tvCantidadMixtaUnid = itemView.findViewById(R.id.tvCantidadMixtaUnid);

                btnEliminar = itemView.findViewById(R.id.btnEliminarCarrito);
            }
        }
    }
}
