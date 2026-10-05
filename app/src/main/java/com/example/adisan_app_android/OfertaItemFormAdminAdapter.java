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

public class OfertaItemFormAdminAdapter extends RecyclerView.Adapter<OfertaItemFormAdminAdapter.AdminItemViewHolder> {

    public interface OnAdminItemRemovedListener {
        void onItemRemoved(int position);
    }

    private final Context context;
    private List<OfertaItemRequest> itemList;
    private List<String> productoNombreList;
    private OnAdminItemRemovedListener listener;

    public OfertaItemFormAdminAdapter(Context context, List<OfertaItemRequest> itemList, List<String> productoNombreList) {
        this.context = context;
        this.itemList = itemList != null ? itemList : new ArrayList<>();
        this.productoNombreList = productoNombreList != null ? productoNombreList : new ArrayList<>();
    }

    public void setOnAdminItemRemovedListener(OnAdminItemRemovedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_oferta_detalle_admin, parent, false);
        return new AdminItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminItemViewHolder holder, int position) {
        OfertaItemRequest item = itemList.get(position);
        String nombreProd = position < productoNombreList.size() ? productoNombreList.get(position) : "Producto #" + item.getProductId();

        holder.tvNombre.setText(nombreProd);
        holder.tvInfo.setText(item.getTipoVenta() + " • Paq: " + item.getCantidadPaquetes() + " | Unid: " + item.getCantidadUnidades());

        if (holder.btnEliminar != null) {
            holder.btnEliminar.setOnClickListener(v -> {
                if (listener != null) listener.onItemRemoved(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class AdminItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvInfo;
        ImageButton btnEliminar;

        public AdminItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvAdminItemProductoNombre);
            tvInfo = itemView.findViewById(R.id.tvAdminItemInfo);
            btnEliminar = itemView.findViewById(R.id.btnEliminarAdminItem);
        }
    }
}
