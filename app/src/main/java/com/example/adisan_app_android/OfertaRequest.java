package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OfertaRequest {

    @SerializedName("nombre")
    private String nombre;

    @SerializedName(value = "precio_final", alternate = {"precioFinal"})
    private String precioFinal;

    @SerializedName(value = "descuento", alternate = {"porcentaje_descuento", "descuentoPorcentaje"})
    private String descuento;

    @SerializedName("activo")
    private int activo = 1;

    @SerializedName("items")
    private List<OfertaItemRequest> items;

    public OfertaRequest(String nombre, String precioFinal, String descuento, int activo, List<OfertaItemRequest> items) {
        this.nombre = nombre;
        this.precioFinal = precioFinal;
        this.descuento = descuento;
        this.activo = activo;
        this.items = items;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrecioFinal() {
        return precioFinal != null ? precioFinal : "0.00";
    }

    public void setPrecioFinal(String precioFinal) {
        this.precioFinal = precioFinal;
    }

    public String getDescuento() {
        return descuento != null ? descuento : "0.00";
    }

    public void setDescuento(String descuento) {
        this.descuento = descuento;
    }

    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }

    public List<OfertaItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OfertaItemRequest> items) {
        this.items = items;
    }
}
