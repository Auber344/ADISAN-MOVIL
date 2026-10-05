package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;

public class PedidoItemRequest {

    @SerializedName(value = "product_id", alternate = {"producto_id"})
    private Integer productId;

    @SerializedName("tipo_venta")
    private String tipoVenta; // 'PAQUETE', 'UNIDAD', 'MIXTA'

    @SerializedName("cantidad_paquetes")
    private int cantidadPaquetes;

    @SerializedName("cantidad_unidades")
    private int cantidadUnidades;

    @SerializedName("tipo")
    private String tipo; // 'OFERTA'

    @SerializedName(value = "oferta_id", alternate = {"id_oferta"})
    private Integer ofertaId;

    @SerializedName(value = "cantidad_ofertas", alternate = {"cantidad_oferta", "cantidad"})
    private Integer cantidadOfertas;

    public PedidoItemRequest(int productId, String tipoVenta, int cantidadPaquetes, int cantidadUnidades) {
        this.productId = productId;
        this.tipoVenta = tipoVenta;
        this.cantidadPaquetes = cantidadPaquetes;
        this.cantidadUnidades = cantidadUnidades;
        this.tipo = null;
        this.ofertaId = null;
        this.cantidadOfertas = null;
    }

    public PedidoItemRequest(String tipo, int ofertaId, int cantidadOfertas) {
        this.tipo = tipo != null ? tipo : "OFERTA";
        this.ofertaId = ofertaId;
        this.cantidadOfertas = cantidadOfertas;
        this.productId = null;
        this.tipoVenta = null;
        this.cantidadPaquetes = 0;
        this.cantidadUnidades = 0;
    }

    public int getProductId() {
        return productId != null ? productId : 0;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getTipoVenta() {
        return tipoVenta != null ? tipoVenta : "";
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getOfertaId() {
        return ofertaId;
    }

    public void setOfertaId(Integer ofertaId) {
        this.ofertaId = ofertaId;
    }

    public Integer getCantidadOfertas() {
        return cantidadOfertas;
    }

    public void setCantidadOfertas(Integer cantidadOfertas) {
        this.cantidadOfertas = cantidadOfertas;
    }
}
