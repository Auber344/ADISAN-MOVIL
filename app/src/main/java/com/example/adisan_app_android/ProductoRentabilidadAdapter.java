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

public class ProductoRentabilidadAdapter extends RecyclerView.Adapter<ProductoRentabilidadAdapter.ViewHolder> {

    public interface OnProductoRentabilidadClickListener {
        void onProductoClick(ProductoRentabilidad producto);
    }

    private final Context context;
    private List<ProductoRentabilidad> productoList;
    private OnProductoRentabilidadClickListener listener;

    public ProductoRentabilidadAdapter(Context context, List<ProductoRentabilidad> productoList) {
        this.context = context;
        this.productoList = productoList != null ? new ArrayList<>(productoList) : new ArrayList<>();
    }

    public void setOnProductoRentabilidadClickListener(OnProductoRentabilidadClickListener listener) {
        this.listener = listener;
    }

    public void setProductos(List<ProductoRentabilidad> productos) {
        this.productoList = productos != null ? new ArrayList<>(productos) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto_rentabilidad, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductoRentabilidad item = productoList.get(position);
        if (item == null) return;

        if (holder.tvNombre != null) holder.tvNombre.setText(item.getNombre());
        if (holder.tvUnidades != null) {
            holder.tvUnidades.setText(item.getUnidadesVendidas() + (item.getUnidadesVendidas() == 1 ? " unidad vendida" : " unidades vendidas"));
        }
        if (holder.tvMonto != null) {
            holder.tvMonto.setText(String.format(Locale.US, "S/ %.2f", item.getMontoVentas()));
        }
        if (holder.tvMargenBadge != null) {
            holder.tvMargenBadge.setText(item.getPorcentajeMargen() + "%");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductoClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productoList != null ? productoList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvUnidades;
        TextView tvMonto;
        TextView tvMargenBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreProdRent);
            tvUnidades = itemView.findViewById(R.id.tvUnidadesProdRent);
            tvMonto = itemView.findViewById(R.id.tvMontoProdRent);
            tvMargenBadge = itemView.findViewById(R.id.tvMargenBadgeProdRent);
        }
    }
}
