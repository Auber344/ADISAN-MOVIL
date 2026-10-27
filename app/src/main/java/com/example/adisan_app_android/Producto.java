package com.example.adisan_app_android;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public class Producto {

    @SerializedName("id")
    private int id;

    @SerializedName("producto_id")
    private int productoId;

    @SerializedName(value = "nombre", alternate = {"producto_nombre", "name", "title", "producto"})
    private String nombre;

    @SerializedName("categoria_id")
    private int categoriaId = 1;

    @SerializedName("producto_categoria_id")
    private int productoCategoriaId = 1;

    @SerializedName("proveedor_id")
    private int proveedorId = 1;

    @SerializedName("unidades_por_paquete")
    private int unidadesPorPaquete = 1;

    @SerializedName("precio_compra_paquete")
    private String precioCompraPaquete = "0.00";

    @SerializedName("precio_venta_paquete")
    private String precioVentaPaquete = "0.00";

    @SerializedName("precio_venta_unidad")
    private String precioVentaUnidad = "0.00";

    @SerializedName("activo")
    private int activo = 1;

    @SerializedName("stock_unidades")
    private int stockUnidades = 0;

    // Solo lectura: FirebaseApiService los agrega al listar para mostrar nombres en vez de ids
    @SerializedName("categoria_nombre")
    private String categoriaNombre;

    @SerializedName("proveedor_nombre")
    private String proveedorNombre;

    public String getCategoriaNombre() {
        return categoriaNombre != null && !categoriaNombre.isEmpty() ? categoriaNombre : "Sin categoría";
    }

    public String getProveedorNombre() {
        return proveedorNombre != null && !proveedorNombre.isEmpty() ? proveedorNombre : "Sin proveedor";
    }

    public int getId() {
        return id > 0 ? id : productoId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductoId() {
        return getId();
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
        this.id = productoId;
    }

    public String getNombre() {
        return nombre != null ? nombre : "";
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getProductoNombre() {
        return getNombre();
    }

    public void setProductoNombre(String productoNombre) {
        this.nombre = productoNombre;
    }

    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }

    public int getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(int proveedorId) {
        this.proveedorId = proveedorId;
    }

    public int getCategoriaId() {
        if (categoriaId > 0) return categoriaId;
        if (productoCategoriaId > 0) return productoCategoriaId;
        return 1;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    public int getUnidadesPorPaqueteVal() {
        return unidadesPorPaquete > 0 ? unidadesPorPaquete : 1;
    }

    public int getUnidadesPorPaquete() {
        return getUnidadesPorPaqueteVal();
    }

    public void setUnidadesPorPaquete(int unidadesPorPaquete) {
        this.unidadesPorPaquete = unidadesPorPaquete;
    }

    public String getPrecioCompraPaquete() {
        return precioCompraPaquete != null && !precioCompraPaquete.isEmpty() ? precioCompraPaquete : "0.00";
    }

    public void setPrecioCompraPaquete(String precioCompraPaquete) {
        this.precioCompraPaquete = precioCompraPaquete;
    }

    public String getPrecioVentaPaquete() {
        return precioVentaPaquete != null && !precioVentaPaquete.isEmpty() ? precioVentaPaquete : "0.00";
    }

    public void setPrecioVentaPaquete(String precioVentaPaquete) {
        this.precioVentaPaquete = precioVentaPaquete;
    }

    public String getPrecioVentaUnidad() {
        return precioVentaUnidad != null && !precioVentaUnidad.isEmpty() ? precioVentaUnidad : "0.00";
    }

    public void setPrecioVentaUnidad(String precioVentaUnidad) {
        this.precioVentaUnidad = precioVentaUnidad;
    }

    public int getStockUnidadesVal() {
        return stockUnidades;
    }

    public void setStockUnidades(int stockUnidades) {
        this.stockUnidades = stockUnidades;
    }

    // Métodos de compatibilidad legacy
    public String getPrecioVenta() {
        return getPrecioVentaPaquete();
    }

    public double getPrecioVentaDouble() {
        try {
            return Double.parseDouble(getPrecioVentaPaquete());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public int getStock() {
        return getStockUnidadesVal();
    }

    public void setStock(int stock) {
        this.stockUnidades = stock;
    }

    public int getPresentacionId() {
        return 1;
    }

    public void setPresentacionId(int presentacionId) {}

    public int getFactor() {
        return getUnidadesPorPaqueteVal();
    }

    @NonNull
    @Override
    public String toString() {
        return getNombre();
    }
}
