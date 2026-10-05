package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Compra {

    @SerializedName(value = "id", alternate = {"compra_id"})
    private int id;

    @SerializedName(value = "proveedor_id", alternate = {"id_proveedor"})
    private int proveedorId;

    @SerializedName(value = "proveedor_nombre", alternate = {"proveedor"})
    private String proveedorNombre;

    @SerializedName(value = "usuario_id", alternate = {"id_usuario"})
    private Integer usuarioId;

    @SerializedName(value = "total", alternate = {"monto_total"})
    private String total;

    @SerializedName(value = "fecha", alternate = {"created_at", "fecha_compra"})
    private String fecha;

    @SerializedName(value = "detalles", alternate = {"items", "detalle_compra"})
    private List<DetalleCompra> detalles;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(int proveedorId) {
        this.proveedorId = proveedorId;
    }

    public String getProveedorNombre() {
        return proveedorNombre != null ? proveedorNombre : "";
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTotal() {
        return total != null ? total : "0.00";
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getFecha() {
        return fecha != null ? fecha : "";
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public List<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = detalles;
    }
}
