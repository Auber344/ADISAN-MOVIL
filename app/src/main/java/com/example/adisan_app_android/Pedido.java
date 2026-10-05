package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Pedido {

    @SerializedName(value = "id", alternate = {"pedido_id", "id_pedido"})
    private int id;

    @SerializedName(value = "cliente_id", alternate = {"usuario_id", "id_cliente"})
    private Integer clienteId;

    @SerializedName(value = "cliente", alternate = {"cliente_nombre", "nombres", "usuario", "nombre_cliente"})
    private String cliente;

    @SerializedName(value = "total", alternate = {"monto", "total_pedido"})
    private String total;

    @SerializedName(value = "metodo_pago", alternate = {"metodoPago", "metodo"})
    private String metodoPago;

    @SerializedName(value = "estado")
    private String estado; // 'pendiente', 'aceptado', 'en_camino', 'entregado', 'cancelado'

    @SerializedName(value = "fecha_estimada_entrega", alternate = {"fechaEstimadaEntrega"})
    private String fechaEstimadaEntrega;

    @SerializedName(value = "fecha", alternate = {"created_at", "fecha_creacion"})
    private String fecha;

    @SerializedName(value = "carrito", alternate = {"detalles", "items", "detalle_pedido", "detalle"})
    private List<DetallePedido> detalles;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public String getCliente() {
        if (clienteId == null || clienteId == 0 || cliente == null || "Venta Mostrador".equalsIgnoreCase(cliente)) {
            return "Venta directa";
        }
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getTotal() {
        return total != null ? total : "0.00";
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public double getTotalDouble() {
        try {
            return total != null ? Double.parseDouble(total) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getMetodoPago() {
        return metodoPago != null ? metodoPago : "Efectivo";
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado != null ? estado : "pendiente";
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaEstimadaEntrega() {
        return fechaEstimadaEntrega;
    }

    public void setFechaEstimadaEntrega(String fechaEstimadaEntrega) {
        this.fechaEstimadaEntrega = fechaEstimadaEntrega;
    }

    public String getFecha() {
        return fecha != null ? fecha : "";
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }
}
