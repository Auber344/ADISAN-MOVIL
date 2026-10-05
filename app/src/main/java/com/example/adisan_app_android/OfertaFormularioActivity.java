package com.example.adisan_app_android;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfertaFormularioActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private TextView tvTitle;
    private TextInputEditText etNombre;
    private Spinner spinnerProducto;
    private Spinner spinnerTipoVenta;
    private TextView tvRefInfo;
    private TextInputEditText etCantPaq;
    private TextInputEditText etCantUnid;
    private MaterialButton btnAgregarComponente;
    private RecyclerView rvComponentes;
    private TextView tvPrecioNormalPreview;
    private TextInputEditText etDescuento;
    private TextInputEditText etPrecioFinal;
    private MaterialButton btnGuardar;

    private List<Producto> productoList = new ArrayList<>();
    private List<OfertaItemRequest> componentRequests = new ArrayList<>();
    private List<String> componenteNombres = new ArrayList<>();
    private OfertaItemFormAdminAdapter adminAdapter;
    private Producto productoSeleccionado;

    private int ofertaIdEditando = -1;
    private double precioNormalCalculado = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oferta_formulario);

        ofertaIdEditando = getIntent().getIntExtra("oferta_id", -1);

        initViews();
        setupRecyclerView();
        setupSpinners();
        setupListeners();
        cargarProductos();

        if (ofertaIdEditando != -1) {
            if (tvTitle != null) tvTitle.setText("Editar Oferta #" + ofertaIdEditando);
            cargarDatosOfertaParaEditar(ofertaIdEditando);
        }
    }

    private void initViews() {
        btnVolver = findViewById(R.id.btnVolverFormOferta);
        tvTitle = findViewById(R.id.tvFormOfertaTitle);
        etNombre = findViewById(R.id.etNombreOfertaAdmin);
        spinnerProducto = findViewById(R.id.spinnerAdminProducto);
        spinnerTipoVenta = findViewById(R.id.spinnerAdminTipoVenta);
        tvRefInfo = findViewById(R.id.tvAdminProductoRefInfo);
        etCantPaq = findViewById(R.id.etAdminCantPaq);
        etCantUnid = findViewById(R.id.etAdminCantUnid);
        btnAgregarComponente = findViewById(R.id.btnAdminAgregarComponente);
        rvComponentes = findViewById(R.id.rvAdminComponentes);
        tvPrecioNormalPreview = findViewById(R.id.tvAdminPrecioNormalPreview);
        etDescuento = findViewById(R.id.etAdminDescuento);
        etPrecioFinal = findViewById(R.id.etAdminPrecioFinal);
        btnGuardar = findViewById(R.id.btnAdminGuardarOferta);
    }

    private void setupRecyclerView() {
        rvComponentes.setLayoutManager(new LinearLayoutManager(this));
        adminAdapter = new OfertaItemFormAdminAdapter(this, componentRequests, componenteNombres);
        rvComponentes.setAdapter(adminAdapter);

        adminAdapter.setOnAdminItemRemovedListener(position -> {
            componentRequests.remove(position);
            componenteNombres.remove(position);
            adminAdapter.notifyDataSetChanged();
            recalcularPrecioNormalPreview();
        });
    }

    private void setupSpinners() {
        List<String> tiposVenta = new ArrayList<>();
        tiposVenta.add("PAQUETE");
        tiposVenta.add("UNIDAD");
        tiposVenta.add("MIXTA");

        ArrayAdapter<String> tvAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposVenta);
        tvAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerTipoVenta != null) {
            spinnerTipoVenta.setAdapter(tvAdapter);
            spinnerTipoVenta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String tipo = tiposVenta.get(position);
                    if ("PAQUETE".equalsIgnoreCase(tipo)) {
                        if (etCantPaq != null) etCantPaq.setEnabled(true);
                        if (etCantUnid != null) {
                            etCantUnid.setText("0");
                            etCantUnid.setEnabled(false);
                        }
                    } else if ("UNIDAD".equalsIgnoreCase(tipo)) {
                        if (etCantPaq != null) {
                            etCantPaq.setText("0");
                            etCantPaq.setEnabled(false);
                        }
                        if (etCantUnid != null) etCantUnid.setEnabled(true);
                    } else {
                        if (etCantPaq != null) etCantPaq.setEnabled(true);
                        if (etCantUnid != null) etCantUnid.setEnabled(true);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void setupListeners() {
        if (btnVolver != null) btnVolver.setOnClickListener(v -> finish());

        if (spinnerProducto != null) {
            spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < productoList.size()) {
                        productoSeleccionado = productoList.get(position);
                        if (tvRefInfo != null) {
                            tvRefInfo.setText("Unidades/paq: " + productoSeleccionado.getUnidadesPorPaqueteVal() +
                                    " • P. Paq: S/ " + productoSeleccionado.getPrecioVentaPaquete() +
                                    " • P. Unid: S/ " + productoSeleccionado.getPrecioVentaUnidad());
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    productoSeleccionado = null;
                }
            });
        }

        if (btnAgregarComponente != null) {
            btnAgregarComponente.setOnClickListener(v -> agregarComponente());
        }

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> guardarOferta());
        }

        if (etDescuento != null) {
            etDescuento.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s != null && s.length() > 0 && etPrecioFinal != null && !etPrecioFinal.hasFocus()) {
                        try {
                            double desc = Double.parseDouble(s.toString().trim());
                            double pFinal = precioNormalCalculado * (1.0 - (desc / 100.0));
                            if (pFinal < 0) pFinal = 0;
                            etPrecioFinal.setText(String.format(Locale.US, "%.2f", pFinal));
                        } catch (Exception ignored) {}
                    }
                }
            });
        }
    }

    private void cargarProductos() {
        ApiClient.getApiService().getProductosDirectList().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(@NonNull Call<List<Producto>> call, @NonNull Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productoList = response.body();
                }
                ArrayAdapter<Producto> adapter = new ArrayAdapter<>(OfertaFormularioActivity.this, android.R.layout.simple_spinner_item, productoList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                if (spinnerProducto != null) spinnerProducto.setAdapter(adapter);
            }

            @Override
            public void onFailure(@NonNull Call<List<Producto>> call, @NonNull Throwable t) {
                ArrayAdapter<Producto> adapter = new ArrayAdapter<>(OfertaFormularioActivity.this, android.R.layout.simple_spinner_item, new ArrayList<>());
                if (spinnerProducto != null) spinnerProducto.setAdapter(adapter);
            }
        });
    }

    private void cargarDatosOfertaParaEditar(int id) {
        ApiClient.getApiService().getOfertas().enqueue(new Callback<OfertaResponse>() {
            @Override
            public void onResponse(@NonNull Call<OfertaResponse> call, @NonNull Response<OfertaResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    for (Oferta o : response.body().getData()) {
                        if (o.getId() == id) {
                            poblarFormulario(o);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<OfertaResponse> call, @NonNull Throwable t) {}
        });
    }

    private void poblarFormulario(Oferta oferta) {
        if (etNombre != null) etNombre.setText(oferta.getNombre());
        if (etPrecioFinal != null) etPrecioFinal.setText(oferta.getPrecioFinal());
        if (etDescuento != null) etDescuento.setText(oferta.getDescuento());

        componentRequests.clear();
        componenteNombres.clear();

        if (oferta.getItems() != null) {
            for (OfertaItem item : oferta.getItems()) {
                componentRequests.add(new OfertaItemRequest(
                        item.getProductoId(),
                        item.getTipoVenta(),
                        item.getCantidadPaquetes(),
                        item.getCantidadUnidades()
                ));
                componenteNombres.add(item.getProductoNombre());
            }
        }
        adminAdapter.notifyDataSetChanged();
        recalcularPrecioNormalPreview();
    }

    private void agregarComponente() {
        if (productoSeleccionado == null) {
            Toast.makeText(this, "Selecciona un producto", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipoVenta = spinnerTipoVenta != null && spinnerTipoVenta.getSelectedItem() != null ? spinnerTipoVenta.getSelectedItem().toString() : "UNIDAD";
        String paqStr = etCantPaq != null && etCantPaq.getText() != null ? etCantPaq.getText().toString().trim() : "0";
        String unidStr = etCantUnid != null && etCantUnid.getText() != null ? etCantUnid.getText().toString().trim() : "0";

        int cantPaq = 0;
        int cantUnid = 0;
        try { cantPaq = Integer.parseInt(paqStr); } catch (Exception ignored) {}
        try { cantUnid = Integer.parseInt(unidStr); } catch (Exception ignored) {}

        if ("PAQUETE".equalsIgnoreCase(tipoVenta) && cantPaq <= 0) {
            Toast.makeText(this, "Ingresa cantidad de paquetes válida (>0)", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("UNIDAD".equalsIgnoreCase(tipoVenta) && cantUnid <= 0) {
            Toast.makeText(this, "Ingresa cantidad de unidades válida (>0)", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("MIXTA".equalsIgnoreCase(tipoVenta) && (cantPaq <= 0 || cantUnid <= 0)) {
            Toast.makeText(this, "En venta mixta paquetes y unidades deben ser mayores a 0", Toast.LENGTH_SHORT).show();
            return;
        }

        componentRequests.add(new OfertaItemRequest(productoSeleccionado.getProductoId(), tipoVenta, cantPaq, cantUnid));
        componenteNombres.add(productoSeleccionado.getProductoNombre());
        adminAdapter.notifyDataSetChanged();
        recalcularPrecioNormalPreview();

        if (etCantPaq != null) etCantPaq.setText("0");
        if (etCantUnid != null) etCantUnid.setText("0");
        Toast.makeText(this, "Componente agregado", Toast.LENGTH_SHORT).show();
    }

    private void recalcularPrecioNormalPreview() {
        double totalNormal = 0.0;
        for (int i = 0; i < componentRequests.size(); i++) {
            OfertaItemRequest req = componentRequests.get(i);
            double pPaq = 0.0;
            double pUnid = 0.0;
            for (Producto p : productoList) {
                if (p.getProductoId() == req.getProductId()) {
                    try { pPaq = Double.parseDouble(p.getPrecioVentaPaquete()); } catch (Exception ignored) {}
                    try { pUnid = Double.parseDouble(p.getPrecioVentaUnidad()); } catch (Exception ignored) {}
                    break;
                }
            }
            if ("PAQUETE".equalsIgnoreCase(req.getTipoVenta())) {
                totalNormal += req.getCantidadPaquetes() * pPaq;
            } else if ("UNIDAD".equalsIgnoreCase(req.getTipoVenta())) {
                totalNormal += req.getCantidadUnidades() * pUnid;
            } else {
                totalNormal += (req.getCantidadPaquetes() * pPaq) + (req.getCantidadUnidades() * pUnid);
            }
        }
        precioNormalCalculado = totalNormal;
        if (tvPrecioNormalPreview != null) {
            tvPrecioNormalPreview.setText(String.format(Locale.US, "Precio Normal Calculado: S/ %.2f", precioNormalCalculado));
        }
    }

    private void guardarOferta() {
        String nombre = etNombre != null && etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String precioFinalStr = etPrecioFinal != null && etPrecioFinal.getText() != null ? etPrecioFinal.getText().toString().trim() : "";
        String descuentoStr = etDescuento != null && etDescuento.getText() != null ? etDescuento.getText().toString().trim() : "0.00";

        if (TextUtils.isEmpty(nombre)) {
            if (etNombre != null) etNombre.setError("Ingresa el nombre de la oferta");
            return;
        }

        if (componentRequests.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un componente a la oferta", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(precioFinalStr)) {
            if (etPrecioFinal != null) etPrecioFinal.setError("Ingresa el precio final");
            return;
        }

        double precioFinal = 0.0;
        try {
            precioFinal = Double.parseDouble(precioFinalStr);
        } catch (Exception e) {
            Toast.makeText(this, "Precio final inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (precioFinal <= 0) {
            Toast.makeText(this, "El precio final debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (precioNormalCalculado > 0 && precioFinal > precioNormalCalculado) {
            Toast.makeText(this, "El precio final no debe superar el precio normal calculado (S/ " + String.format(Locale.US, "%.2f", precioNormalCalculado) + ")", Toast.LENGTH_LONG).show();
            return;
        }

        OfertaRequest request = new OfertaRequest(nombre, precioFinalStr, descuentoStr, 1, componentRequests);
        if (btnGuardar != null) btnGuardar.setEnabled(false);

        if (ofertaIdEditando != -1) {
            ApiClient.getApiService().editarOferta(ofertaIdEditando, request).enqueue(new Callback<OfertaResponse>() {
                @Override
                public void onResponse(@NonNull Call<OfertaResponse> call, @NonNull Response<OfertaResponse> response) {
                    if (btnGuardar != null) btnGuardar.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(OfertaFormularioActivity.this, "¡Oferta actualizada exitosamente!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(OfertaFormularioActivity.this, "Error al actualizar oferta (" + response.code() + ")", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<OfertaResponse> call, @NonNull Throwable t) {
                    if (btnGuardar != null) btnGuardar.setEnabled(true);
                    Toast.makeText(OfertaFormularioActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            ApiClient.getApiService().crearOferta(request).enqueue(new Callback<OfertaResponse>() {
                @Override
                public void onResponse(@NonNull Call<OfertaResponse> call, @NonNull Response<OfertaResponse> response) {
                    if (btnGuardar != null) btnGuardar.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(OfertaFormularioActivity.this, "¡Oferta creada exitosamente!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(OfertaFormularioActivity.this, "Error al crear oferta (" + response.code() + ")", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<OfertaResponse> call, @NonNull Throwable t) {
                    if (btnGuardar != null) btnGuardar.setEnabled(true);
                    Toast.makeText(OfertaFormularioActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
