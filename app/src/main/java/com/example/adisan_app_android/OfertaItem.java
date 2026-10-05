package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;

public class OfertaItem {

    @SerializedName(value = "id", alternate = {"detalle_id", "oferta_item_id"})
    private int id;

    @SerializedName(value = "oferta_id", alternate = {"id_oferta"})
    private int ofertaId;

    @SerializedName(value = "producto_id", alternate = {"product_id"})
    private int productoId;

    @SerializedName(value = "producto_nombre", alternate = {"nombre", "producto"})
    private String productoNombre;

    @SerializedName(value = "tipo_venta", alternate = {"tipoVenta"})
    private String tipoVenta;

    @SerializedName(value = "cantidad_paquetes", alternate = {"cantidadPaquetes"})
    private int cantidadPaquetes;

    @SerializedName(value = "cantidad_unidades", alternate = {"cantidadUnidades"})
    private int cantidadUnidades;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOfertaId() {
        return ofertaId;
    }

    public void setOfertaId(int ofertaId) {
        this.ofertaId = ofertaId;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre != null ? productoNombre : "Producto #" + productoId;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public String getTipoVenta() {
        return tipoVenta != null ? tipoVenta : "UNIDAD";
    }

    public void setTipoVenta(String tipoVenta) {
        this.tipoVenta = tipoVenta;
    }

    public int getCantidadPaquetes() {
        return cantidadPaquetes;
    }

    public void setCantidadPaquetes(int cantidadPaquetes) {
        this.cantidadPaquetes = cantidadPaquetes;
    }

    public int getCantidadUnidades() {
        return cantidadUnidades;
    }

    public void setCantidadUnidades(int cantidadUnidades) {
        this.cantidadUnidades = cantidadUnidades;
    }
}
