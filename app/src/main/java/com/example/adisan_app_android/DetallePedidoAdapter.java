package com.example.adisan_app_android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetallePedidoAdapter extends RecyclerView.Adapter<DetallePedidoAdapter.DetalleViewHolder> {

    private final Context context;
    private List<DetallePedido> detalleList;

    public DetallePedidoAdapter(Context context, List<DetallePedido> detalleList) {
        this.context = context;
        this.detalleList = detalleList != null ? detalleList : new ArrayList<>();
    }

    @NonNull
    @Override
    public DetalleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_detalle_pedido, parent, false);
        return new DetalleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetalleViewHolder holder, int position) {
        DetallePedido det = detalleList.get(position);

        holder.tvNombre.setText(det.getItemNombre());
        holder.tvModalidad.setText(det.getModalidadDescripcion());
        holder.tvSubtotal.setText(String.format(Locale.US, "S/ %.2f", det.getSubtotalDouble()));
    }

    @Override
    public int getItemCount() {
        return detalleList != null ? detalleList.size() : 0;
    }

    public static class DetalleViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvModalidad;
        TextView tvSubtotal;

        public DetalleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvDetalleItemNombre);
            tvModalidad = itemView.findViewById(R.id.tvDetalleItemModalidad);
            tvSubtotal = itemView.findViewById(R.id.tvDetalleItemSubtotal);
        }
    }
}
