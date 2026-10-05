package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PedidoRequest {

    @SerializedName(value = "cliente_id", alternate = {"usuario_id"})
    private Integer clienteId; // null for direct sales

    @SerializedName("metodo_pago")
    private String metodoPago;

    @SerializedName("items")
    private List<PedidoItemRequest> items;

    public PedidoRequest(Integer clienteId, String metodoPago, List<PedidoItemRequest> items) {
        this.clienteId = clienteId;
        this.metodoPago = metodoPago;
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

    public List<PedidoItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PedidoItemRequest> items) {
        this.items = items;
    }
}
