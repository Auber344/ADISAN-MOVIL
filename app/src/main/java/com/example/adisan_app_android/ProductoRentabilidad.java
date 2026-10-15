package com.example.adisan_app_android;

public class ProductoRentabilidad {

    private final int productoId;
    private final String nombre;
    private final int unidadesVendidas;
    private final double montoVentas;
    private final int porcentajeMargen;

    public ProductoRentabilidad(int productoId, String nombre, int unidadesVendidas, double montoVentas, int porcentajeMargen) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.unidadesVendidas = unidadesVendidas;
        this.montoVentas = montoVentas;
        this.porcentajeMargen = porcentajeMargen;
    }

    public int getProductoId() {
        return productoId;
    }

    public String getNombre() {
        return nombre != null ? nombre : "Producto #" + productoId;
    }

    public int getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public double getMontoVentas() {
        return montoVentas;
    }

    public int getPorcentajeMargen() {
        return porcentajeMargen;
    }
}
