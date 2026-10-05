package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;

public class Inventario {

    @SerializedName(value = "id", alternate = {"inventario_id"})
    private int id;

    @SerializedName(value = "producto_id", alternate = {"id_producto"})
    private int productoId;

    @SerializedName(value = "stock_unidades", alternate = {"stock", "cantidad_unidades", "existencia"})
    private int stockUnidades;

    @SerializedName(value = "producto_nombre", alternate = {"nombre", "producto"})
    private String productoNombre;

    @SerializedName(value = "unidades_por_paquete", alternate = {"factor"})
    private Integer unidadesPorPaquete;

    @SerializedName(value = "categoria", alternate = {"categoria_nombre", "nombre_categoria"})
    private String categoria;

    public String getCategoria() {
        return categoria != null && !categoria.isEmpty() ? categoria : "GENERAL";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public int getStockUnidades() {
        return stockUnidades;
    }

    public void setStockUnidades(int stockUnidades) {
        this.stockUnidades = stockUnidades;
    }

    public String getProductoNombre() {
        return productoNombre != null ? productoNombre : "";
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public int getUnidadesPorPaquete() {
        return unidadesPorPaquete != null && unidadesPorPaquete > 0 ? unidadesPorPaquete : 1;
    }

    public void setUnidadesPorPaquete(Integer unidadesPorPaquete) {
        this.unidadesPorPaquete = unidadesPorPaquete;
    }

    public int getPaquetesDisponibles() {
        int upp = getUnidadesPorPaquete();
        return upp > 0 ? stockUnidades / upp : 0;
    }

    public int getUnidadesSueltas() {
        int upp = getUnidadesPorPaquete();
        return upp > 0 ? stockUnidades % upp : stockUnidades;
    }
}
