package com.example.adisan_app_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.Locale;

public class CompraDetalleActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private TextView tvIdCompra;
    private TextView tvFechaCompra;
    private TextView tvEstadoCompraHeader;

    private TextView tvProveedor;
    private TextView tvTotalGeneral;
    private TextView tvEstadoInfo;
    private TextView tvObservacion;
    private TextView tvTotalFinal;

    private RecyclerView rvItems;
    private CompraDetalleItemAdapter itemAdapter;

    private MaterialButton btnImprimir;
    private MaterialButton btnEliminar;

    private Compra compra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compra_detalle);

        String compraJson = getIntent().getStringExtra("compra_json");
        if (compraJson != null) {
            try {
                compra = new Gson().fromJson(compraJson, Compra.class);
            } catch (Exception ignored) {}
        }

        if (compra == null) {
            Toast.makeText(this, "Error al cargar la compra", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        bindData();
        setupListeners();
    }

    private void initViews() {
        btnVolver = findViewById(R.id.btnVolverDetalleCompra);
        tvIdCompra = findViewById(R.id.tvDetalleIdCompra);
        tvFechaCompra = findViewById(R.id.tvDetalleFechaCompra);
        tvEstadoCompraHeader = findViewById(R.id.tvDetalleEstadoCompra);

        tvProveedor = findViewById(R.id.tvDetalleProveedor);
        tvTotalGeneral = findViewById(R.id.tvDetalleTotal);
        tvEstadoInfo = findViewById(R.id.tvDetalleEstadoInfo);
        tvObservacion = findViewById(R.id.tvDetalleObservacion);
        tvTotalFinal = findViewById(R.id.tvDetalleTotalFinal);

        rvItems = findViewById(R.id.rvDetalleCompraItems);
        btnImprimir = findViewById(R.id.btnImprimirCompra);
        btnEliminar = findViewById(R.id.btnEliminarCompra);
    }

    private void setupRecyclerView() {
        if (rvItems != null) {
            rvItems.setLayoutManager(new LinearLayoutManager(this));
            itemAdapter = new CompraDetalleItemAdapter(this, compra.getDetalles());
            rvItems.setAdapter(itemAdapter);
        }
    }

    private void bindData() {
        if (compra == null) return;

        if (tvIdCompra != null) tvIdCompra.setText("Compra #" + compra.getId());
        if (tvFechaCompra != null) tvFechaCompra.setText(!TextUtils.isEmpty(compra.getFecha()) ? compra.getFecha() : "Fecha pendiente");
        if (tvEstadoCompraHeader != null) tvEstadoCompraHeader.setText("Completada");

        if (tvProveedor != null) tvProveedor.setText(!TextUtils.isEmpty(compra.getProveedorNombre()) ? compra.getProveedorNombre() : "Proveedor #" + compra.getProveedorId());

        double totalNum = 0.0;
        try {
            totalNum = Double.parseDouble(compra.getTotal());
        } catch (Exception ignored) {}

        String totalStr = String.format(Locale.US, "S/ %.2f", totalNum);
        if (tvTotalGeneral != null) tvTotalGeneral.setText(totalStr);
        if (tvTotalFinal != null) tvTotalFinal.setText(totalStr);
        if (tvEstadoInfo != null) tvEstadoInfo.setText("Completada");
        if (tvObservacion != null) tvObservacion.setText("-");
    }

    private void setupListeners() {
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnImprimir != null) {
            btnImprimir.setOnClickListener(v -> Toast.makeText(this, "Funcionalidad de impresión (Próximamente)", Toast.LENGTH_SHORT).show());
        }

        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> Toast.makeText(this, "La eliminación de compras no está permitida por seguridad de inventario.", Toast.LENGTH_LONG).show());
        }
    }
}
