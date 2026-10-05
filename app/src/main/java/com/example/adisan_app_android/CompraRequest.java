package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CompraRequest {

    @SerializedName("proveedor_id")
    private int proveedorId;

    @SerializedName("detalles")
    private List<CompraItemRequest> detalles;

    public static class CompraItemRequest {
        @SerializedName("producto_id")
        private int productoId;

        @SerializedName("cantidad_paquetes")
        private int cantidadPaquetes;

        @SerializedName("cantidad_unidades")
        private int cantidadUnidades;

        @SerializedName("precio_compra_paquete")
        private String precioCompraPaquete;

        public CompraItemRequest(int productoId, int cantidadPaquetes, int cantidadUnidades, String precioCompraPaquete) {
            this.productoId = productoId;
            this.cantidadPaquetes = cantidadPaquetes;
            this.cantidadUnidades = cantidadUnidades;
            this.precioCompraPaquete = precioCompraPaquete;
        }

        public int getProductoId() {
            return productoId;
        }

        public int getCantidadPaquetes() {
            return cantidadPaquetes;
        }

        public int getCantidadUnidades() {
            return cantidadUnidades;
        }

        public String getPrecioCompraPaquete() {
            return precioCompraPaquete;
        }

        public String getPrecioUnitario() {
            return precioCompraPaquete;
        }
    }

    public CompraRequest(int proveedorId, List<CompraItemRequest> detalles) {
        this.proveedorId = proveedorId;
        this.detalles = detalles;
    }

    public int getProveedorId() {
        return proveedorId;
    }

    public List<CompraItemRequest> getDetalles() {
        return detalles;
    }
}
