package com.example.adisan_app_android;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Chips de filtro por categoría ("Todos" + una por cada categoría registrada en la base).
 * Se generan desde los datos para que una categoría nueva aparezca sin tocar el layout.
 */
public class FiltroCategorias {

    public interface Listener {
        void onCambio(@Nullable Categoria categoria);
    }

    private static final int COLOR_ACTIVO = Color.parseColor("#F97316");
    private static final int COLOR_INACTIVO = Color.parseColor("#101725");
    private static final int TEXTO_INACTIVO = Color.parseColor("#94A3B8");
    private static final int BORDE_INACTIVO = Color.parseColor("#1E293B");

    private final LinearLayout contenedor;
    private final Listener listener;
    private final List<MaterialButton> chips = new ArrayList<>();
    private Categoria seleccionada;

    public FiltroCategorias(@NonNull LinearLayout contenedor, @NonNull Listener listener) {
        this.contenedor = contenedor;
        this.listener = listener;
    }

    public void cargar() {
        construir(new ArrayList<>());
        ApiClient.getApiService().getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(@NonNull Call<List<Categoria>> call, @NonNull Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) construir(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Categoria>> call, @NonNull Throwable t) {
            }
        });
    }

    @Nullable
    public Categoria getSeleccionada() {
        return seleccionada;
    }

    public boolean coincide(int categoriaId) {
        return seleccionada == null || seleccionada.getId() == categoriaId;
    }

    public boolean coincide(@Nullable String nombreCategoria) {
        return seleccionada == null || seleccionada.getNombre().equalsIgnoreCase(nombreCategoria != null ? nombreCategoria.trim() : "");
    }

    private void construir(List<Categoria> categorias) {
        contenedor.removeAllViews();
        chips.clear();
        agregarChip("Todos", null);
        for (Categoria c : categorias) agregarChip(c.getNombre(), c);

        // Conservar la selección si la categoría sigue existiendo
        Categoria previa = seleccionada;
        seleccionada = null;
        for (int i = 0; i < chips.size(); i++) {
            Categoria c = (Categoria) chips.get(i).getTag();
            if (previa != null && c != null && c.getId() == previa.getId()) seleccionada = c;
        }
        pintar();
    }

    private void agregarChip(String texto, @Nullable Categoria categoria) {
        Context ctx = contenedor.getContext();
        MaterialButton chip = new MaterialButton(ctx, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(ctx, 38));
        lp.setMarginEnd(dp(ctx, 8));
        lp.topMargin = dp(ctx, 6);
        lp.bottomMargin = dp(ctx, 4);
        chip.setLayoutParams(lp);
        chip.setInsetTop(0);
        chip.setInsetBottom(0);
        chip.setMinWidth(dp(ctx, 72));
        chip.setPadding(dp(ctx, 18), 0, dp(ctx, 18), 0);
        chip.setCornerRadius(dp(ctx, 18));
        chip.setStrokeWidth(dp(ctx, 1));
        chip.setAllCaps(false);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        chip.setText(texto);
        chip.setTag(categoria);
        chip.setOnClickListener(v -> {
            seleccionada = categoria;
            pintar();
            listener.onCambio(categoria);
        });
        contenedor.addView(chip);
        chips.add(chip);
    }

    private void pintar() {
        for (MaterialButton chip : chips) {
            boolean activo = chip.getTag() == seleccionada;
            chip.setBackgroundTintList(ColorStateList.valueOf(activo ? COLOR_ACTIVO : COLOR_INACTIVO));
            chip.setStrokeColor(ColorStateList.valueOf(activo ? COLOR_ACTIVO : BORDE_INACTIVO));
            chip.setTextColor(activo ? Color.WHITE : TEXTO_INACTIVO);
            chip.setTypeface(null, activo ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private static int dp(Context ctx, int valor) {
        return Math.round(valor * ctx.getResources().getDisplayMetrics().density);
    }
}
