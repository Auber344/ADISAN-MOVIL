package com.example.adisan_app_android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CompraItemFormAdapter extends RecyclerView.Adapter<CompraItemFormAdapter.ItemViewHolder> {

    public interface OnItemRemovedListener {
        void onItemRemoved(int position);
    }

    private final Context context;
    private List<CompraRequest.CompraItemRequest> itemList;
    private List<String> productoNombreList;
    private List<Integer> unidadesPorPaqueteList;
    private OnItemRemovedListener listener;

    public CompraItemFormAdapter(Context context, List<CompraRequest.CompraItemRequest> itemList, List<String> productoNombreList, List<Integer> unidadesPorPaqueteList) {
        this.context = context;
        this.itemList = itemList != null ? itemList : new ArrayList<>();
        this.productoNombreList = productoNombreList != null ? productoNombreList : new ArrayList<>();
        this.unidadesPorPaqueteList = unidadesPorPaqueteList != null ? unidadesPorPaqueteList : new ArrayList<>();
    }

    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_compra_detalle_form, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        CompraRequest.CompraItemRequest item = itemList.get(position);
        String nombreProd = position < productoNombreList.size() ? productoNombreList.get(position) : "Producto #" + item.getProductoId();
        int upp = position < unidadesPorPaqueteList.size() ? unidadesPorPaqueteList.get(position) : 1;

        holder.tvNombre.setText(nombreProd);
        holder.tvInfo.setText(item.getCantidadPaquetes() + " Paquetes (" + upp + " un./paq) • S/ " + item.getPrecioUnitario() + " /paq");

        if (holder.btnEliminar != null) {
            holder.btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvInfo;
        ImageButton btnEliminar;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvItemProductoNombre);
            tvInfo = itemView.findViewById(R.id.tvItemDetalleInfo);
            btnEliminar = itemView.findViewById(R.id.btnEliminarItemCompra);
        }
    }
}
