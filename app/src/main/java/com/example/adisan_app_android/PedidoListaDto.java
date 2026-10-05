package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PedidoListaDto {

    @SerializedName(value = "id", alternate = {"pedido_id", "id_pedido"})
    private Integer id;

    @SerializedName(value = "cliente_id", alternate = {"usuario_id", "id_cliente"})
    private Integer clienteId;

    @SerializedName(value = "cliente", alternate = {"cliente_nombre", "nombres", "usuario", "nombre_cliente"})
    private String cliente;

    @SerializedName(value = "total", alternate = {"monto", "total_pedido"})
    private Double total;

    @SerializedName(value = "metodo_pago", alternate = {"metodoPago", "metodo"})
    private String metodoPago;

    @SerializedName(value = "estado")
    private String estado;

    @SerializedName(value = "fecha", alternate = {"created_at", "fecha_creacion"})
    private String fecha;

    @SerializedName(value = "carrito", alternate = {"detalles", "items", "detalle_pedido", "detalle"})
    private List<DetallePedido> detalles;

    public Integer getId() { return id != null ? id : 0; }
    public Integer getClienteId() { return clienteId; }
    public String getCliente() {
        if (clienteId == null || clienteId == 0 || cliente == null || "Venta Mostrador".equalsIgnoreCase(cliente)) {
            return "Venta directa";
        }
        return cliente;
    }
    public Double getTotal() { return total != null ? total : 0.0; }
    public String getEstado() { return estado != null ? estado : "pendiente"; }
    public String getMetodoPago() { return metodoPago != null ? metodoPago : "Efectivo"; }
    public String getFecha() { return fecha != null ? fecha : ""; }
    public List<DetallePedido> getDetalles() { return detalles; }
}
