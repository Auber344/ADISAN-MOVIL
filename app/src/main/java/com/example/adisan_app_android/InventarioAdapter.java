package com.example.adisan_app_android;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InventarioAdapter extends RecyclerView.Adapter<InventarioAdapter.InventarioViewHolder> {

    public interface OnInventarioActionListener {
        void onAjustarStock(Inventario inventario);
        void onVerMovimientos(Inventario inventario);
    }

    private final Context context;
    private List<Inventario> inventarioList;
    private OnInventarioActionListener listener;

    public InventarioAdapter(Context context, List<Inventario> inventarioList) {
        this.context = context;
        this.inventarioList = inventarioList != null ? new ArrayList<>(inventarioList) : new ArrayList<>();
    }

    public void setOnInventarioActionListener(OnInventarioActionListener listener) {
        this.listener = listener;
    }

    public void setInventarioList(List<Inventario> inventarioList) {
        this.inventarioList = inventarioList != null ? new ArrayList<>(inventarioList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventario, parent, false);
        return new InventarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventarioViewHolder holder, int position) {
        Inventario inv = inventarioList.get(position);
        if (inv == null) return;

        holder.tvNombre.setText(inv.getProductoNombre());

        int stock = inv.getStockUnidades();
        holder.tvStockUnidades.setText(stock + " un.");

        int paq = inv.getPaquetesDisponibles();
        int sueltas = inv.getUnidadesSueltas();
        holder.tvDesglose.setText(paq + " paq. + " + sueltas + " un.");

        if (stock == 0) {
            holder.tvEstadoStock.setText("Sin stock");
            holder.tvEstadoStock.setTextColor(Color.parseColor("#EF4444"));
            holder.tvEstadoStock.setBackgroundColor(Color.parseColor("#3B1111"));
        } else if (stock <= 10) {
            holder.tvEstadoStock.setText("Stock bajo");
            holder.tvEstadoStock.setTextColor(Color.parseColor("#F59E0B"));
            holder.tvEstadoStock.setBackgroundColor(Color.parseColor("#3B2C0D"));
        } else {
            holder.tvEstadoStock.setText("Stock suficiente");
            holder.tvEstadoStock.setTextColor(Color.parseColor("#10B981"));
            holder.tvEstadoStock.setBackgroundColor(Color.parseColor("#062E29"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAjustarStock(inv);
            }
        });

        if (holder.btnAjustar != null) {
            holder.btnAjustar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAjustarStock(inv);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return inventarioList != null ? inventarioList.size() : 0;
    }

    public static class InventarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvCategoria;
        TextView tvStockUnidades;
        TextView tvDesglose;
        TextView tvEstadoStock;
        ImageView btnAjustar;

        public InventarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreProductoInventario);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaInventario);
            tvStockUnidades = itemView.findViewById(R.id.tvStockUnidades);
            tvDesglose = itemView.findViewById(R.id.tvDesglosePaquetes);
            tvEstadoStock = itemView.findViewById(R.id.tvEstadoStock);
            btnAjustar = itemView.findViewById(R.id.btnAjustarStock);
        }
    }
}
