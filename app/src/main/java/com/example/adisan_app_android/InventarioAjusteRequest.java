package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;

public class InventarioAjusteRequest {

    @SerializedName("tipo")
    private String tipo; // 'entrada', 'salida', 'ajuste'

    @SerializedName("cantidad_unidades")
    private int cantidadUnidades;

    @SerializedName("motivo")
    private String motivo;

    public InventarioAjusteRequest(String tipo, int cantidadUnidades, String motivo) {
        this.tipo = tipo;
        this.cantidadUnidades = cantidadUnidades;
        this.motivo = motivo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCantidadUnidades() {
        return cantidadUnidades;
    }

    public void setCantidadUnidades(int cantidadUnidades) {
        this.cantidadUnidades = cantidadUnidades;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
