package com.example.adisan_app_android;

public class MovimientoFinanciero {

    public enum TipoMovimientoFinanciero {
        VENTA,
        COMPRA,
        GASTO,
        COBRO,
        PAGO
    }

    private String id;
    private TipoMovimientoFinanciero tipo;
    private String titulo;
    private String subtitulo;
    private String fecha;
    private double monto;
    private boolean esEntrada; // true for positive cash flow, false for expense/purchase

    public MovimientoFinanciero(String id, TipoMovimientoFinanciero tipo, String titulo, String subtitulo, String fecha, double monto, boolean esEntrada) {
        this.id = id;
        this.tipo = tipo;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.fecha = fecha;
        this.monto = monto;
        this.esEntrada = esEntrada;
    }

    public String getId() {
        return id;
    }

    public TipoMovimientoFinanciero getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public String getFecha() {
        return fecha;
    }

    public double getMonto() {
        return monto;
    }

    public boolean isEsEntrada() {
        return esEntrada;
    }
}
