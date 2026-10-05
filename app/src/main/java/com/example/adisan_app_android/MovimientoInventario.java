package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;

public class MovimientoInventario {

    @SerializedName("id")
    private int id;

    @SerializedName(value = "producto_id", alternate = {"id_producto"})
    private int productoId;

    @SerializedName(value = "tipo", alternate = {"tipo_movimiento", "tipo_item"})
    private String tipo; // 'COMPRA', 'VENTA', 'REVERSIÓN', 'AJUSTE', 'PERDIDA', 'DEVOLUCION', 'entrada', 'salida'

    @SerializedName(value = "cantidad_unidades", alternate = {"cantidad", "unidades", "delta"})
    private int cantidadUnidades;

    @SerializedName(value = "motivo", alternate = {"observacion", "descripcion"})
    private String motivo;

    @SerializedName(value = "usuario_id", alternate = {"id_usuario"})
    private Integer usuarioId;

    @SerializedName(value = "usuario_nombre", alternate = {"usuario", "nombre_usuario", "responsable"})
    private String usuarioNombre;

    @SerializedName(value = "fecha", alternate = {"created_at", "fecha_movimiento", "fecha_creacion"})
    private String fecha;

    @SerializedName(value = "stock_anterior", alternate = {"stockAnterior", "stock_inicial"})
    private Integer stockAnterior;

    @SerializedName(value = "stock_nuevo", alternate = {"stockNuevo", "stock_final", "stock_resultante"})
    private Integer stockNuevo;

    @SerializedName(value = "pedido_id", alternate = {"pedidoId", "id_pedido"})
    private Integer pedidoId;

    @SerializedName(value = "compra_id", alternate = {"compraId", "id_compra"})
    private Integer compraId;

    @SerializedName(value = "proveedor_nombre", alternate = {"proveedor", "nombre_proveedor"})
    private String proveedorNombre;

    @SerializedName(value = "cliente_nombre", alternate = {"cliente", "nombre_cliente"})
    private String clienteNombre;

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

    public String getTipo() {
        return tipo != null ? tipo : "";
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
        return motivo != null ? motivo : "";
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getFecha() {
        return fecha != null ? fecha : "";
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Integer getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(Integer stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public Integer getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(Integer stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public Integer getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Integer pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Integer getCompraId() {
        return compraId;
    }

    public void setCompraId(Integer compraId) {
        this.compraId = compraId;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }
}
