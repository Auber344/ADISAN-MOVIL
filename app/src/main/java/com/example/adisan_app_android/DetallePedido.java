package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;

public class DetallePedido {

    @SerializedName(value = "id", alternate = {"detalle_id"})
    private int id;

    @SerializedName(value = "pedido_id")
    private int pedidoId;

    @SerializedName(value = "producto_id")
    private int productoId;

    @SerializedName(value = "presentacion_id")
    private Integer presentacionId;

    @SerializedName(value = "producto_nombre", alternate = {"nombre", "producto"})
    private String productoNombre;

    @SerializedName(value = "cantidad")
    private int cantidad;

    @SerializedName(value = "precio")
    private String precio;

    @SerializedName(value = "subtotal")
    private String subtotal;

    @SerializedName(value = "nombre_oferta")
    private String nombreOferta;

    @SerializedName("tipo_venta")
    private String tipoVenta;

    @SerializedName("cantidad_paquetes")
    private int cantidadPaquetes;

    @SerializedName("cantidad_unidades")
    private int cantidadUnidades;

    @SerializedName(value = "unidades_por_paquete_snapshot", alternate = {"unidadesPorPaqueteSnapshot", "factor"})
    private Integer unidadesPorPaqueteSnapshot;

    @SerializedName(value = "unidades_totales", alternate = {"unidadesTotales"})
    private Integer unidadesTotales;

    @SerializedName(value = "precio_paquete_snapshot", alternate = {"precioPaqueteSnapshot", "precio_paquete"})
    private String precioPaqueteSnapshot;

    @SerializedName(value = "precio_unidad_snapshot", alternate = {"precioUnidadSnapshot", "precio_unidad"})
    private String precioUnidadSnapshot;

    @SerializedName(value = "nombre_producto_snapshot", alternate = {"nombreProductoSnapshot"})
    private String nombreProductoSnapshot;

    @SerializedName(value = "tipo", alternate = {"tipo_item", "tipoItem"})
    private String tipo;

    @SerializedName(value = "oferta_id", alternate = {"ofertaId", "id_oferta"})
    private Integer ofertaId;

    @SerializedName(value = "cantidad_ofertas", alternate = {"cantidadOfertas", "cantidad_oferta"})
    private Integer cantidadOfertas;

    public boolean isOferta() {
        return "OFERTA".equalsIgnoreCase(tipo) || (ofertaId != null && ofertaId > 0) || (nombreOferta != null && !nombreOferta.isEmpty() && (tipoVenta == null || tipoVenta.isEmpty()));
    }

    public String getItemNombre() {
        if (isOferta()) {
            if (nombreOferta != null && !nombreOferta.isEmpty()) return nombreOferta;
            return "Oferta #" + (ofertaId != null ? ofertaId : "");
        } else {
            if (nombreProductoSnapshot != null && !nombreProductoSnapshot.isEmpty()) return nombreProductoSnapshot;
            if (productoNombre != null && !productoNombre.isEmpty()) return productoNombre;
            return "Producto #" + productoId;
        }
    }

    public String getModalidadDescripcion() {
        if (isOferta()) {
            return "OFERTA (" + (cantidadOfertas != null && cantidadOfertas > 0 ? cantidadOfertas : cantidad) + " ofertas)";
        }
        String tv = tipoVenta != null ? tipoVenta : "UNIDAD";
        if ("PAQUETE".equalsIgnoreCase(tv)) {
            int paq = cantidadPaquetes > 0 ? cantidadPaquetes : cantidad;
            if (unidadesPorPaqueteSnapshot != null && unidadesPorPaqueteSnapshot > 0) {
                return paq + (paq == 1 ? " paquete (" : " paquetes (") + unidadesPorPaqueteSnapshot + " un.)";
            }
            return paq + (paq == 1 ? " paquete" : " paquetes");
        } else if ("UNIDAD".equalsIgnoreCase(tv)) {
            int uni = cantidadUnidades > 0 ? cantidadUnidades : cantidad;
            return uni + (uni == 1 ? " unidad" : " unidades");
        } else if ("MIXTA".equalsIgnoreCase(tv)) {
            return cantidadPaquetes + " paq. + " + cantidadUnidades + " un.";
        } else {
            return cantidad + " unidades";
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    public Integer getPresentacionId() { return presentacionId; }
    public void setPresentacionId(Integer presentacionId) { this.presentacionId = presentacionId; }
    public String getProductoNombre() { return productoNombre != null ? productoNombre : "Producto #" + productoId; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getPrecio() { return precio != null ? precio : "0.00"; }
    public void setPrecio(String precio) { this.precio = precio; }
    public String getSubtotal() { return subtotal != null ? subtotal : "0.00"; }
    public void setSubtotal(String subtotal) { this.subtotal = subtotal; }
    public String getNombreOferta() { return nombreOferta; }
    public void setNombreOferta(String nombreOferta) { this.nombreOferta = nombreOferta; }
    public String getTipoVenta() { return tipoVenta != null ? tipoVenta : "UNIDAD"; }
    public void setTipoVenta(String tipoVenta) { this.tipoVenta = tipoVenta; }
    public int getCantidadPaquetes() { return cantidadPaquetes; }
    public void setCantidadPaquetes(int cantidadPaquetes) { this.cantidadPaquetes = cantidadPaquetes; }
    public int getCantidadUnidades() { return cantidadUnidades; }
    public void setCantidadUnidades(int cantidadUnidades) { this.cantidadUnidades = cantidadUnidades; }
    public Integer getUnidadesPorPaqueteSnapshot() { return unidadesPorPaqueteSnapshot; }
    public Integer getUnidadesTotales() {
        if (unidadesTotales != null && unidadesTotales > 0) {
            return unidadesTotales;
        }
        int factor = (unidadesPorPaqueteSnapshot != null && unidadesPorPaqueteSnapshot > 0) ? unidadesPorPaqueteSnapshot : 1;
        int paq = cantidadPaquetes;
        int uni = cantidadUnidades;
        if (paq == 0 && uni == 0) {
            if ("PAQUETE".equalsIgnoreCase(tipoVenta)) {
                paq = cantidad;
            } else {
                uni = cantidad;
            }
        }
        int tot = (paq * factor) + uni;
        return tot > 0 ? tot : (cantidad > 0 ? cantidad : 1);
    }
    public String getPrecioPaqueteSnapshot() { return precioPaqueteSnapshot; }
    public String getPrecioUnidadSnapshot() { return precioUnidadSnapshot; }
    public String getNombreProductoSnapshot() { return nombreProductoSnapshot; }
    public String getTipo() { return tipo; }
    public Integer getOfertaId() { return ofertaId; }
    public Integer getCantidadOfertas() { return cantidadOfertas; }

    public double getPrecioDouble() {
        try {
            if (precioPaqueteSnapshot != null) return Double.parseDouble(precioPaqueteSnapshot);
            if (precioUnidadSnapshot != null) return Double.parseDouble(precioUnidadSnapshot);
            return precio != null ? Double.parseDouble(precio) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getSubtotalDouble() {
        try {
            return subtotal != null ? Double.parseDouble(subtotal) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
