package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PedidoRequest {

    @SerializedName(value = "cliente_id", alternate = {"usuario_id"})
    private Integer clienteId; // null for direct sales

    @SerializedName("metodo_pago")
    private String metodoPago;

    @SerializedName("total")
    private String total;

    @SerializedName(value = "items", alternate = {"carrito", "detalles"})
    private List<PedidoItemRequest> items;

    public PedidoRequest(Integer clienteId, String metodoPago, String total, List<PedidoItemRequest> items) {
        this.clienteId = clienteId;
        this.metodoPago = metodoPago;
        this.total = total;
        this.items = items;
    }

    public PedidoRequest(Integer clienteId, String metodoPago, List<PedidoItemRequest> items) {
        this.clienteId = clienteId;
        this.metodoPago = metodoPago;
        this.total = "0.00";
        this.items = items;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public String getMetodoPago() {
        return metodoPago != null ? metodoPago : "Efectivo";
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getTotal() {
        return total != null ? total : "0.00";
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<PedidoItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PedidoItemRequest> items) {
        this.items = items;
    }
}
