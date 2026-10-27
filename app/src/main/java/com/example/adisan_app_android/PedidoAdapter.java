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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    public interface OnPedidoActionListener {
        void onVerDetalle(Pedido pedido);
        void onEditarEstado(Pedido pedido);
        void onCancelar(Pedido pedido);
    }

    private final Context context;
    private List<Pedido> pedidoList;
    private OnPedidoActionListener listener;

    public PedidoAdapter(Context context, List<Pedido> pedidoList) {
        this.context = context;
        this.pedidoList = pedidoList != null ? new ArrayList<>(pedidoList) : new ArrayList<>();
    }

    public void setOnPedidoActionListener(OnPedidoActionListener listener) {
        this.listener = listener;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidoList = pedidos != null ? new ArrayList<>(pedidos) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidoList.get(position);
        if (pedido == null) return;

        holder.tvId.setText("Pedido #" + pedido.getId());
        holder.tvFecha.setText(!TextUtils.isEmpty(pedido.getFecha()) ? pedido.getFecha() : "Fecha pendiente");
        holder.tvMetodoPago.setText(pedido.getMetodoPago());
        holder.tvTotal.setText(String.format(Locale.US, "S/ %.2f", pedido.getTotalDouble()));

        // Cliente vs Venta Directa
        if (pedido.getClienteId() == null || pedido.getClienteId() == 0 || TextUtils.isEmpty(pedido.getCliente()) || "Venta Mostrador".equalsIgnoreCase(pedido.getCliente())) {
            holder.tvCliente.setText("Venta directa");
            if (holder.ivIconoCliente != null) holder.ivIconoCliente.setImageResource(R.drawable.ic_box);
        } else {
            holder.tvCliente.setText(pedido.getCliente());
            if (holder.ivIconoCliente != null) holder.ivIconoCliente.setImageResource(R.drawable.ic_user);
        }

        // Resumen de items / unidades
        int prodCount = 0;
        int totalUnid = 0;
        if (pedido.getDetalles() != null) {
            prodCount = pedido.getDetalles().size();
            for (DetallePedido det : pedido.getDetalles()) {
                // Un paquete cuenta todas sus unidades, no 1
                totalUnid += det.getUnidadesTotales();
            }
        }
        holder.tvResumenItems.setText("Productos: " + prodCount + " • Unidades: " + totalUnid);

        String estado = pedido.getEstado() != null ? pedido.getEstado().toLowerCase() : "pendiente";
        if (estado.isEmpty()) {
            estado = "pendiente";
        }
        holder.tvEstado.setText(estado.substring(0, 1).toUpperCase() + estado.substring(1).replace("_", " "));

        switch (estado) {
            case "aceptado":
            case "aprobado":
                holder.tvEstado.setTextColor(Color.parseColor("#3B82F6"));
                holder.tvEstado.setBackgroundColor(Color.parseColor("#0D2247"));
                break;
            case "en_camino":
            case "en camino":
                holder.tvEstado.setTextColor(Color.parseColor("#3B82F6"));
                holder.tvEstado.setBackgroundColor(Color.parseColor("#0D2247"));
                break;
            case "entregado":
            case "completado":
                holder.tvEstado.setTextColor(Color.parseColor("#10B981"));
                holder.tvEstado.setBackgroundColor(Color.parseColor("#062E29"));
                break;
            case "cancelado":
            case "rechazado":
                holder.tvEstado.setTextColor(Color.parseColor("#EF4444"));
                holder.tvEstado.setBackgroundColor(Color.parseColor("#3B1111"));
                break;
            default: // pendiente
                holder.tvEstado.setTextColor(Color.parseColor("#F97316"));
                holder.tvEstado.setBackgroundColor(Color.parseColor("#3B2C0D"));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVerDetalle(pedido);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pedidoList != null ? pedidoList.size() : 0;
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvId;
        TextView tvFecha;
        TextView tvEstado;
        TextView tvCliente;
        TextView tvResumenItems;
        TextView tvMetodoPago;
        TextView tvTotal;
        ImageView ivIconoCliente;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvIdPedido);
            tvFecha = itemView.findViewById(R.id.tvFechaPedido);
            tvEstado = itemView.findViewById(R.id.tvEstadoPedido);
            tvCliente = itemView.findViewById(R.id.tvClientePedido);
            tvResumenItems = itemView.findViewById(R.id.tvResumenItemsPedido);
            tvMetodoPago = itemView.findViewById(R.id.tvMetodoPagoPedido);
            tvTotal = itemView.findViewById(R.id.tvTotalPedido);
            ivIconoCliente = itemView.findViewById(R.id.ivIconoClientePedido);
        }
    }
}
