package com.example.adisan_app_android;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompraAdapter extends RecyclerView.Adapter<CompraAdapter.CompraViewHolder> {

    public interface OnCompraActionListener {
        void onVerDetalle(Compra compra);
    }

    private final Context context;
    private List<Compra> compraList;
    private OnCompraActionListener listener;

    public CompraAdapter(Context context, List<Compra> compraList) {
        this.context = context;
        this.compraList = compraList != null ? new ArrayList<>(compraList) : new ArrayList<>();
    }

    public void setOnCompraActionListener(OnCompraActionListener listener) {
        this.listener = listener;
    }

    public void setCompraList(List<Compra> compraList) {
        this.compraList = compraList != null ? new ArrayList<>(compraList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CompraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_compra, parent, false);
        return new CompraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompraViewHolder holder, int position) {
        Compra compra = compraList.get(position);
        if (compra == null) return;

        holder.tvProveedor.setText(!TextUtils.isEmpty(compra.getProveedorNombre()) ? compra.getProveedorNombre() : "Proveedor #" + compra.getProveedorId());
        holder.tvFecha.setText(!TextUtils.isEmpty(compra.getFecha()) ? compra.getFecha() : "Fecha pendiente");

        double totalNum = 0.0;
        try {
            totalNum = Double.parseDouble(compra.getTotal());
        } catch (Exception ignored) {}
        holder.tvTotal.setText(String.format(Locale.US, "S/ %.2f", totalNum));

        StringBuilder sb = new StringBuilder();
        if (compra.getDetalles() != null && !compra.getDetalles().isEmpty()) {
            for (DetalleCompra det : compra.getDetalles()) {
                sb.append(det.getProductoNombre()).append("\n")
                        .append(det.getCantidadPaquetes()).append(" paquetes · ").append(det.getCantidadUnidades()).append(" unidades\n")
                        .append("S/ ").append(det.getPrecioUnitario()).append(" por paquete\n\n");
            }
        } else {
            sb.append("Sin detalles registrados");
        }
        holder.tvDetalleSummary.setText(sb.toString().trim());

        if (holder.btnVerDetalle != null) {
            holder.btnVerDetalle.setOnClickListener(v -> {
                if (listener != null) listener.onVerDetalle(compra);
            });
        }
    }

    @Override
    public int getItemCount() {
        return compraList != null ? compraList.size() : 0;
    }

    public static class CompraViewHolder extends RecyclerView.ViewHolder {
        TextView tvProveedor;
        TextView tvFecha;
        TextView tvEstado;
        TextView tvTotal;
        TextView tvDetalleSummary;
        TextView btnVerDetalle;

        public CompraViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProveedor = itemView.findViewById(R.id.tvProveedorCompra);
            tvFecha = itemView.findViewById(R.id.tvFechaCompra);
            tvEstado = itemView.findViewById(R.id.tvEstadoCompra);
            tvTotal = itemView.findViewById(R.id.tvTotalCompra);
            tvDetalleSummary = itemView.findViewById(R.id.tvDetalleCompraSummary);
            btnVerDetalle = itemView.findViewById(R.id.btnVerDetalleCompra);
        }
    }
}
