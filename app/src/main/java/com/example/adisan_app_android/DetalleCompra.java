package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;

public class DetalleCompra {

    @SerializedName(value = "id", alternate = {"detalle_id"})
    private int id;

    @SerializedName(value = "compra_id")
    private int compraId;

    @SerializedName(value = "producto_id")
    private int productoId;

    @SerializedName(value = "producto", alternate = {"producto_nombre", "nombre"})
    private String productoNombre;

    @SerializedName("cantidad_paquetes")
    private int cantidadPaquetes;

    @SerializedName("unidades_por_paquete_snapshot")
    private int unidadesPorPaqueteSnapshot;

    @SerializedName("unidades_ingresadas")
    private int unidadesIngresadas;

    @SerializedName(value = "precio_compra_paquete", alternate = {"precio_unitario", "precio"})
    private String precioCompraPaquete;

    @SerializedName("subtotal")
    private String subtotal;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompraId() {
        return compraId;
    }

    public void setCompraId(int compraId) {
        this.compraId = compraId;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre != null ? productoNombre : "";
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public int getCantidadPaquetes() {
        return cantidadPaquetes;
    }

    public void setCantidadPaquetes(int cantidadPaquetes) {
        this.cantidadPaquetes = cantidadPaquetes;
    }

    public int getUnidadesPorPaqueteSnapshot() {
        return unidadesPorPaqueteSnapshot;
    }

    public void setUnidadesPorPaqueteSnapshot(int unidadesPorPaqueteSnapshot) {
        this.unidadesPorPaqueteSnapshot = unidadesPorPaqueteSnapshot;
    }

    public int getUnidadesIngresadas() {
        return unidadesIngresadas > 0 ? unidadesIngresadas : (cantidadPaquetes * unidadesPorPaqueteSnapshot);
    }

    public void setUnidadesIngresadas(int unidadesIngresadas) {
        this.unidadesIngresadas = unidadesIngresadas;
    }

    // Métodos de compatibilidad legacy
    public int getCantidadUnidades() {
        return getUnidadesIngresadas();
    }

    public void setCantidadUnidades(int cantidadUnidades) {
        this.unidadesIngresadas = cantidadUnidades;
    }

    public String getPrecioCompraPaquete() {
        return precioCompraPaquete != null && !precioCompraPaquete.isEmpty() ? precioCompraPaquete : "0.00";
    }

    public void setPrecioCompraPaquete(String precioCompraPaquete) {
        this.precioCompraPaquete = precioCompraPaquete;
    }

    public String getPrecioUnitario() {
        return getPrecioCompraPaquete();
    }

    public void setPrecioUnitario(String precioUnitario) {
        this.precioCompraPaquete = precioUnitario;
    }

    public String getSubtotal() {
        return subtotal != null && !subtotal.isEmpty() ? subtotal : "0.00";
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }
}
