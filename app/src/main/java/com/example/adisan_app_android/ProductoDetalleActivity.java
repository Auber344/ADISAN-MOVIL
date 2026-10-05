package com.example.adisan_app_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductoDetalleActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private TextView tvNombreHeader;
    private TextView tvEstadoHeader;
    private TextView tvCategoriaHeader;

    private TextView tvInfoNombre;
    private TextView tvInfoCategoria;
    private TextView tvInfoProveedor;
    private TextView tvInfoUnidadesPaq;
    private TextView tvInfoEstado;

    private TextView tvPrecioCompraPaq;
    private TextView tvPrecioVentaPaq;
    private TextView tvPrecioVentaUnid;

    private MaterialButton btnEditar;
    private MaterialButton btnEliminar;

    private Producto producto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_detalle);

        String productoJson = getIntent().getStringExtra("producto_json");
        if (productoJson != null) {
            try {
                producto = new Gson().fromJson(productoJson, Producto.class);
            } catch (Exception ignored) {}
        }

        if (producto == null) {
            Toast.makeText(this, "Error al cargar producto", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        btnVolver = findViewById(R.id.btnVolverDetalle);
        tvNombreHeader = findViewById(R.id.tvDetalleNombre);
        tvEstadoHeader = findViewById(R.id.tvDetalleEstado);
        tvCategoriaHeader = findViewById(R.id.tvDetalleCategoriaBadge);

        tvInfoNombre = findViewById(R.id.tvInfoNombre);
        tvInfoCategoria = findViewById(R.id.tvInfoCategoria);
        tvInfoProveedor = findViewById(R.id.tvInfoProveedor);
        tvInfoUnidadesPaq = findViewById(R.id.tvInfoUnidadesPaq);
        tvInfoEstado = findViewById(R.id.tvInfoEstado);

        tvPrecioCompraPaq = findViewById(R.id.tvPrecioCompraPaqDetalle);
        tvPrecioVentaPaq = findViewById(R.id.tvPrecioVentaPaqDetalle);
        tvPrecioVentaUnid = findViewById(R.id.tvPrecioVentaUnidDetalle);

        btnEditar = findViewById(R.id.btnEditarDetalle);
        btnEliminar = findViewById(R.id.btnEliminarDetalle);
    }

    private void bindData() {
        if (producto == null) return;

        if (tvNombreHeader != null) tvNombreHeader.setText(producto.getProductoNombre());
        if (tvInfoNombre != null) tvInfoNombre.setText(producto.getProductoNombre());
        if (tvInfoCategoria != null) tvInfoCategoria.setText("Categoría #" + producto.getCategoriaId());
        if (tvCategoriaHeader != null) tvCategoriaHeader.setText("Categoría #" + producto.getCategoriaId());
        if (tvInfoProveedor != null) tvInfoProveedor.setText("Proveedor #" + producto.getProveedorId());
        if (tvInfoUnidadesPaq != null) tvInfoUnidadesPaq.setText(producto.getUnidadesPorPaqueteVal() + " unidades");

        boolean activo = producto.getActivo() == 1;
        if (tvEstadoHeader != null) {
            tvEstadoHeader.setText(activo ? "Activo" : "Inactivo");
            tvEstadoHeader.setTextColor(Color.parseColor(activo ? "#10B981" : "#EF4444"));
        }
        if (tvInfoEstado != null) {
            tvInfoEstado.setText(activo ? "Activo" : "Inactivo");
            tvInfoEstado.setTextColor(Color.parseColor(activo ? "#10B981" : "#EF4444"));
        }

        double pCompraPaq = 0.0;
        double pVentaPaq = 0.0;
        double pVentaUnid = 0.0;
        try { pCompraPaq = Double.parseDouble(producto.getPrecioCompraPaquete()); } catch (Exception ignored) {}
        try { pVentaPaq = Double.parseDouble(producto.getPrecioVentaPaquete()); } catch (Exception ignored) {}
        try { pVentaUnid = Double.parseDouble(producto.getPrecioVentaUnidad()); } catch (Exception ignored) {}

        if (tvPrecioCompraPaq != null) tvPrecioCompraPaq.setText(String.format(Locale.US, "S/ %.2f", pCompraPaq));
        if (tvPrecioVentaPaq != null) tvPrecioVentaPaq.setText(String.format(Locale.US, "S/ %.2f", pVentaPaq));
        if (tvPrecioVentaUnid != null) tvPrecioVentaUnid.setText(String.format(Locale.US, "S/ %.2f", pVentaUnid));
    }

    private void setupListeners() {
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnEditar != null) {
            btnEditar.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.putExtra("editar_producto_id", producto.getProductoId());
                setResult(RESULT_OK, intent);
                finish();
            });
        }

        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> mostrarDialogoConfirmarEliminar());
        }
    }

    private void mostrarDialogoConfirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Deseas eliminar el producto \"" + producto.getProductoNombre() + "\"?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> ejecutarEliminar())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarEliminar() {
        if (btnEliminar != null) btnEliminar.setEnabled(false);

        ApiClient.getApiService().eliminarProducto(producto.getProductoId()).enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductoResponse> call, @NonNull Response<ProductoResponse> response) {
                if (btnEliminar != null) btnEliminar.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ProductoDetalleActivity.this, "¡Producto eliminado correctamente!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ProductoDetalleActivity.this, "Error al eliminar producto (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductoResponse> call, @NonNull Throwable t) {
                if (btnEliminar != null) btnEliminar.setEnabled(true);
                Toast.makeText(ProductoDetalleActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
