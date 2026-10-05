package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InventarioResponse {

    private boolean success;

    @SerializedName(value = "data", alternate = {"inventario", "items", "results"})
    private List<Inventario> data;

    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Inventario> getData() {
        return data;
    }

    public void setData(List<Inventario> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
