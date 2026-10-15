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

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovimientoFinancieroAdapter extends RecyclerView.Adapter<MovimientoFinancieroAdapter.ViewHolder> {

    private final Context context;
    private List<MovimientoFinanciero> movimientoList;

    public MovimientoFinancieroAdapter(Context context, List<MovimientoFinanciero> movimientoList) {
        this.context = context;
        this.movimientoList = movimientoList != null ? new ArrayList<>(movimientoList) : new ArrayList<>();
    }

    public void setMovimientos(List<MovimientoFinanciero> movimientos) {
        this.movimientoList = movimientos != null ? new ArrayList<>(movimientos) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movimiento_financiero, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovimientoFinanciero mov = movimientoList.get(position);
        if (mov == null) return;

        if (holder.tvTitle != null) holder.tvTitle.setText(mov.getTitulo());
        if (holder.tvSubtitle != null) holder.tvSubtitle.setText(mov.getSubtitulo());
        if (holder.tvDate != null) holder.tvDate.setText(mov.getFecha());

        double monto = mov.getMonto();
        boolean esEntrada = mov.isEsEntrada();

        String strMonto = String.format(Locale.US, "%sS/ %.2f", (esEntrada ? "" : "- "), Math.abs(monto));
        if (holder.tvAmount != null) {
            holder.tvAmount.setText(strMonto);
            holder.tvAmount.setTextColor(Color.parseColor(esEntrada ? "#10B981" : "#EF4444"));
        }

        MovimientoFinanciero.TipoMovimientoFinanciero tipo = mov.getTipo();

        int iconRes = R.drawable.ic_shopping_cart;
        int iconBg = Color.parseColor("#062E29");
        int iconTint = Color.parseColor("#10B981");
        String badgeText = "Venta";
        int badgeBg = Color.parseColor("#062E29");
        int badgeTextCol = Color.parseColor("#10B981");

        if (tipo == MovimientoFinanciero.TipoMovimientoFinanciero.COMPRA) {
            iconRes = R.drawable.ic_truck;
            iconBg = Color.parseColor("#3B1111");
            iconTint = Color.parseColor("#EF4444");
            badgeText = "Compra";
            badgeBg = Color.parseColor("#3B1111");
            badgeTextCol = Color.parseColor("#EF4444");
        } else if (tipo == MovimientoFinanciero.TipoMovimientoFinanciero.GASTO) {
            iconRes = R.drawable.ic_dollar;
            iconBg = Color.parseColor("#3B2C0D");
            iconTint = Color.parseColor("#F59E0B");
            badgeText = "Gasto";
            badgeBg = Color.parseColor("#3B2C0D");
            badgeTextCol = Color.parseColor("#F59E0B");
        } else if (tipo == MovimientoFinanciero.TipoMovimientoFinanciero.COBRO) {
            iconRes = R.drawable.ic_shopping_cart;
            iconBg = Color.parseColor("#062E29");
            iconTint = Color.parseColor("#10B981");
            badgeText = "Cobro";
            badgeBg = Color.parseColor("#062E29");
            badgeTextCol = Color.parseColor("#10B981");
        } else if (tipo == MovimientoFinanciero.TipoMovimientoFinanciero.PAGO) {
            iconRes = R.drawable.ic_box;
            iconBg = Color.parseColor("#2D124D");
            iconTint = Color.parseColor("#A855F7");
            badgeText = "Pago";
            badgeBg = Color.parseColor("#2D124D");
            badgeTextCol = Color.parseColor("#A855F7");
        }

        if (holder.cardIcon != null) holder.cardIcon.setCardBackgroundColor(iconBg);
        if (holder.ivIcon != null) {
            holder.ivIcon.setImageResource(iconRes);
            holder.ivIcon.setColorFilter(iconTint);
        }

        if (holder.tvBadge != null) {
            holder.tvBadge.setText(badgeText);
            holder.tvBadge.setTextColor(badgeTextCol);
            holder.tvBadge.setBackgroundColor(badgeBg);
        }
    }

    @Override
    public int getItemCount() {
        return movimientoList != null ? movimientoList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSubtitle;
        TextView tvDate;
        TextView tvAmount;
        TextView tvBadge;
        MaterialCardView cardIcon;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitleMovFin);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitleMovFin);
            tvDate = itemView.findViewById(R.id.tvDateMovFin);
            tvAmount = itemView.findViewById(R.id.tvAmountMovFin);
            tvBadge = itemView.findViewById(R.id.tvBadgeMovFin);
            cardIcon = itemView.findViewById(R.id.cardIconMovFin);
            ivIcon = itemView.findViewById(R.id.ivIconMovFin);
        }
    }
}
