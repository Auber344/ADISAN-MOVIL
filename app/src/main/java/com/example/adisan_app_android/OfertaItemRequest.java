package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;

public class OfertaItemRequest {

    @SerializedName(value = "product_id", alternate = {"producto_id"})
    private int productId;

    @SerializedName("tipo_venta")
    private String tipoVenta; // 'PAQUETE', 'UNIDAD', 'MIXTA'

    @SerializedName("cantidad_paquetes")
    private int cantidadPaquetes;

    @SerializedName("cantidad_unidades")
    private int cantidadUnidades;

    public OfertaItemRequest(int productId, String tipoVenta, int cantidadPaquetes, int cantidadUnidades) {
        this.productId = productId;
        this.tipoVenta = tipoVenta != null ? tipoVenta : "UNIDAD";
        this.cantidadPaquetes = cantidadPaquetes;
        this.cantidadUnidades = cantidadUnidades;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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
