package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Oferta {

    @SerializedName(value = "id", alternate = {"oferta_id", "id_oferta"})
    private int id;

    @SerializedName(value = "nombre", alternate = {"nombre_oferta", "title", "name"})
    private String nombre;

    @SerializedName(value = "precio_normal", alternate = {"precioNormal", "precio_base"})
    private String precioNormal;

    @SerializedName(value = "precio_final", alternate = {"precioFinal", "precio_oferta"})
    private String precioFinal;

    @SerializedName(value = "descuento", alternate = {"porcentaje_descuento", "descuentoPorcentaje"})
    private String descuento;

    @SerializedName("activo")
    private Object activoObj;

    @SerializedName(value = "created_at", alternate = {"fecha_creacion", "fecha"})
    private String fecha;

    @SerializedName(value = "items", alternate = {"detalles", "componentes", "oferta_items"})
    private List<OfertaItem> items;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre != null ? nombre : "";
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrecioNormal() {
        return precioNormal != null ? precioNormal : "0.00";
    }

    public void setPrecioNormal(String precioNormal) {
        this.precioNormal = precioNormal;
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
        if (activoObj instanceof Number) {
            return ((Number) activoObj).intValue();
        }
        if (activoObj instanceof Boolean) {
            return ((Boolean) activoObj) ? 1 : 0;
        }
        try {
            return activoObj != null ? Integer.parseInt(activoObj.toString()) : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    public void setActivo(int activo) {
        this.activoObj = activo;
    }

    public String getFecha() {
        return fecha != null ? fecha : "";
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public List<OfertaItem> getItems() {
        return items;
    }

    public void setItems(List<OfertaItem> items) {
        this.items = items;
    }

    public double getPrecioNormalDouble() {
        try {
            return precioNormal != null ? Double.parseDouble(precioNormal) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getPrecioFinalDouble() {
        try {
            return precioFinal != null ? Double.parseDouble(precioFinal) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
