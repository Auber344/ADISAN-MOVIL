package com.example.adisan_app_android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OfertaAdapter extends RecyclerView.Adapter<OfertaAdapter.OfertaViewHolder> {

    public interface OnOfertaActionListener {
        void onAgregarAlCarrito(Oferta oferta);
    }

    private final Context context;
    private List<Oferta> ofertaList;
    private OnOfertaActionListener listener;

    public OfertaAdapter(Context context, List<Oferta> ofertaList) {
        this.context = context;
        this.ofertaList = ofertaList != null ? new ArrayList<>(ofertaList) : new ArrayList<>();
    }

    public void setOnOfertaActionListener(OnOfertaActionListener listener) {
        this.listener = listener;
    }

    public void setOfertaList(List<Oferta> ofertaList) {
        this.ofertaList = ofertaList != null ? new ArrayList<>(ofertaList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OfertaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_oferta, parent, false);
        return new OfertaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfertaViewHolder holder, int position) {
        Oferta oferta = ofertaList.get(position);
        if (oferta == null) return;

        holder.tvNombre.setText(oferta.getNombre());
        holder.tvPrecioNormal.setText(String.format(Locale.US, "S/ %.2f", oferta.getPrecioNormalDouble()));
        holder.tvPrecioNormal.setPaintFlags(holder.tvPrecioNormal.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        holder.tvPrecioOferta.setText(String.format(Locale.US, "S/ %.2f", oferta.getPrecioFinalDouble()));

        String desc = oferta.getDescuento();
        if (desc != null && !desc.isEmpty() && !desc.equals("0.00") && !desc.equals("0")) {
            holder.tvDescuento.setVisibility(View.VISIBLE);
            holder.tvDescuento.setText(desc + "% OFF");
        } else {
            holder.tvDescuento.setVisibility(View.GONE);
        }

        StringBuilder componentesStr = new StringBuilder();
        if (oferta.getItems() != null && !oferta.getItems().isEmpty()) {
            for (OfertaItem item : oferta.getItems()) {
                String tipoVenta = item.getTipoVenta();
                String cantidadDesc = "";
                if ("PAQUETE".equalsIgnoreCase(tipoVenta)) {
                    cantidadDesc = item.getCantidadPaquetes() + " paquetes";
                } else if ("UNIDAD".equalsIgnoreCase(tipoVenta)) {
                    cantidadDesc = item.getCantidadUnidades() + " unidades";
                } else {
                    cantidadDesc = item.getCantidadPaquetes() + " paq. + " + item.getCantidadUnidades() + " un.";
                }
                componentesStr.append("• ").append(item.getProductoNombre()).append(" — ").append(cantidadDesc).append("\n");
            }
        } else {
            componentesStr.append("Sin componentes especificados");
        }
        holder.tvComponentes.setText(componentesStr.toString().trim());

        if (holder.btnAgregar != null) {
            holder.btnAgregar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAgregarAlCarrito(oferta);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return ofertaList != null ? ofertaList.size() : 0;
    }

    public static class OfertaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvDescuento;
        TextView tvComponentes;
        TextView tvPrecioNormal;
        TextView tvPrecioOferta;
        MaterialButton btnAgregar;

        public OfertaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreOferta);
            tvDescuento = itemView.findViewById(R.id.tvDescuentoOferta);
            tvComponentes = itemView.findViewById(R.id.tvComponentesOferta);
            tvPrecioNormal = itemView.findViewById(R.id.tvPrecioNormalOferta);
            tvPrecioOferta = itemView.findViewById(R.id.tvPrecioOferta);
            btnAgregar = itemView.findViewById(R.id.btnAgregarOfertaCarrito);
        }
    }
}
