package com.example.adisan_app_android;

import android.graphics.Color;
import android.os.Bundle;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedidoDetalleActivity extends AppCompatActivity {

    private int pedidoId = -1;

    // Vistas principales
    private ImageView btnVolverDetalle;
    private ProgressBar pbLoadingDetalle;
    private ScrollView svContent;
    private LinearLayout bottomBar;

    // Header
    private TextView tvDetalleTitle;
    private TextView tvDetalleStatus;
    private TextView tvDetalleDate;

    // Información del pedido
    private TextView tvInfoCliente;
    private TextView tvInfoClienteId;
    private TextView tvInfoTipoVenta;
    private TextView tvInfoMetodoPago;
    private MaterialCardView cvClienteAvatar;
    private ImageView ivClienteAvatar;

    // Resumen
    private TextView tvResumenProductos;
    private TextView tvResumenPaquetes;
    private TextView tvResumenUnidades;
    private TextView tvResumenTotalUnidades;
    private TextView tvResumenTotalMonto;

    private MaterialCardView tlDotPendiente, tlRingPendiente, tlDotAceptado, tlDotEnCamino, tlDotEntregado;
    private ImageView tlCheckPendiente, tlCheckAceptado, tlCheckEnCamino;
    private TextView tlTextPendiente, tlTextAceptado, tlTextEnCamino, tlTextEntregado, tlDatePendiente;
    private View tlLine1, tlLine2, tlLine3;
    private LinearLayout layoutTimelineCancelado, layoutTimeline;
    private View layoutTimelineAceptado, layoutTimelineEnCamino, layoutTimelineEntregado;

    // Tabs
    private TextView tvTabProductos;

    // Acciones
    private MaterialButton btnCambiarEstado;
    private MaterialButton btnCancelarPedido;

    private Pedido pedidoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_detalle);

        pedidoId = getIntent().getIntExtra("pedido_id", -1);
        if (pedidoId <= 0) {
            pedidoId = getIntent().getIntExtra("id", getIntent().getIntExtra("id_pedido", -1));
        }

        if (pedidoId <= 0) {
            Toast.makeText(this, "ID de pedido inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        if (tvDetalleTitle != null) {
            tvDetalleTitle.setText("Pedido #" + pedidoId);
        }
        setupListeners();
        cargarDetallePedido();
    }

    private void initViews() {
        btnVolverDetalle = findViewById(R.id.btnVolverDetalle);
        pbLoadingDetalle = findViewById(R.id.pbLoadingDetalle);
        svContent = findViewById(R.id.svContent);
        bottomBar = findViewById(R.id.bottomBar);

        tvDetalleTitle = findViewById(R.id.tvDetalleTitle);
        tvDetalleStatus = findViewById(R.id.tvDetalleStatus);
        tvDetalleDate = findViewById(R.id.tvDetalleDate);

        tvInfoCliente = findViewById(R.id.tvInfoCliente);
        tvInfoClienteId = findViewById(R.id.tvInfoClienteId);
        tvInfoTipoVenta = findViewById(R.id.tvInfoTipoVenta);
        tvInfoMetodoPago = findViewById(R.id.tvInfoMetodoPago);
        cvClienteAvatar = findViewById(R.id.cvClienteAvatar);
        ivClienteAvatar = findViewById(R.id.ivClienteAvatar);

        tvResumenProductos = findViewById(R.id.tvResumenProductos);
        tvResumenPaquetes = findViewById(R.id.tvResumenPaquetes);
        tvResumenUnidades = findViewById(R.id.tvResumenUnidades);
        tvResumenTotalUnidades = findViewById(R.id.tvResumenTotalUnidades);
        tvResumenTotalMonto = findViewById(R.id.tvResumenTotalMonto);

        tlDotPendiente = findViewById(R.id.tlDotPendiente);
        tlRingPendiente = findViewById(R.id.tlRingPendiente);
        tlDotAceptado = findViewById(R.id.tlDotAceptado);
        tlDotEnCamino = findViewById(R.id.tlDotEnCamino);
        tlDotEntregado = findViewById(R.id.tlDotEntregado);
        
        tlCheckPendiente = findViewById(R.id.tlCheckPendiente);
        tlCheckAceptado = findViewById(R.id.tlCheckAceptado);
        tlCheckEnCamino = findViewById(R.id.tlCheckEnCamino);
        
        tlTextPendiente = findViewById(R.id.tlTextPendiente);
        tlTextAceptado = findViewById(R.id.tlTextAceptado);
        tlTextEnCamino = findViewById(R.id.tlTextEnCamino);
        tlTextEntregado = findViewById(R.id.tlTextEntregado);
        tlDatePendiente = findViewById(R.id.tlDatePendiente);

        tlLine1 = findViewById(R.id.tlLine1);
        tlLine2 = findViewById(R.id.tlLine2);
        tlLine3 = findViewById(R.id.tlLine3);

        layoutTimelineCancelado = findViewById(R.id.layoutTimelineCancelado);
        layoutTimeline = findViewById(R.id.layoutTimeline);
        layoutTimelineAceptado = findViewById(R.id.layoutTimelineAceptado);
        layoutTimelineEnCamino = findViewById(R.id.layoutTimelineEnCamino);
        layoutTimelineEntregado = findViewById(R.id.layoutTimelineEntregado);

        tvTabProductos = findViewById(R.id.tvTabProductos);

        btnCambiarEstado = findViewById(R.id.btnCambiarEstado);
        btnCancelarPedido = findViewById(R.id.btnCancelarPedido);
    }

    private void setupListeners() {
        btnVolverDetalle.setOnClickListener(v -> finish());

        btnCambiarEstado.setOnClickListener(v -> {
            if (pedidoActual != null) {
                mostrarDialogoCambiarEstado(pedidoActual);
            }
        });

        btnCancelarPedido.setOnClickListener(v -> {
            if (pedidoActual != null) {
                mostrarDialogoConfirmarCancelar(pedidoActual);
            }
        });
    }

    private void cargarDetallePedido() {
        pbLoadingDetalle.setVisibility(View.VISIBLE);
        svContent.setVisibility(View.GONE);
        bottomBar.setVisibility(View.GONE);

        ApiClient.getApiService().getPedidoByIdJson(pedidoId).enqueue(new Callback<com.google.gson.JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<com.google.gson.JsonElement> call, @NonNull Response<com.google.gson.JsonElement> response) {
                pbLoadingDetalle.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        com.google.gson.JsonElement elem = response.body();
                        com.google.gson.JsonObject pedidoObj = elem.isJsonObject() ? elem.getAsJsonObject() : new com.google.gson.JsonObject();
                        if (pedidoObj.has("data") && pedidoObj.get("data").isJsonObject()) {
                            pedidoObj = pedidoObj.getAsJsonObject("data");
                        } else if (pedidoObj.has("pedido") && pedidoObj.get("pedido").isJsonObject()) {
                            pedidoObj = pedidoObj.getAsJsonObject("pedido");
                        }

                        pedidoActual = new com.google.gson.Gson().fromJson(pedidoObj, Pedido.class);
                        if (pedidoActual != null) {
                            if (pedidoActual.getId() <= 0 && pedidoId > 0) {
                                pedidoActual.setId(pedidoId);
                            }
                            android.util.Log.d("TIMELINE_DEBUG", "Pedido recibido id=" + pedidoActual.getId() + ", estado=" + pedidoActual.getEstado());
                            poblarDatosUI(pedidoActual);
                        } else {
                            Toast.makeText(PedidoDetalleActivity.this, "Error al cargar detalle", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("TIMELINE_DEBUG", "Error parseando JsonElement pedido", e);
                        Toast.makeText(PedidoDetalleActivity.this, "Error al cargar detalle", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(PedidoDetalleActivity.this, "Error al cargar detalle (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.google.gson.JsonElement> call, @NonNull Throwable t) {
                pbLoadingDetalle.setVisibility(View.GONE);
                Toast.makeText(PedidoDetalleActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void poblarDatosUI(Pedido pedido) {
        svContent.setVisibility(View.VISIBLE);
        bottomBar.setVisibility(View.VISIBLE);

        if (pedido != null && pedido.getId() <= 0 && pedidoId > 0) {
            pedido.setId(pedidoId);
        }

        int idAMostrar = (pedido != null && pedido.getId() > 0) ? pedido.getId() : pedidoId;

        // Header
        tvDetalleTitle.setText("Pedido #" + idAMostrar);
        tvDetalleDate.setText(pedido.getFecha() != null ? pedido.getFecha() : "--/--/---- --:--");

        String estado = pedido.getEstado() != null ? pedido.getEstado().toLowerCase() : "pendiente";
        if (estado.isEmpty()) estado = "pendiente";
        
        tvDetalleStatus.setText(estado.substring(0, 1).toUpperCase() + estado.substring(1).replace("_", " "));
        configurarColoresEstado(estado);

        // Información del cliente
        if (pedido.getClienteId() == null || pedido.getClienteId() == 0 || "Venta Mostrador".equalsIgnoreCase(pedido.getCliente())) {
            tvInfoCliente.setText("Venta directa");
            tvInfoClienteId.setVisibility(View.GONE);
            tvInfoTipoVenta.setText("Venta directa");
            ivClienteAvatar.setImageResource(R.drawable.ic_box);
        } else {
            tvInfoCliente.setText(pedido.getCliente());
            tvInfoClienteId.setText("ID: " + pedido.getClienteId());
            tvInfoClienteId.setVisibility(View.VISIBLE);
            tvInfoTipoVenta.setText("Cliente registrado");
            ivClienteAvatar.setImageResource(R.drawable.ic_user);
        }

        tvInfoMetodoPago.setText(pedido.getMetodoPago() != null ? pedido.getMetodoPago() : "Efectivo");

        // Resumen y Tab Productos
        int nProductos = 0;
        int nPaquetes = 0;
        int nUnidades = 0;
        int totalUnidadesAcumuladas = 0;

        if (pedido.getDetalles() != null) {
            nProductos = pedido.getDetalles().size();
            for (DetallePedido det : pedido.getDetalles()) {
                int paq = det.getCantidadPaquetes();
                int uni = det.getCantidadUnidades();
                if (paq == 0 && uni == 0) {
                    if ("PAQUETE".equalsIgnoreCase(det.getTipoVenta())) {
                        paq = det.getCantidad();
                    } else if ("UNIDAD".equalsIgnoreCase(det.getTipoVenta())) {
                        uni = det.getCantidad();
                    } else if ("MIXTA".equalsIgnoreCase(det.getTipoVenta())) {
                        paq = det.getCantidadPaquetes();
                        uni = det.getCantidadUnidades();
                    } else {
                        uni = det.getCantidad();
                    }
                }
                nPaquetes += paq;
                nUnidades += uni;
                totalUnidadesAcumuladas += det.getUnidadesTotales();
            }
        }

        double totalMonto = pedido.getTotalDouble();
        if (totalMonto == 0.0 && pedido.getDetalles() != null) {
            for (DetallePedido det : pedido.getDetalles()) {
                totalMonto += det.getSubtotalDouble();
            }
        }

        tvTabProductos.setText("Productos (" + nProductos + ")");
        tvResumenProductos.setText(String.valueOf(nProductos));
        tvResumenPaquetes.setText(String.valueOf(nPaquetes));
        tvResumenUnidades.setText(String.valueOf(nUnidades));
        tvResumenTotalUnidades.setText(String.valueOf(totalUnidadesAcumuladas));
        
        tvResumenTotalMonto.setText(String.format(Locale.US, "S/ %.2f", totalMonto));

        // Configurar botones base
        if ("cancelado".equals(estado)) {
            btnCambiarEstado.setVisibility(View.GONE);
            btnCancelarPedido.setVisibility(View.GONE);
        } else if ("entregado".equals(estado)) {
            btnCambiarEstado.setVisibility(View.GONE);
            btnCancelarPedido.setVisibility(View.GONE);
        } else {
            btnCambiarEstado.setVisibility(View.VISIBLE);
            btnCancelarPedido.setVisibility(View.VISIBLE);
        }
    }

    private void configurarColoresEstado(String estado) {
        String estadoLimpio = estado != null ? estado.trim().toLowerCase(java.util.Locale.ROOT) : "pendiente";

        // Reset a Inactivo
        int colorInactivo = Color.parseColor("#334155");
        int colorFondoInactivo = Color.parseColor("#0F172A");
        int textInactivo = Color.parseColor("#94A3B8");

        tlRingPendiente.setVisibility(View.GONE);
        tlDotPendiente.setCardBackgroundColor(colorFondoInactivo);
        tlDotPendiente.setStrokeColor(colorInactivo);
        tlDotPendiente.setStrokeWidth(2);
        tlTextPendiente.setTextColor(textInactivo);
        tlTextPendiente.setTypeface(null, android.graphics.Typeface.NORMAL);
        tlDatePendiente.setVisibility(View.GONE);
        if (tlCheckPendiente != null) tlCheckPendiente.setVisibility(View.GONE);

        tlDotAceptado.setCardBackgroundColor(colorFondoInactivo);
        tlDotAceptado.setStrokeColor(colorInactivo);
        tlDotAceptado.setStrokeWidth(2);
        tlTextAceptado.setTextColor(textInactivo);
        tlTextAceptado.setTypeface(null, android.graphics.Typeface.NORMAL);
        if (tlCheckAceptado != null) tlCheckAceptado.setVisibility(View.GONE);

        tlDotEnCamino.setCardBackgroundColor(colorFondoInactivo);
        tlDotEnCamino.setStrokeColor(colorInactivo);
        tlDotEnCamino.setStrokeWidth(2);
        tlTextEnCamino.setTextColor(textInactivo);
        tlTextEnCamino.setTypeface(null, android.graphics.Typeface.NORMAL);
        if (tlCheckEnCamino != null) tlCheckEnCamino.setVisibility(View.GONE);

        tlDotEntregado.setCardBackgroundColor(colorFondoInactivo);
        tlDotEntregado.setStrokeColor(colorInactivo);
        tlDotEntregado.setStrokeWidth(2);
        tlTextEntregado.setTextColor(textInactivo);
        tlTextEntregado.setTypeface(null, android.graphics.Typeface.NORMAL);

        tlLine1.setBackgroundColor(colorInactivo);
        tlLine2.setBackgroundColor(colorInactivo);
        tlLine3.setBackgroundColor(colorInactivo);

        if (layoutTimelineAceptado != null) layoutTimelineAceptado.setVisibility(View.VISIBLE);
        if (layoutTimelineEnCamino != null) layoutTimelineEnCamino.setVisibility(View.VISIBLE);
        if (layoutTimelineEntregado != null) layoutTimelineEntregado.setVisibility(View.VISIBLE);
        tlLine2.setVisibility(View.VISIBLE);
        tlLine3.setVisibility(View.VISIBLE);

        layoutTimelineCancelado.setVisibility(View.GONE);

        int colorNaranja = Color.parseColor("#F59E0B");
        int colorAzul = Color.parseColor("#3B82F6");
        int colorVerde = Color.parseColor("#10B981");

        // Top Status Badge
        switch (estadoLimpio) {
            case "aceptado":
            case "aprobado":
                tvDetalleStatus.setTextColor(colorAzul);
                tvDetalleStatus.setBackgroundColor(Color.parseColor("#0D2247"));
                // Timeline
                tlRingPendiente.setVisibility(View.GONE);
                tlDotPendiente.setCardBackgroundColor(colorVerde);
                tlDotPendiente.setStrokeWidth(0);
                tlTextPendiente.setTextColor(colorVerde);
                if (tlCheckPendiente != null) tlCheckPendiente.setVisibility(View.VISIBLE);
                tlLine1.setBackgroundColor(colorAzul);
                
                tlDotAceptado.setCardBackgroundColor(colorAzul);
                tlDotAceptado.setStrokeWidth(0);
                tlTextAceptado.setTextColor(colorAzul);
                tlTextAceptado.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case "en_camino":
            case "en camino":
            case "encamino":
                tvDetalleStatus.setTextColor(colorAzul);
                tvDetalleStatus.setBackgroundColor(Color.parseColor("#0D2247"));
                // Timeline
                tlRingPendiente.setVisibility(View.GONE);
                tlDotPendiente.setCardBackgroundColor(colorVerde);
                tlDotPendiente.setStrokeWidth(0);
                tlTextPendiente.setTextColor(colorVerde);
                if (tlCheckPendiente != null) tlCheckPendiente.setVisibility(View.VISIBLE);
                tlLine1.setBackgroundColor(colorVerde);

                tlDotAceptado.setCardBackgroundColor(colorVerde);
                tlDotAceptado.setStrokeWidth(0);
                tlTextAceptado.setTextColor(colorVerde);
                if (tlCheckAceptado != null) tlCheckAceptado.setVisibility(View.VISIBLE);
                tlLine2.setBackgroundColor(colorAzul);

                tlDotEnCamino.setCardBackgroundColor(colorAzul);
                tlDotEnCamino.setStrokeWidth(0);
                tlTextEnCamino.setTextColor(colorAzul);
                tlTextEnCamino.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case "entregado":
            case "completado":
            case "finalizado":
                tvDetalleStatus.setTextColor(colorVerde);
                tvDetalleStatus.setBackgroundColor(Color.parseColor("#062E29"));
                // Timeline - Todos los pasos completados en verde
                tlRingPendiente.setVisibility(View.GONE);
                tlDotPendiente.setCardBackgroundColor(colorVerde);
                tlDotPendiente.setStrokeWidth(0);
                tlTextPendiente.setTextColor(colorVerde);
                if (tlCheckPendiente != null) tlCheckPendiente.setVisibility(View.VISIBLE);
                tlLine1.setBackgroundColor(colorVerde);

                tlDotAceptado.setCardBackgroundColor(colorVerde);
                tlDotAceptado.setStrokeWidth(0);
                tlTextAceptado.setTextColor(colorVerde);
                if (tlCheckAceptado != null) tlCheckAceptado.setVisibility(View.VISIBLE);
                tlLine2.setBackgroundColor(colorVerde);

                tlDotEnCamino.setCardBackgroundColor(colorVerde);
                tlDotEnCamino.setStrokeWidth(0);
                tlTextEnCamino.setTextColor(colorVerde);
                if (tlCheckEnCamino != null) tlCheckEnCamino.setVisibility(View.VISIBLE);
                tlLine3.setBackgroundColor(colorVerde);

                tlDotEntregado.setCardBackgroundColor(colorVerde);
                tlDotEntregado.setStrokeWidth(0);
                tlTextEntregado.setTextColor(colorVerde);
                tlTextEntregado.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case "cancelado":
            case "rechazado":
                int colorRojo = Color.parseColor("#EF4444");
                int colorBgRojo = Color.parseColor("#3B1111");

                tvDetalleStatus.setTextColor(colorRojo);
                tvDetalleStatus.setBackgroundColor(colorBgRojo);
                
                // Timeline
                tlRingPendiente.setVisibility(View.GONE);
                tlDotPendiente.setCardBackgroundColor(colorVerde);
                tlDotPendiente.setStrokeWidth(0);
                tlTextPendiente.setTextColor(colorVerde);
                if (tlCheckPendiente != null) tlCheckPendiente.setVisibility(View.VISIBLE);
                
                tlLine1.setBackgroundColor(colorRojo);

                layoutTimelineCancelado.setVisibility(View.VISIBLE);
                if (layoutTimelineCancelado.getChildCount() > 1 && layoutTimelineCancelado.getChildAt(1) instanceof TextView) {
                    ((TextView) layoutTimelineCancelado.getChildAt(1)).setTextColor(colorRojo);
                }
                if (layoutTimelineCancelado.getChildCount() > 0 && layoutTimelineCancelado.getChildAt(0) instanceof android.view.ViewGroup) {
                    android.view.ViewGroup frame = (android.view.ViewGroup) layoutTimelineCancelado.getChildAt(0);
                    for (int i = 0; i < frame.getChildCount(); i++) {
                        View child = frame.getChildAt(i);
                        if (child instanceof com.google.android.material.card.MaterialCardView) {
                            com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) child;
                            if (card.getStrokeWidth() > 0) {
                                card.setStrokeColor(colorRojo);
                            } else {
                                card.setCardBackgroundColor(colorRojo);
                            }
                        }
                    }
                }
                
                // Ocultar los pasos intermedios no alcanzados
                if (layoutTimelineAceptado != null) layoutTimelineAceptado.setVisibility(View.GONE);
                if (layoutTimelineEnCamino != null) layoutTimelineEnCamino.setVisibility(View.GONE);
                if (layoutTimelineEntregado != null) layoutTimelineEntregado.setVisibility(View.GONE);
                
                tlLine2.setVisibility(View.GONE);
                tlLine3.setVisibility(View.GONE);
                break;
            default: // pendiente
                tvDetalleStatus.setTextColor(colorNaranja);
                tvDetalleStatus.setBackgroundColor(Color.parseColor("#3B2C0D"));
                // Timeline
                tlRingPendiente.setVisibility(View.VISIBLE);
                tlDotPendiente.setCardBackgroundColor(colorNaranja);
                tlDotPendiente.setStrokeWidth(0);
                tlTextPendiente.setTextColor(colorNaranja);
                tlTextPendiente.setTypeface(null, android.graphics.Typeface.BOLD);
                tlDatePendiente.setVisibility(View.VISIBLE);
                if (pedidoActual != null && pedidoActual.getFecha() != null) {
                    tlDatePendiente.setText(pedidoActual.getFecha());
                }
                break;
        }
    }

    // =======================================================
    // LOGICA DE ESTADOS Y CANCELACION IGUAL QUE PEDIDOS_ACTIVITY
    // =======================================================

    private void mostrarDialogoCambiarEstado(Pedido pedido) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cambiar_estado_pedido, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnCerrar = dialogView.findViewById(R.id.btnCerrarDialogEstado);
        android.widget.RadioGroup rgEstados = dialogView.findViewById(R.id.rgEstadosPedido);
        android.widget.RadioButton rbPendiente = dialogView.findViewById(R.id.rbPendiente);
        android.widget.RadioButton rbAceptado = dialogView.findViewById(R.id.rbAceptado);
        android.widget.RadioButton rbEnCamino = dialogView.findViewById(R.id.rbEnCamino);
        android.widget.RadioButton rbEntregado = dialogView.findViewById(R.id.rbEntregado);
        android.widget.RadioButton rbCancelado = dialogView.findViewById(R.id.rbCancelado);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarDialog);
        MaterialButton btnActualizar = dialogView.findViewById(R.id.btnActualizarEstadoDialog);

        if (rbCancelado != null) rbCancelado.setVisibility(View.GONE);

        String pEstado = pedido.getEstado() != null ? pedido.getEstado().toLowerCase() : "pendiente";
        switch (pEstado) {
            case "aceptado": if (rbAceptado != null) rbAceptado.setChecked(true); break;
            case "en_camino":
            case "en camino": if (rbEnCamino != null) rbEnCamino.setChecked(true); break;
            case "entregado": if (rbEntregado != null) rbEntregado.setChecked(true); break;
            default: if (rbPendiente != null) rbPendiente.setChecked(true); break;
        }

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnActualizar.setOnClickListener(v -> {
            String nuevoEstado = "pendiente";
            int checkedId = rgEstados.getCheckedRadioButtonId();
            if (checkedId == R.id.rbAceptado) nuevoEstado = "aceptado";
            else if (checkedId == R.id.rbEnCamino) nuevoEstado = "en_camino";
            else if (checkedId == R.id.rbEntregado) nuevoEstado = "entregado";

            btnActualizar.setText("Actualizando...");
            btnActualizar.setEnabled(false);
            btnCancelar.setEnabled(false);

            ejecutarActualizarEstado(pedido.getId(), nuevoEstado, dialog);
        });

        dialog.show();
    }

    private void mostrarDialogoConfirmarCancelar(Pedido pedido) {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar Pedido")
                .setMessage("¿Estás seguro de cancelar el pedido #" + pedido.getId() + "?")
                .setPositiveButton("Confirmar", (dialog, which) -> ejecutarActualizarEstado(pedido.getId(), "cancelado", null))
                .setNegativeButton("Volver", null)
                .show();
    }

    private void ejecutarActualizarEstado(int pId, String nuevoEstado, AlertDialog dialog) {
        pbLoadingDetalle.setVisibility(View.VISIBLE);
        svContent.setVisibility(View.GONE);
        bottomBar.setVisibility(View.GONE);

        String estadoLower = nuevoEstado != null ? nuevoEstado.trim().toLowerCase() : "pendiente";

        Pedido updateRequest = new Pedido();
        updateRequest.setEstado(estadoLower);

        ApiClient.getApiService().editarPedido(pId, updateRequest).enqueue(new Callback<PedidoResponse>() {
            @Override
            public void onResponse(@NonNull Call<PedidoResponse> call, @NonNull Response<PedidoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PedidoResponse body = response.body();
                    if (body.isSuccess()) {
                        if (dialog != null && dialog.isShowing()) dialog.dismiss();
                        String msg = body.getMessage() != null ? body.getMessage() : "Estado actualizado correctamente";
                        Toast.makeText(PedidoDetalleActivity.this, msg, Toast.LENGTH_SHORT).show();
                        cargarDetallePedido();
                    } else {
                        restaurarModalPorError(dialog, body.getMessage() != null ? body.getMessage() : "Error al actualizar estado");
                    }
                } else {
                    String errorMsg = "Error al actualizar estado";
                    try {
                        if (response.errorBody() != null) {
                            String errStr = response.errorBody().string();
                            PedidoResponse errRes = new com.google.gson.Gson().fromJson(errStr, PedidoResponse.class);
                            if (errRes != null && errRes.getMessage() != null) {
                                errorMsg = errRes.getMessage();
                            }
                        }
                    } catch (Exception ignored) {}

                    restaurarModalPorError(dialog, errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PedidoResponse> call, @NonNull Throwable t) {
                restaurarModalPorError(dialog, "No se pudo conectar con el servidor.");
            }
        });
    }

    private void restaurarModalPorError(AlertDialog dialog, String mensaje) {
        if (dialog != null && dialog.isShowing()) {
            MaterialButton btnActualizar = dialog.findViewById(R.id.btnActualizarEstadoDialog);
            MaterialButton btnCancelar = dialog.findViewById(R.id.btnCancelarDialog);
            if (btnActualizar != null) {
                btnActualizar.setText("Actualizar estado");
                btnActualizar.setEnabled(true);
            }
            if (btnCancelar != null) btnCancelar.setEnabled(true);
        }
        Toast.makeText(PedidoDetalleActivity.this, mensaje, Toast.LENGTH_LONG).show();
        
        pbLoadingDetalle.setVisibility(View.GONE);
        svContent.setVisibility(View.VISIBLE);
        bottomBar.setVisibility(View.VISIBLE);
    }
}