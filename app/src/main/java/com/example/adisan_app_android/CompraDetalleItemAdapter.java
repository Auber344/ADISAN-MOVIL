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

public class CompraDetalleItemAdapter extends RecyclerView.Adapter<CompraDetalleItemAdapter.DetalleItemViewHolder> {

    private final Context context;
    private List<DetalleCompra> detalleList;

    public CompraDetalleItemAdapter(Context context, List<DetalleCompra> detalleList) {
        this.context = context;
        this.detalleList = detalleList != null ? new ArrayList<>(detalleList) : new ArrayList<>();
    }

    public void setDetalleList(List<DetalleCompra> detalleList) {
        this.detalleList = detalleList != null ? new ArrayList<>(detalleList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DetalleItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_detalle_compra_card, parent, false);
        return new DetalleItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetalleItemViewHolder holder, int position) {
        DetalleCompra det = detalleList.get(position);
        if (det == null) return;

        holder.tvNombre.setText(!TextUtils.isEmpty(det.getProductoNombre()) ? det.getProductoNombre() : "Producto #" + det.getProductoId());
        holder.tvPaquetes.setText(String.valueOf(det.getCantidadPaquetes()));
        holder.tvUnidades.setText(String.valueOf(det.getCantidadUnidades()));

        double precioPaq = 0.0;
        try { precioPaq = Double.parseDouble(det.getPrecioUnitario()); } catch (Exception ignored) {}
        double subtotal = 0.0;
        try { subtotal = Double.parseDouble(det.getSubtotal()); } catch (Exception ignored) {}
        if (subtotal == 0.0) {
            subtotal = det.getCantidadPaquetes() * precioPaq;
        }

        holder.tvPrecioPaq.setText(String.format(Locale.US, "S/ %.2f", precioPaq));
        holder.tvSubtotal.setText(String.format(Locale.US, "S/ %.2f", subtotal));
    }

    @Override
    public int getItemCount() {
        return detalleList != null ? detalleList.size() : 0;
    }

    public static class DetalleItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvCategoria;
        TextView tvPaquetes;
        TextView tvUnidades;
        TextView tvPrecioPaq;
        TextView tvSubtotal;

        public DetalleItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvDetalleItemNombre);
            tvCategoria = itemView.findViewById(R.id.tvDetalleItemCategoria);
            tvPaquetes = itemView.findViewById(R.id.tvDetalleItemPaquetes);
            tvUnidades = itemView.findViewById(R.id.tvDetalleItemUnidades);
            tvPrecioPaq = itemView.findViewById(R.id.tvDetalleItemPrecioPaq);
            tvSubtotal = itemView.findViewById(R.id.tvDetalleItemSubtotal);
        }
    }
}
