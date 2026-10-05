package com.example.adisan_app_android;

import android.content.Context;
import android.graphics.Color;
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

public class GestionOfertaAdapter extends RecyclerView.Adapter<GestionOfertaAdapter.GestionOfertaViewHolder> {

    public interface OnGestionOfertaActionListener {
        void onEditar(Oferta oferta);
        void onEliminar(Oferta oferta);
    }

    private final Context context;
    private List<Oferta> ofertaList;
    private OnGestionOfertaActionListener listener;

    public GestionOfertaAdapter(Context context, List<Oferta> ofertaList) {
        this.context = context;
        this.ofertaList = ofertaList != null ? new ArrayList<>(ofertaList) : new ArrayList<>();
    }

    public void setOnGestionOfertaActionListener(OnGestionOfertaActionListener listener) {
        this.listener = listener;
    }

    public void setOfertaList(List<Oferta> ofertaList) {
        this.ofertaList = ofertaList != null ? new ArrayList<>(ofertaList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GestionOfertaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gestion_oferta, parent, false);
        return new GestionOfertaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GestionOfertaViewHolder holder, int position) {
        Oferta oferta = ofertaList.get(position);

        holder.tvNombre.setText(oferta.getNombre());
        if (oferta.getActivo() == 1) {
            holder.tvEstado.setText("Activa");
            holder.tvEstado.setTextColor(Color.parseColor("#059669"));
        } else {
            holder.tvEstado.setText("Inactiva");
            holder.tvEstado.setTextColor(Color.parseColor("#DC2626"));
        }

        int count = oferta.getItems() != null ? oferta.getItems().size() : 0;
        holder.tvComponentesCount.setText(count + " componente(s)");

        holder.tvPrecioNormal.setText(String.format(Locale.US, "S/ %.2f", oferta.getPrecioNormalDouble()));
        holder.tvPrecioFinal.setText(String.format(Locale.US, "S/ %.2f", oferta.getPrecioFinalDouble()));

        if (holder.btnEditar != null) {
            holder.btnEditar.setOnClickListener(v -> {
                if (listener != null) listener.onEditar(oferta);
            });
        }

        if (holder.btnEliminar != null) {
            holder.btnEliminar.setOnClickListener(v -> {
                if (listener != null) listener.onEliminar(oferta);
            });
        }
    }

    @Override
    public int getItemCount() {
        return ofertaList != null ? ofertaList.size() : 0;
    }

    public static class GestionOfertaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvEstado;
        TextView tvComponentesCount;
        TextView tvPrecioNormal;
        TextView tvPrecioFinal;
        MaterialButton btnEditar;
        MaterialButton btnEliminar;

        public GestionOfertaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreGestionOferta);
            tvEstado = itemView.findViewById(R.id.tvEstadoGestionOferta);
            tvComponentesCount = itemView.findViewById(R.id.tvInfoComponentesCount);
            tvPrecioNormal = itemView.findViewById(R.id.tvPrecioNormalGestion);
            tvPrecioFinal = itemView.findViewById(R.id.tvPrecioFinalGestion);
            btnEditar = itemView.findViewById(R.id.btnEditarOfertaAdmin);
            btnEliminar = itemView.findViewById(R.id.btnEliminarOfertaAdmin);
        }
    }
}
