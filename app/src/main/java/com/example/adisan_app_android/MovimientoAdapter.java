package com.example.adisan_app_android;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovimientoAdapter extends RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder> {

    private final Context context;
    private List<MovimientoInventario> movimientoList;

    public MovimientoAdapter(Context context, List<MovimientoInventario> movimientoList) {
        this.context = context;
        this.movimientoList = movimientoList != null ? new ArrayList<>(movimientoList) : new ArrayList<>();
    }

    public void setMovimientos(List<MovimientoInventario> movimientos) {
        this.movimientoList = movimientos != null ? new ArrayList<>(movimientos) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovimientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movimiento, parent, false);
        return new MovimientoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovimientoViewHolder holder, int position) {
        MovimientoInventario mov = movimientoList.get(position);
        if (mov == null) return;

        String tipoRaw = mov.getTipo() != null ? mov.getTipo().trim().toLowerCase() : "";
        int cant = mov.getCantidadUnidades();

        String titleText;
        int titleColor;
        int iconRes;
        int iconBgColor;
        int iconTint;

        String badgeText;
        int badgeBgColor;
        int badgeTextColor;

        String refText = "";
        String detailText = "";

        if (tipoRaw.contains("compra") || tipoRaw.contains("entrada")) {
            titleText = "Compra";
            titleColor = Color.parseColor("#10B981");
            iconRes = R.drawable.ic_shopping_cart;
            iconBgColor = Color.parseColor("#062E29");
            iconTint = Color.parseColor("#10B981");

            badgeText = "+ " + Math.abs(cant) + " un.";
            badgeBgColor = Color.parseColor("#062E29");
            badgeTextColor = Color.parseColor("#10B981");

            refText = (mov.getCompraId() != null && mov.getCompraId() > 0) ? "Compra #" + mov.getCompraId() : "Compra #" + mov.getId();
            if (!TextUtils.isEmpty(mov.getProveedorNombre())) {
                detailText = "Proveedor: " + mov.getProveedorNombre();
            } else if (!TextUtils.isEmpty(mov.getMotivo())) {
                detailText = "Motivo: " + mov.getMotivo();
            }
        } else if (tipoRaw.contains("venta") || tipoRaw.contains("salida")) {
            titleText = "Venta";
            titleColor = Color.parseColor("#EF4444");
            iconRes = R.drawable.ic_shopping_cart;
            iconBgColor = Color.parseColor("#3B1111");
            iconTint = Color.parseColor("#EF4444");

            badgeText = "- " + Math.abs(cant) + " un.";
            badgeBgColor = Color.parseColor("#3B1111");
            badgeTextColor = Color.parseColor("#EF4444");

            refText = (mov.getPedidoId() != null && mov.getPedidoId() > 0) ? "Pedido #" + mov.getPedidoId() : "Venta #" + mov.getId();
            if (!TextUtils.isEmpty(mov.getClienteNombre())) {
                detailText = "Cliente: " + mov.getClienteNombre();
            } else if (!TextUtils.isEmpty(mov.getMotivo())) {
                detailText = "Motivo: " + mov.getMotivo();
            } else {
                detailText = "Cliente: Venta directa";
            }
        } else if (tipoRaw.contains("revers") || tipoRaw.contains("devoluc") || tipoRaw.contains("cancelac")) {
            titleText = "Reversión";
            titleColor = Color.parseColor("#10B981");
            iconRes = R.drawable.ic_refresh;
            iconBgColor = Color.parseColor("#062E29");
            iconTint = Color.parseColor("#10B981");

            badgeText = "+ " + Math.abs(cant) + " un.";
            badgeBgColor = Color.parseColor("#062E29");
            badgeTextColor = Color.parseColor("#10B981");

            if (mov.getPedidoId() != null && mov.getPedidoId() > 0) {
                refText = "Cancelación del pedido #" + mov.getPedidoId();
            } else {
                refText = !TextUtils.isEmpty(mov.getMotivo()) ? mov.getMotivo() : "Cancelación de pedido";
            }
            if (!TextUtils.isEmpty(mov.getClienteNombre())) {
                detailText = "Cliente: " + mov.getClienteNombre();
            }
        } else if (tipoRaw.contains("ajuste")) {
            boolean esEntrada = cant >= 0;
            titleText = esEntrada ? "Ajuste (Entrada)" : "Ajuste (Salida)";
            titleColor = Color.parseColor("#A855F7");
            iconRes = R.drawable.ic_tune;
            iconBgColor = Color.parseColor("#2D124D");
            iconTint = Color.parseColor("#A855F7");

            badgeText = (esEntrada ? "+ " : "- ") + Math.abs(cant) + " un.";
            badgeBgColor = Color.parseColor(esEntrada ? "#062E29" : "#3B1111");
            badgeTextColor = Color.parseColor(esEntrada ? "#10B981" : "#EF4444");

            refText = "Ajuste manual";
            if (!TextUtils.isEmpty(mov.getMotivo())) {
                detailText = "Motivo: " + mov.getMotivo();
            }
        } else if (tipoRaw.contains("perdid") || tipoRaw.contains("pérdid")) {
            titleText = "Pérdida";
            titleColor = Color.parseColor("#EF4444");
            iconRes = R.drawable.ic_box;
            iconBgColor = Color.parseColor("#3B1111");
            iconTint = Color.parseColor("#EF4444");

            badgeText = "- " + Math.abs(cant) + " un.";
            badgeBgColor = Color.parseColor("#3B1111");
            badgeTextColor = Color.parseColor("#EF4444");

            refText = "Salida por pérdida";
            if (!TextUtils.isEmpty(mov.getMotivo())) {
                detailText = "Motivo: " + mov.getMotivo();
            }
        } else {
            titleText = !TextUtils.isEmpty(tipoRaw) ? (tipoRaw.substring(0, 1).toUpperCase() + tipoRaw.substring(1)) : "Movimiento";
            titleColor = Color.parseColor("#CBD5E1");
            iconRes = R.drawable.ic_box;
            iconBgColor = Color.parseColor("#1E293B");
            iconTint = Color.parseColor("#CBD5E1");

            boolean esPositivo = cant >= 0;
            badgeText = (esPositivo ? "+ " : "- ") + Math.abs(cant) + " un.";
            badgeBgColor = Color.parseColor(esPositivo ? "#062E29" : "#3B1111");
            badgeTextColor = Color.parseColor(esPositivo ? "#10B981" : "#EF4444");

            refText = !TextUtils.isEmpty(mov.getMotivo()) ? mov.getMotivo() : "Ajuste de inventario";
        }

        // Título y Estilos
        holder.tvTipo.setText(titleText);
        holder.tvTipo.setTextColor(titleColor);

        if (holder.cardIcon != null) holder.cardIcon.setCardBackgroundColor(iconBgColor);
        if (holder.ivIcon != null) {
            holder.ivIcon.setImageResource(iconRes);
            holder.ivIcon.setColorFilter(iconTint);
        }

        // Cantidad Badge
        holder.tvCantidadBadge.setText(badgeText);
        holder.tvCantidadBadge.setTextColor(badgeTextColor);
        holder.tvCantidadBadge.setBackgroundColor(badgeBgColor);

        // Subtítulo Referencia y Detalle
        holder.tvSubtituloRef.setText(refText);

        if (!TextUtils.isEmpty(detailText)) {
            holder.tvDetalleInvolucrado.setText(detailText);
            holder.tvDetalleInvolucrado.setVisibility(View.VISIBLE);
        } else {
            holder.tvDetalleInvolucrado.setVisibility(View.GONE);
        }

        // Stock Transición
        if (mov.getStockAnterior() != null && mov.getStockNuevo() != null) {
            holder.tvStockTransicion.setText("Stock: " + mov.getStockAnterior() + " → " + mov.getStockNuevo());
            holder.tvStockTransicion.setVisibility(View.VISIBLE);
        } else {
            holder.tvStockTransicion.setVisibility(View.GONE);
        }

        // Fecha y Hora
        String[] fh = formatearFechaYHora(mov.getFecha());
        holder.tvFecha.setText(fh[0]);
        if (!TextUtils.isEmpty(fh[1])) {
            holder.tvHora.setText(fh[1]);
            holder.tvHora.setVisibility(View.VISIBLE);
        } else {
            holder.tvHora.setVisibility(View.GONE);
        }
    }

    private String[] formatearFechaYHora(String rawFecha) {
        if (TextUtils.isEmpty(rawFecha)) {
            return new String[]{"Fecha pendiente", ""};
        }
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
                    int year = Integer.parseInt(dateSplit[0]);
                    int month = Integer.parseInt(dateSplit[1]);
                    int day = Integer.parseInt(dateSplit[2]);

                    String[] meses = {"ene.", "feb.", "mar.", "abr.", "may.", "jun.", "jul.", "ago.", "sep.", "oct.", "nov.", "dic."};
                    String mesNombre = (month >= 1 && month <= 12) ? meses[month - 1] : String.format(Locale.US, "%02d", month);
                    String fechaFormateada = String.format(Locale.US, "%02d %s %d", day, mesNombre, year);

                    String horaFormateada = "";
                    if (!TextUtils.isEmpty(timePart)) {
                        String[] timeSplit = timePart.split(":");
                        if (timeSplit.length >= 2) {
                            int hour = Integer.parseInt(timeSplit[0]);
                            int min = Integer.parseInt(timeSplit[1]);
                            String ampm = hour >= 12 ? "p. m." : "a. m.";
                            int hour12 = hour % 12;
                            if (hour12 == 0) hour12 = 12;
                            horaFormateada = String.format(Locale.US, "%02d:%02d %s", hour12, min, ampm);
                        }
                    }
                    return new String[]{fechaFormateada, horaFormateada};
                }
            }
        } catch (Exception ignored) {}
        return new String[]{rawFecha, ""};
    }

    @Override
    public int getItemCount() {
        return movimientoList != null ? movimientoList.size() : 0;
    }

    public static class MovimientoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo;
        TextView tvSubtituloRef;
        TextView tvDetalleInvolucrado;
        TextView tvCantidadBadge;
        TextView tvStockTransicion;
        TextView tvFecha;
        TextView tvHora;
        MaterialCardView cardIcon;
        ImageView ivIcon;

        public MovimientoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipoMovimiento);
            tvSubtituloRef = itemView.findViewById(R.id.tvSubtituloReferencia);
            tvDetalleInvolucrado = itemView.findViewById(R.id.tvDetalleInvolucrado);
            tvCantidadBadge = itemView.findViewById(R.id.tvCantidadBadge);
            tvStockTransicion = itemView.findViewById(R.id.tvStockTransicion);
            tvFecha = itemView.findViewById(R.id.tvFechaMovimiento);
            tvHora = itemView.findViewById(R.id.tvHoraMovimiento);
            cardIcon = itemView.findViewById(R.id.cardIconTimeline);
            ivIcon = itemView.findViewById(R.id.ivMovimientoIcon);
        }
    }
}
