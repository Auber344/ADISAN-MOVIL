package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovimientoInventarioResponse {

    private boolean success;

    @SerializedName(value = "data", alternate = {"movimientos", "items", "results"})
    private List<MovimientoInventario> data;

    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<MovimientoInventario> getData() {
        return data;
    }

    public void setData(List<MovimientoInventario> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
